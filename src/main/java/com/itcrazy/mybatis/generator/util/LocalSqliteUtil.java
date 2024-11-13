package com.itcrazy.mybatis.generator.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.itcrazy.mybatis.generator.constant.SqliteConstants;
import com.itcrazy.mybatis.generator.enums.DataBaseTypeEnum;
import com.itcrazy.mybatis.generator.model.DatabaseConnectionConfig;
import com.itcrazy.mybatis.generator.model.MybatisCodeGenerateConfig;

/**
 * @author: itcrazy0717
 * @version: $ MybatisCodeGenerateConfigUtil.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class LocalSqliteUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalSqliteUtil.class);

	/**
	 * 配置目录
	 */
	private static final String LOCAL_CONFIG_DATABASE_DIR = "config";

	/**
	 * 配置文件路径
	 */
	private static final String SQLITE_FILE = "/sqlite3.db";

	/**
	 * 生成配置数据库
	 * by itcrazy0717
	 *
	 * @throws Exception
	 */
    public static void createConfigSqlite() throws Exception {
        File file = new File(LOCAL_CONFIG_DATABASE_DIR);
        if (!file.exists()) {
            file.mkdir();
        }
        File cofigSqliteFile = new File(LOCAL_CONFIG_DATABASE_DIR + SQLITE_FILE);
        if (!cofigSqliteFile.exists()) {
            createSqlite(cofigSqliteFile);
        }
    }

	/**
	 * 生成sqlite
	 * by itcrazy0717
	 *
	 * @param file
	 * @throws IOException
	 */
	public static void createSqlite(File file) throws IOException {
		InputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = Thread.currentThread().getContextClassLoader().getResourceAsStream("sqlite3.db");
			if (Objects.isNull(fis)) {
				throw new RuntimeException("默认配置数据库不存在");
			}
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int byteRead = 0;
            while ((byteRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, byteRead);
            }
        } finally {
	        if (Objects.nonNull(fis)) {
		        fis.close();
	        }
	        if (Objects.nonNull(fos)) {
		        fos.close();
	        }
        }
    }

	/**
	 * 导入数据库连接配置
	 * by itcrazy0717
	 *
	 * @return
	 * @throws Exception
	 */
	public static List<DatabaseConnectionConfig> loadDatabaseConnectionConfig() throws Exception {
        Connection conn = null;
        Statement stat = null;
        ResultSet rs = null;
        try {
            conn = DataBaseUtil.getSqLiteConnection();
            stat = conn.createStatement();
            rs = stat.executeQuery("SELECT * FROM database_connection_config");
            List<DatabaseConnectionConfig> configs = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String value = rs.getString("value");
                DatabaseConnectionConfig databaseConfig = JSON.parseObject(value, DatabaseConnectionConfig.class);
                databaseConfig.setId(id);
                configs.add(databaseConfig);
            }

            return configs;
        } finally {
	        if (Objects.nonNull(rs)) {
		        rs.close();
	        }
	        if (Objects.nonNull(stat)) {
		        stat.close();
	        }
	        if (Objects.nonNull(conn)) {
		        conn.close();
	        }
        }
    }

	/**
	 * 保存数据库连接配置
	 * by itcrazy0717
	 *
	 * @param isUpdate
	 * @param primaryKey
	 * @param dbConfig
	 * @throws Exception
	 */
	public static void saveDatabaseConnectionConfig(DatabaseConnectionConfig dbConfig, Integer primaryKey, boolean isUpdate) throws Exception {
        String configName = dbConfig.getName();
        Connection conn = null;
        Statement stat = null;
        try {
            conn = DataBaseUtil.getSqLiteConnection();
            stat = conn.createStatement();
            if (!isUpdate) {
                ResultSet rs1 = stat.executeQuery("SELECT * from database_connection_config where name = '" + configName + "'");
                if (rs1.next()) {
                    throw new RuntimeException("配置已经存在, 请使用其它名字");
                }
            }
            String jsonStr = JSON.toJSONString(dbConfig);
            String sql;
            if (isUpdate) {
                sql = String.format("UPDATE database_connection_config SET name = '%s', value = '%s' where id = %d", configName, jsonStr, primaryKey);
            } else {
                sql = String.format("INSERT INTO database_connection_config (name, value) values('%s', '%s')", configName, jsonStr);
            }
            stat.executeUpdate(sql);
        } finally {
	        if (Objects.nonNull(stat)) {
		        stat.close();
	        }
	        if (Objects.nonNull(conn)) {
		        conn.close();
	        }
        }
    }

	/**
	 * 删除数据库连接配置
	 * by itcrazy0717
	 *
	 * @param databaseConfig
	 * @throws Exception
	 */
	public static void deleteDatabaseConnectionConfig(DatabaseConnectionConfig databaseConfig) throws Exception {
        Connection conn = null;
        Statement stat = null;
        try {
            conn = DataBaseUtil.getSqLiteConnection();
            stat = conn.createStatement();
            String sql = String.format("delete from database_connection_config where id=%d", databaseConfig.getId());
            stat.executeUpdate(sql);
        } finally {
	        if (Objects.nonNull(stat)) {
		        stat.close();
	        }
	        if (Objects.nonNull(conn)) {
		        conn.close();
	        }
        }
    }

	/**
	 * 保存代码生成配置
	 * by itcrazy0717
	 *
	 * @param codeGenerateConfig
	 * @throws Exception
	 */
    public static void saveCodeGenerateConfig(MybatisCodeGenerateConfig codeGenerateConfig) throws Exception {
        Connection conn = null;
        Statement stat = null;
        try {
            conn = DataBaseUtil.getSqLiteConnection();
            stat = conn.createStatement();
            String configJson = JSON.toJSONString(codeGenerateConfig);
            String insertSql = String.format("INSERT INTO code_generate_config values('%s', '%s')", codeGenerateConfig.getName(), configJson);
            stat.executeUpdate(insertSql);
        } finally {
	        if (Objects.nonNull(stat)) {
		        stat.close();
	        }
	        if (Objects.nonNull(conn)) {
		        conn.close();
	        }
        }
    }

	/**
	 * 根据名称导入代码生成配置
	 * by itcrazy0717
	 *
	 * @param name
	 * @return
	 * @throws Exception
	 */
    public static MybatisCodeGenerateConfig loadCodeGenerateConfigByName(String name) throws Exception {
        Connection conn = null;
        Statement stat = null;
        ResultSet rs = null;
        try {
            conn = DataBaseUtil.getSqLiteConnection();
            stat = conn.createStatement();
            String selectSql = String.format("SELECT * FROM code_generate_config where name='%s'", name);
            LOGGER.info("sql: {}", selectSql);
            rs = stat.executeQuery(selectSql);
            MybatisCodeGenerateConfig generatorConfig = null;
            if (rs.next()) {
                String value = rs.getString("value");
                generatorConfig = JSON.parseObject(value, MybatisCodeGenerateConfig.class);
            }
            return generatorConfig;
        } finally {
	        if (Objects.nonNull(rs)) {
		        rs.close();
	        }
	        if (Objects.nonNull(stat)) {
		        stat.close();
	        }
	        if (Objects.nonNull(conn)) {
		        conn.close();
	        }
        }
    }

	/**
	 * 导入所有代码生成配置列表
	 * by itcrazy0717
	 *
	 * @return
	 * @throws Exception
	 */
	public static List<MybatisCodeGenerateConfig> loadCodeGenerateConfigList() throws Exception {
        Connection conn = null;
        Statement stat = null;
        ResultSet rs = null;
        try {
            conn = DataBaseUtil.getSqLiteConnection();
            stat = conn.createStatement();
            String sql = "SELECT * FROM code_generate_config";
            LOGGER.info("sql: {}", sql);
            rs = stat.executeQuery(sql);
            List<MybatisCodeGenerateConfig> configs = new ArrayList<>();
            while (rs.next()) {
                String value = rs.getString("value");
                configs.add(JSON.parseObject(value, MybatisCodeGenerateConfig.class));
            }
            return configs;
        } finally {
	        if (Objects.nonNull(rs)) {
		        rs.close();
	        }
	        if (Objects.nonNull(stat)) {
		        stat.close();
	        }
	        if (Objects.nonNull(conn)) {
		        conn.close();
	        }
        }
    }

	/**
	 * 根据名称删除生成代码配置
	 * by itcrazy0717
	 *
	 * @param name
	 * @throws Exception
	 */
	public static void deleteCodeGenerateConfigByName(String name) throws Exception {
        Connection conn = null;
        Statement stat = null;
        try {
            conn = DataBaseUtil.getSqLiteConnection();
            stat = conn.createStatement();
            String deleteSql = String.format("DELETE FROM code_generate_config where name='%s'", name);
            LOGGER.info("sql: {}", deleteSql);
            stat.executeUpdate(deleteSql);
        } finally {
	        if (Objects.nonNull(stat)) {
		        stat.close();
	        }
	        if (Objects.nonNull(conn)) {
		        conn.close();
	        }
        }
    }

	/**
	 * 根据数据库类型获取驱动jar路径
	 * by itcrazy0717
	 *
	 * @param dataBaseType
	 * @return
	 */
	public static String getDataBaseDriverJarPath(String dataBaseType) {
		DataBaseTypeEnum dataType = DataBaseTypeEnum.valueOf(dataBaseType);
		try {
			File file = getDataBaseDriverClassJarFile(dataType);
			return file.getCanonicalPath();
		} catch (Exception e) {
			throw new RuntimeException("找不到驱动文件，请联系开发者!");
		}
	}

	/**
	 * 获取数据库驱动jar路径集合
	 * by itcrazy0717
	 *
	 * @return
	 */
	public static List<String> getAllDataBaseDriverJarPath() {
		Set<String> jarFilePathSets = new HashSet<>();
		try {
			File file = getDataBaseDriverClassJarFile(null);
			LOGGER.info("driver jar path:{}", file.getCanonicalPath());
			File[] jarFiles = file.listFiles();
			LOGGER.info("driver jar file:{}", Arrays.toString(jarFiles));
			if (Objects.nonNull(jarFiles)) {
				// 对驱动文件进行过滤
				// 获取驱动文件名称
				Set<String> driverClassNameSets = Arrays.stream(DataBaseTypeEnum.values())
														.map(DataBaseTypeEnum::getDriverJar)
														.collect(Collectors.toSet());
				Stream.of(jarFiles)
					  .filter(Objects::nonNull)
					  .filter(e -> e.isFile() && e.getAbsolutePath().endsWith(SqliteConstants.DATABASE_DRIVER_JAR_SUFFIX))
					  .filter(e -> driverClassNameSets.contains(e.getName()))
					  .forEach(e -> jarFilePathSets.add(e.getAbsolutePath()));
			}
		} catch (Exception e) {
			throw new RuntimeException("找不到驱动文件，请联系开发者");
		}
		return new ArrayList<>(jarFilePathSets);
	}

	/**
	 * 获取数据库驱动文件对象
	 * by itcrazy0717
	 *
	 * @param dataType
	 * @return
	 */
	private static File getDataBaseDriverClassJarFile(DataBaseTypeEnum dataType) {
		// 用sqlite3.db做数据文件基础，主要是为了生成jfx文件时能准确找到驱动文件
		URL url = Thread.currentThread().getContextClassLoader().getResource("sqlite3.db");
		String path;
		if (Objects.isNull(url)) {
			throw new RuntimeException("本地配置基础数据文件不存在");
		}
		// 生成jfx app时，路径中会存在.jar文件，因此需重新设置路径
		if (url.getPath().contains(SqliteConstants.DATABASE_DRIVER_JAR_SUFFIX)) {
			path = SqliteConstants.DATABASE_DRIVER_JAR_PATH_SUFFIX;
		} else {
			path = SqliteConstants.DATABASE_DRIVER_JAR_PATH;
		}
		if (Objects.nonNull(dataType) && StringUtils.isNotBlank(dataType.getDriverJar())) {
			path = path + dataType.getDriverJar();
		}
        File file = new File(path);
		// 未指定数据类型的时候做驱动文件兜底处理
		if (Objects.isNull(dataType)) {
			File[] files = file.listFiles();
			// 驱动文件兜底，解决部分jdk找不到文件的情况
			if (Objects.isNull(files) || files.length == 0) {
				String libPath = url.getPath().replace("sqlite3.db", "lib");
				return new File(libPath);
			}
		}
		return new File(path);
	}

}
