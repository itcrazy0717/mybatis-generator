package com.itcrazy.mybatis.generator.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.internal.util.ClassloaderUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.enums.DataBaseTypeEnum;
import com.itcrazy.mybatis.generator.model.DatabaseConnectionConfig;
import com.itcrazy.mybatis.generator.model.TableColumn;


/**
 * @author: itcrazy0717
 * @version: $ DataBaseUtil.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:数据库工具
 */
public class DataBaseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseUtil.class);

    /**
     * sqlite db 地址
     */
    private static final String SQLITE_URL = "jdbc:sqlite:./config/sqlite3.db";

    /**
     * 数据库连接超时时间
     */
    private static final int DATABASE_CONNECTION_TIMEOUT_SECOND = 3;

    /**
     * 数据库驱动集合
     */
    private final static Map<DataBaseTypeEnum, Driver> DATABASE_DRIVER_MAP;

    static {
        DATABASE_DRIVER_MAP = new HashMap<>();
        List<String> driverJars = SqliteUtil.getAllDataBaseDriverJarPath();
        ClassLoader classloader = ClassloaderUtility.getCustomClassloader(driverJars);
        DataBaseTypeEnum[] dataBaseTypeList = DataBaseTypeEnum.values();
        for (DataBaseTypeEnum dataBaseType : dataBaseTypeList) {
            try {
                Class<?> clazz = Class.forName(dataBaseType.getDriverClass(), true, classloader);
                Driver driver = (Driver) clazz.newInstance();
                LOGGER.info("load driver class: {}", driver);
                DATABASE_DRIVER_MAP.put(dataBaseType, driver);
            } catch (Exception e) {
                LOGGER.error("load_driver_error", e);
                throw new RuntimeException("载入驱动文件异常，请联系开发者");
            }
        }
    }

    /**
     * 获取数据库连接
     * by itcrazy0717
     *
     * @param config
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection getConnection(DatabaseConnectionConfig config) throws Exception {
        String url = buildConnectionUrlWithSchema(config);
        Properties props = new Properties();

        props.setProperty("user", config.getUserName());
        props.setProperty("password", config.getPassword());

        DriverManager.setLoginTimeout(DATABASE_CONNECTION_TIMEOUT_SECOND);
        Connection connection = DATABASE_DRIVER_MAP.get(DataBaseTypeEnum.valueOf(config.getDataBaseType())).connect(url, props);
        LOGGER.info("getConnection, connection url: {}", connection);
        return connection;
    }

    /**
     * 获取数据库库表名集合
     * by itcrazy0717
     *
     * @param databaseConfig
     * @return
     * @throws Exception
     */
    public static List<String> getTableNameList(DatabaseConnectionConfig databaseConfig) throws Exception {
        String url = buildConnectionUrlWithSchema(databaseConfig);
        LOGGER.info("get_table_name_list, connection url: {}", url);
        try (Connection connection = getConnection(databaseConfig)) {
            // 获取数据库类型
            DataBaseTypeEnum dataBaseType = DataBaseTypeEnum.valueOf(databaseConfig.getDataBaseType());
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs;
            // SQLServer结果集无TABLE_SCHEM列、Oracle需按用户名大写比对、PostgreSQL按原行为不过滤，
            // 因此这三类各自在分支内直接收集；DM与MySQL走统一校验收集
            if (Objects.equals(DataBaseTypeEnum.SQLServer, dataBaseType)) {
                String sql = "select name from sysobjects  where xtype='u' or xtype='v' ";
                rs = connection.createStatement().executeQuery(sql);
                List<String> tableNameList = new ArrayList<>();
                while (rs.next()) {
                    tableNameList.add(rs.getString("name"));
                }
                return tableNameList;
            } else if (Objects.equals(DataBaseTypeEnum.Oracle, dataBaseType)) {
                // Oracle的schema为用户名大写，TABLE_SCHEM校验过滤掉账号有权限访问的其他schema的同名表
                String oracleSchema = databaseConfig.getUserName().toUpperCase();
                rs = metaData.getTables(null, oracleSchema, null, new String[]{"TABLE", "VIEW"});
                Set<String> tableNameSet = new LinkedHashSet<>();
                while (rs.next()) {
                    if (StringUtils.equalsIgnoreCase(oracleSchema, rs.getString(2))) {
                        tableNameSet.add(rs.getString(3));
                    }
                }
                return new ArrayList<>(tableNameSet);
            } else if (Objects.equals(DataBaseTypeEnum.PostgreSQL, dataBaseType)) {
                // 针对 postgresql 的左侧数据表显示，保持原有行为：显示所有schema的表
                rs = metaData.getTables(null, "%", "%", new String[]{"TABLE", "VIEW"});
            } else if (Objects.equals(DataBaseTypeEnum.DM8, dataBaseType)) {
                // DM基于账号可访问对象返回表，schemaPattern传具体模式名，
                // 避免账号有权限的其他模式的表通过同义词/授权混入
                rs = metaData.getTables(databaseConfig.getSchemaName(), databaseConfig.getSchemaName(), "%", new String[]{"TABLE"});
            } else {
                // 默认mysql数据库
                // rs = metaData.getTables(null, "%", "%", new String[]{"TABLE"});
                // 获取schema下的表
                rs = metaData.getTables(databaseConfig.getSchemaName(), "%", "%", new String[]{"TABLE"});
            }
            // DM与MySQL统一按模式名过滤并去重（保持顺序）：
            // 部分驱动的schemaPattern过滤不严，账号有权限的其他模式的同名表会混入结果集，
            // TABLE_SCHEM（rs.getString(2)）校验作为最终防线
            String schemaName = databaseConfig.getSchemaName();
            Set<String> tableNameSet = new LinkedHashSet<>();
            while (rs.next()) {
                if (StringUtils.equalsIgnoreCase(schemaName, rs.getString(2))) {
                    tableNameSet.add(rs.getString(3));
                }
            }
            return new ArrayList<>(tableNameSet);
        }
    }

    /**
     * 获取数据表列
     * by itcrazy0717
     *
     * @param dbConfig
     * @param tableName
     * @return
     * @throws Exception
     */
    public static List<TableColumn> getTableColumns(DatabaseConnectionConfig dbConfig, String tableName) throws Exception {
        String url = buildConnectionUrlWithSchema(dbConfig);
        LOGGER.info("getTableColumns, connection url: {}", url);
        try (Connection conn = getConnection(dbConfig)) {
            DatabaseMetaData metaData = conn.getMetaData();
            // schema传null会返回账号可访问的其他模式同名表的列，导致列重复，统一传schemaName过滤
            // （MySQL的schema参数即库名；DM为模式名，行为一致）
            ResultSet rs = metaData.getColumns(null, dbConfig.getSchemaName(), tableName, null);
            List<TableColumn> columns = new ArrayList<>();
            while (rs.next()) {
                TableColumn tableColumn = new TableColumn();
                String columnName = rs.getString("COLUMN_NAME");
                tableColumn.setColumnName(columnName);
                tableColumn.setJdbcType(rs.getString("TYPE_NAME"));
                columns.add(tableColumn);
            }
            return columns;
        }
    }

    /**
     * 构建数据库连接url
     * by itcrazy0717
     *
     * @param dbConfig
     * @return
     * @throws ClassNotFoundException
     */
    public static String buildConnectionUrlWithSchema(DatabaseConnectionConfig dbConfig) throws Exception {
        DataBaseTypeEnum dataBaseType = DataBaseTypeEnum.valueOf(dbConfig.getDataBaseType());
        String connectionUrl = String.format(dataBaseType.getConnectionUrlPattern(), dbConfig.getHostUrl(), dbConfig.getPort(), dbConfig.getSchemaName(), dbConfig.getEncoding());
        LOGGER.info("get_connection_url_with_schema, connection url: {}", connectionUrl);
        return connectionUrl;
    }

    /**
     * 获取 sqlite 连接
     * by itcrazy0717
     *
     * @return
     * @throws Exception
     */
    public static Connection getSqLiteConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(SQLITE_URL);
    }

}
