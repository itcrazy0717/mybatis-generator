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
import com.itcrazy.mybatis.generator.dto.DatabaseConfig;
import com.itcrazy.mybatis.generator.dto.TableColumn;


/**
 * @author: itcrazy0717
 * @version: $ DataBaseUtil.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
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
    private static final int DB_CONNECTION_TIMEOUTS_SECONDS = 1;

    /**
     * 数据库驱动集合
     */
    private final static Map<DataBaseTypeEnum, Driver> DATABASE_DRIVER_MAP;

    static {
        DATABASE_DRIVER_MAP = new HashMap<>();
        List<String> driverJars = MybatisCodeGenerateConfigUtil.getAllJDBCDriverJarPaths();
        ClassLoader classloader = ClassloaderUtility.getCustomClassloader(driverJars);
        DataBaseTypeEnum[] dbTypes = DataBaseTypeEnum.values();
        for (DataBaseTypeEnum dbType : dbTypes) {
            try {
                Class<?> clazz = Class.forName(dbType.getDriverClass(), true, classloader);
                Driver driver = (Driver) clazz.newInstance();
                LOGGER.info("load driver class: {}", driver);
                DATABASE_DRIVER_MAP.put(dbType, driver);
            } catch (Exception e) {
                LOGGER.error("load driver error");
            }
        }
    }

    public static Connection getConnection(DatabaseConfig config) throws ClassNotFoundException, SQLException {
        String url = getConnectionUrlWithSchema(config);
        Properties props = new Properties();

        props.setProperty("user", config.getUserName());
        props.setProperty("password", config.getPassword());

        DriverManager.setLoginTimeout(DB_CONNECTION_TIMEOUTS_SECONDS);
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
    public static List<String> getTableNameList(DatabaseConfig databaseConfig) throws Exception {
        String url = getConnectionUrlWithSchema(databaseConfig);
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

    public static List<TableColumn> getTableColumns(DatabaseConfig dbConfig, String tableName) throws Exception {
        String url = getConnectionUrlWithSchema(dbConfig);
        LOGGER.info("getTableColumns, connection url: {}", url);
        Connection conn = getConnection(dbConfig);
        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getColumns(null, null, tableName, null);
            List<TableColumn> columns = new ArrayList<>();
            while (rs.next()) {
                TableColumn tableColumn = new TableColumn();
                String columnName = rs.getString("COLUMN_NAME");
                tableColumn.setColumnName(columnName);
                tableColumn.setJdbcType(rs.getString("TYPE_NAME"));
                columns.add(tableColumn);
            }
            return columns;
        } finally {
            conn.close();
        }
    }

    public static String getConnectionUrlWithSchema(DatabaseConfig dbConfig) throws ClassNotFoundException {
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
