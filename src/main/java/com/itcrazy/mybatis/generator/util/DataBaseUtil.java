package com.itcrazy.mybatis.generator.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

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
    public static Connection getConnection(DatabaseConnectionConfig config) throws ClassNotFoundException, SQLException {
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
            List<String> tableNameList = new ArrayList<>();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs;
            if (Objects.equals(DataBaseTypeEnum.SQLServer, dataBaseType)) {
                String sql = "select name from sysobjects  where xtype='u' or xtype='v' ";
                rs = connection.createStatement().executeQuery(sql);
                while (rs.next()) {
                    tableNameList.add(rs.getString("name"));
                }
            } else if (Objects.equals(DataBaseTypeEnum.Oracle, dataBaseType)) {
                rs = metaData.getTables(null, databaseConfig.getUserName().toUpperCase(), null, new String[]{"TABLE", "VIEW"});
            } else if (Objects.equals(DataBaseTypeEnum.PostgreSQL, dataBaseType)) {
                // 针对 postgresql 的左侧数据表显示
                rs = metaData.getTables(null, "%", "%", new String[]{"TABLE", "VIEW"});
            } else {
                // 默认mysql数据库
                // rs = metaData.getTables(null, "%", "%", new String[]{"TABLE"});
                // 获取schema下的表
                rs = metaData.getTables(databaseConfig.getSchemaName(), "%", "%", new String[]{"TABLE"});
            }
            while (rs.next()) {
                tableNameList.add(rs.getString(3));
            }
            return tableNameList;
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
            ResultSet rs = metaData.getColumns(null, null, tableName, null);
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
    public static String buildConnectionUrlWithSchema(DatabaseConnectionConfig dbConfig) throws ClassNotFoundException {
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
