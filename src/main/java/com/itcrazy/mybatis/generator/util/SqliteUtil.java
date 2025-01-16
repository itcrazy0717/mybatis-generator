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
import com.itcrazy.mybatis.generator.model.MybatisGeneratorTemplate;

/**
 * @author: itcrazy0717
 * @version: $ SqliteUtil.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class SqliteUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqliteUtil.class);

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
            createSqliteDB(cofigSqliteFile);
        }
    }

    /**
     * 生成sqlite
     * by itcrazy0717
     *
     * @param file
     * @throws IOException
     */
    public static void createSqliteDB(File file) throws IOException {
        InputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = Thread.currentThread().getContextClassLoader().getResourceAsStream("sqlite3.db");
            if (Objects.isNull(fis)) {
                throw new RuntimeException("默认配置数据库不存在");
            }
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int byteRead;
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
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DataBaseUtil.getSqLiteConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM database_connection_config");
            List<DatabaseConnectionConfig> dbConnectionConfigList = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String value = resultSet.getString("value");
                DatabaseConnectionConfig dbConnectionConfig = JSON.parseObject(value, DatabaseConnectionConfig.class);
                dbConnectionConfig.setId(id);
                dbConnectionConfigList.add(dbConnectionConfig);
            }
            return dbConnectionConfigList;
        } finally {
            if (Objects.nonNull(resultSet)) {
                resultSet.close();
            }
            if (Objects.nonNull(statement)) {
                statement.close();
            }
            if (Objects.nonNull(connection)) {
                connection.close();
            }
        }
    }

    /**
     * 保存数据库连接配置
     * by itcrazy0717
     *
     * @param update
     * @param primaryKey
     * @param dbConnectConfig
     * @throws Exception
     */
    public static void saveDatabaseConnectionConfig(DatabaseConnectionConfig dbConnectConfig, Integer primaryKey, boolean update) throws Exception {
        String connectionName = dbConnectConfig.getName();
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DataBaseUtil.getSqLiteConnection();
            statement = connection.createStatement();
            if (!update) {
                ResultSet resultSet = statement.executeQuery("SELECT * from database_connection_config where name = '" + connectionName + "'");
                if (resultSet.next()) {
                    throw new RuntimeException("已存在相同名称的配置");
                }
            }
            String dbConnectConfigJsonValue = JSON.toJSONString(dbConnectConfig);
            String executeSql;
            if (update) {
                executeSql = String.format("UPDATE database_connection_config SET name = '%s', value = '%s' where id = %d", connectionName, dbConnectConfigJsonValue, primaryKey);
            } else {
                executeSql = String.format("INSERT INTO database_connection_config (name, value) values('%s', '%s')", connectionName, dbConnectConfigJsonValue);
            }
            LOGGER.info("save_database_connection_config_execute_sql:{}", executeSql);
            statement.executeUpdate(executeSql);
        } finally {
            if (Objects.nonNull(statement)) {
                statement.close();
            }
            if (Objects.nonNull(connection)) {
                connection.close();
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
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DataBaseUtil.getSqLiteConnection();
            statement = connection.createStatement();
            String executeSql = String.format("delete from database_connection_config where id=%d", databaseConfig.getId());
            LOGGER.info("delete_database_connection_config_execute_sql:{}", executeSql);
            statement.executeUpdate(executeSql);
        } finally {
            if (Objects.nonNull(statement)) {
                statement.close();
            }
            if (Objects.nonNull(connection)) {
                connection.close();
            }
        }
    }

    /**
     * 判断代码模板名称是否存在
     * by itcrazy0717
     *
     * @param templateName
     * @return true-存在，false-不存在
     * @throws Exception
     */
    public static boolean existGeneratorTemplate(String templateName) throws Exception {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DataBaseUtil.getSqLiteConnection();
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT count(*) from code_generator_template where name = '" + templateName + "'");
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            return false;
        } finally {
            if (Objects.nonNull(statement)) {
                statement.close();
            }
            if (Objects.nonNull(connection)) {
                connection.close();
            }
        }
    }

    /**
     * 保存生成器模板
     * by itcrazy0717
     *
     * @param generatorTemplate
     * @throws Exception
     */
    public static void saveGeneratorTemplate(MybatisGeneratorTemplate generatorTemplate) throws Exception {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DataBaseUtil.getSqLiteConnection();
            statement = connection.createStatement();
            String templateName = generatorTemplate.getName();
            generatorTemplate.setName(null);
            String templateJsonValue = JSON.toJSONString(generatorTemplate);
            String executeSql = String.format("INSERT INTO code_generator_template values('%s', '%s')", templateName, templateJsonValue);
            LOGGER.info("save_generator_template_execute_sql:{}", executeSql);
            statement.executeUpdate(executeSql);
        } finally {
            if (Objects.nonNull(statement)) {
                statement.close();
            }
            if (Objects.nonNull(connection)) {
                connection.close();
            }
        }
    }

    /**
     * 根据名称导入代码生成配置
     * by itcrazy0717
     *
     * @param templateName
     * @return
     * @throws Exception
     */
    public static MybatisGeneratorTemplate loadGeneratorTemplateByName(String templateName) throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DataBaseUtil.getSqLiteConnection();
            statement = connection.createStatement();
            String executeSql = String.format("SELECT * FROM code_generator_template where name='%s'", templateName);
            LOGGER.info("load_generator_template_by_name_sql:{}", executeSql);
            resultSet = statement.executeQuery(executeSql);
            MybatisGeneratorTemplate generatorConfig = null;
            if (resultSet.next()) {
                String value = resultSet.getString("value");
                generatorConfig = JSON.parseObject(value, MybatisGeneratorTemplate.class);
            }
            return generatorConfig;
        } finally {
            if (Objects.nonNull(resultSet)) {
                resultSet.close();
            }
            if (Objects.nonNull(statement)) {
                statement.close();
            }
            if (Objects.nonNull(connection)) {
                connection.close();
            }
        }
    }

    /**
     * 导入所有代码生成模板列表
     * by itcrazy0717
     *
     * @return
     * @throws Exception
     */
    public static List<MybatisGeneratorTemplate> loadGeneratorTemplateList() throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DataBaseUtil.getSqLiteConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM code_generator_template");
            List<MybatisGeneratorTemplate> templateList = new ArrayList<>();
            while (resultSet.next()) {
                String templateName = resultSet.getString("name");
                String templateContent = resultSet.getString("value");
                MybatisGeneratorTemplate template = JSON.parseObject(templateContent, MybatisGeneratorTemplate.class);
                template.setName(templateName);
                templateList.add(template);
            }
            return templateList;
        } finally {
            if (Objects.nonNull(resultSet)) {
                resultSet.close();
            }
            if (Objects.nonNull(statement)) {
                statement.close();
            }
            if (Objects.nonNull(connection)) {
                connection.close();
            }
        }
    }

    /**
     * 根据名称删除生成代码配置
     * by itcrazy0717
     *
     * @param templateName
     * @throws Exception
     */
    public static void deleteGeneratorTemplateByName(String templateName) throws Exception {
        Connection conn = null;
        Statement stat = null;
        try {
            conn = DataBaseUtil.getSqLiteConnection();
            stat = conn.createStatement();
            String executeSql = String.format("DELETE FROM code_generator_template where name='%s'", templateName);
            LOGGER.info("delet_egenerator_template_by_name_execute_sql:{}", executeSql);
            stat.executeUpdate(executeSql);
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
     * 更新模板名称
     * by itcrazy0717
     *
     * @param newTemplateName
     * @param originalTemplateName
     * @throws Exception
     */
    public static void updateGeneratorTemplateName(String newTemplateName, String originalTemplateName) throws Exception {
        Connection conn = null;
        Statement stat = null;
        try {
            conn = DataBaseUtil.getSqLiteConnection();
            stat = conn.createStatement();
            String executeSql = String.format("UPDATE code_generator_template SET name='%s' where name='%s'", newTemplateName, originalTemplateName);
            LOGGER.info("update_generator_template_name_execute_sql:{}", executeSql);
            stat.executeUpdate(executeSql);
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
            LOGGER.error("get_driver_path_error", e);
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
            LOGGER.error("get_driver_path_error", e);
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
