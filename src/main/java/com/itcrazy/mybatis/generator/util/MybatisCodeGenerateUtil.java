package com.itcrazy.mybatis.generator.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.CommentGeneratorConfiguration;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.config.IgnoredColumn;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.JavaTypeResolverConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.plugins.ToStringPlugin;
import org.mybatis.generator.plugins.UnmergeableXmlMappersPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.enums.DataBaseTypeEnum;
import com.itcrazy.mybatis.generator.model.DatabaseConnectionConfig;
import com.itcrazy.mybatis.generator.model.MybatisGeneratorTemplate;
import com.itcrazy.mybatis.generator.plugins.AddMethodCommentPlugin;
import com.itcrazy.mybatis.generator.plugins.BatchInsertPlugin;
import com.itcrazy.mybatis.generator.plugins.CustomCommentGenerator;
import com.itcrazy.mybatis.generator.plugins.PagePlugin;
import com.itcrazy.mybatis.generator.plugins.ReplaceExampleContentPlugin;
import com.itcrazy.mybatis.generator.plugins.SortPlugin;
import com.itcrazy.mybatis.generator.typeresolver.TinyIntToBooleanTypeResolver;
import com.itcrazy.mybatis.generator.typeresolver.TinyIntToIntegerResolver;


/**
 * @author: itcrazy0717
 * @version: $ MybatisGeneratorBridge.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class MybatisCodeGenerateUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisCodeGenerateUtil.class);

    /**
     * 代码生成配置
     */
    private static MybatisGeneratorTemplate generateConfig;

    /**
     * 数据库配置
     */
    private static DatabaseConnectionConfig selectedDatabaseConfig;

    /**
     * 回调处理
     */
    private static ProgressCallback progressCallback;

    /**
     * 忽略的列
     */
    private static List<IgnoredColumn> ignoredColumns;

    /**
     * 覆盖的列
     */
    private static List<ColumnOverride> columnOverrides;

    /**
     * param对象包路径
     */
    private static String paramPackage;

    /**
     * 属性值名称
     */
    private final static String PROPERTY_NAME = "type";

    /**
     * 生成代码
     * by itcrazy0717
     *
     * @throws Exception
     */
    public static void generateCode() throws Exception {
        Configuration configuration = new Configuration();
        Context context = new Context(ModelType.CONDITIONAL);
        configuration.addContext(context);
        context.addProperty("javaFileEncoding", "UTF-8");
        String dataBaseType = selectedDatabaseConfig.getDataBaseType();
        String driverJarPath = SqliteUtil.getDataBaseDriverJarPath(dataBaseType);
        LOGGER.info("driver_jar_path: {}", driverJarPath);
        configuration.addClasspathEntry(driverJarPath);
        // Table configuration
        TableConfiguration tableConfig = new TableConfiguration(context);
        tableConfig.setTableName(generateConfig.getTableName());
        tableConfig.setDomainObjectName(generateConfig.getDomainObjectName());
        // 设置catalog，避免跨库扫表问题
        tableConfig.setCatalog(selectedDatabaseConfig.getSchemaName());

        // 针对postgresql单独配置
        if (StringUtils.equals(DataBaseTypeEnum.PostgreSQL.getDriverClass(), DataBaseTypeEnum.valueOf(dataBaseType).getDriverClass())) {
            tableConfig.setDelimitIdentifiers(true);
        }

        // 添加GeneratedKey主键生成，用于insert的时候返回主键
        // 以上只在MySql下进行过测试
        if (BooleanUtils.isTrue(generateConfig.getInsertReturnPrimaryKey())
            && StringUtils.isNotBlank(generateConfig.getPrimaryKey())) {
            String dbType = dataBaseType;
            if (StringUtils.equals(DataBaseTypeEnum.MySQL.name(), dbType)) {
                dbType = "JDBC";
                // dbType为JDBC，且配置中开启useGeneratedKeys时，Mybatis会使用Jdbc3KeyGenerator,
                // 使用该KeyGenerator的好处就是直接在一次INSERT语句内，通过resultSet获取得到生成的主键值，
                // 并很好的支持设置了读写分离代理的数据库
                // 例如阿里云RDS + 读写分离代理
                // 无需指定主库
                // 当使用SelectKey时，Mybatis会使用SelectKeyGenerator，INSERT之后，多发送一次查询语句，获得主键值
                // 在上述读写分离被代理的情况下，会得不到正确的主键
            }
            tableConfig.setGeneratedKey(new GeneratedKey(generateConfig.getPrimaryKey(), dbType, true, null));
        }

        if (StringUtils.isNotBlank(generateConfig.getMapperName())) {
            tableConfig.setMapperName(generateConfig.getMapperName());
        }
        // add ignore columns
        if (CollectionUtils.isNotEmpty(ignoredColumns)) {
            ignoredColumns.forEach(tableConfig::addIgnoredColumn);
        }
        if (CollectionUtils.isNotEmpty(columnOverrides)) {
            columnOverrides.forEach(tableConfig::addColumnOverride);
        }
        JDBCConnectionConfiguration jdbcConfig = new JDBCConnectionConfiguration();
        jdbcConfig.setDriverClass(DataBaseTypeEnum.valueOf(dataBaseType).getDriverClass());
        jdbcConfig.setConnectionURL(DataBaseUtil.buildConnectionUrlWithSchema(selectedDatabaseConfig));
        jdbcConfig.setUserId(selectedDatabaseConfig.getUserName());
        jdbcConfig.setPassword(selectedDatabaseConfig.getPassword());

        // 实体类路径配置
        JavaModelGeneratorConfiguration modelConfig = new JavaModelGeneratorConfiguration();
        modelConfig.setTargetPackage(generateConfig.getModelPackage());
        modelConfig.setTargetProject(generateConfig.getProjectFolder() + "/" + generateConfig.getModelAndDaoInterfacePackageTargetFolder());

        // DAO接口文件路径配置
        JavaClientGeneratorConfiguration daoConfig = new JavaClientGeneratorConfiguration();
        daoConfig.setConfigurationType("XMLMAPPER");
        daoConfig.setTargetPackage(generateConfig.getDaoPackage());
        daoConfig.setTargetProject(generateConfig.getProjectFolder() + "/" + generateConfig.getModelAndDaoInterfacePackageTargetFolder());

        // Mapper文件路径配置
        SqlMapGeneratorConfiguration mapperConfig = new SqlMapGeneratorConfiguration();
        mapperConfig.setTargetPackage(generateConfig.getMapperXMLPackage());
        mapperConfig.setTargetProject(generateConfig.getProjectFolder() + "/" + generateConfig.getMapperXMLTargetFolder());

        // 设置param对象包路径，单独命名为xxx.parm,便于管理
        paramPackage = generateConfig.getParamModelPackage();
        if (StringUtils.isBlank(paramPackage)) {
            // 未自定义param包路径，则默认使用实体包路径，兜底
            String modelPackage = modelConfig.getTargetPackage();
            paramPackage = modelPackage.substring(0, modelPackage.lastIndexOf(".")) + ".param";
        }

        context.setId("myid");
        context.addTableConfiguration(tableConfig);
        context.setJdbcConnectionConfiguration(jdbcConfig);
        context.setJavaModelGeneratorConfiguration(modelConfig);
        context.setSqlMapGeneratorConfiguration(mapperConfig);
        context.setJavaClientGeneratorConfiguration(daoConfig);
        // 注释
        CommentGeneratorConfiguration commentConfig = new CommentGeneratorConfiguration();
        commentConfig.setConfigurationType(CustomCommentGenerator.class.getName());
        commentConfig.addProperty("columnRemarks", "true");
        // commentConfig.addProperty("annotations", "true");

        context.setCommentGeneratorConfiguration(commentConfig);

        // 增加自定义插件
        addCustomPlugins(context);

        context.setTargetRuntime("MyBatis3");

        List<String> warnings = new ArrayList<>();
        Set<String> fullyqualifiedTables = new HashSet<>();
        Set<String> contexts = new HashSet<>();
        ShellCallback shellCallback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, shellCallback, warnings);
        myBatisGenerator.generate(progressCallback, contexts, fullyqualifiedTables);
        // 修复xml文件内容表名双点问题
        FixTableNameUtil.fixTableName(buildXmlFile(), selectedDatabaseConfig.getSchemaName(), generateConfig.getTableName());
    }

    /**
     * 增加自定义插件
     * by itcrazy0717
     *
     * @param context
     */
    private static void addCustomPlugins(Context context) {
        JavaTypeResolverConfiguration typeResolverConfiguration = new JavaTypeResolverConfiguration();
        // 根据选项设置TINYINT->Boolean类型
        if (BooleanUtils.isTrue(generateConfig.getTinyInt2Boolean())) {
            typeResolverConfiguration.setConfigurationType(TinyIntToBooleanTypeResolver.class.getName());
        } else {
            // 不勾选时，转换成Integer类型
            typeResolverConfiguration.setConfigurationType(TinyIntToIntegerResolver.class.getName());
        }
        context.setJavaTypeResolverConfiguration(typeResolverConfiguration);

        /**
         // 序列化插件
         PluginConfiguration serializablePlugin = new PluginConfiguration();
         serializablePlugin.addProperty(PROPERTY_NAME, SerializablePlugin.class.getName());
         serializablePlugin.setConfigurationType(SerializablePlugin.class.getName());
         context.addPluginConfiguration(serializablePlugin);
         */

        // toString插件
        PluginConfiguration toStringPlugin = new PluginConfiguration();
        toStringPlugin.addProperty(PROPERTY_NAME, ToStringPlugin.class.getName());
        toStringPlugin.setConfigurationType(ToStringPlugin.class.getName());
        context.addPluginConfiguration(toStringPlugin);
        // 分页插件
        if (DataBaseTypeEnum.MySQL.name().equals(selectedDatabaseConfig.getDataBaseType()) || DataBaseTypeEnum.PostgreSQL.name().equals(selectedDatabaseConfig.getDataBaseType())) {
            PluginConfiguration pagePlugin = new PluginConfiguration();
            pagePlugin.addProperty(PROPERTY_NAME, PagePlugin.class.getName());
            pagePlugin.setConfigurationType(PagePlugin.class.getName());
            context.addPluginConfiguration(pagePlugin);
        }
        // 覆写xml文件插件
        PluginConfiguration overWiriteXmlPlugin = new PluginConfiguration();
        overWiriteXmlPlugin.addProperty(PROPERTY_NAME, UnmergeableXmlMappersPlugin.class.getName());
        overWiriteXmlPlugin.setConfigurationType(UnmergeableXmlMappersPlugin.class.getName());
        context.addPluginConfiguration(overWiriteXmlPlugin);
        // 替换example内容插件
        PluginConfiguration replaeceExampleContentPlugin = new PluginConfiguration();
        replaeceExampleContentPlugin.addProperty(PROPERTY_NAME, ReplaceExampleContentPlugin.class.getName());
        replaeceExampleContentPlugin.setConfigurationType(ReplaceExampleContentPlugin.class.getName());
        replaeceExampleContentPlugin.addProperty("searchString", "Example");
        replaeceExampleContentPlugin.addProperty("replaceString", "Param");
        replaeceExampleContentPlugin.addProperty("simpleMethod", "True");
        replaeceExampleContentPlugin.addProperty("paramPackage", paramPackage);
        context.addPluginConfiguration(replaeceExampleContentPlugin);
        // 方法注释插件
        PluginConfiguration addMethodComentPlugin = new PluginConfiguration();
        addMethodComentPlugin.addProperty(PROPERTY_NAME, AddMethodCommentPlugin.class.getName());
        addMethodComentPlugin.setConfigurationType(AddMethodCommentPlugin.class.getName());
        context.addPluginConfiguration(addMethodComentPlugin);
        // 批量插入插件
        PluginConfiguration batchInsertPlugin = new PluginConfiguration();
        batchInsertPlugin.addProperty(PROPERTY_NAME, BatchInsertPlugin.class.getName());
        batchInsertPlugin.setConfigurationType(BatchInsertPlugin.class.getName());
        context.addPluginConfiguration(batchInsertPlugin);
        // 排序插件
        PluginConfiguration sortPlugin = new PluginConfiguration();
        sortPlugin.addProperty(PROPERTY_NAME, SortPlugin.class.getName());
        sortPlugin.setConfigurationType(SortPlugin.class.getName());
        context.addPluginConfiguration(sortPlugin);
    }

    /**
     * 导入配置
     * by itcrazy0717
     *
     * @param generateConfig
     * @param databaseConfig
     * @param progressCallback
     * @param ignoredColumns
     * @param columnOverrides
     */
    public static void loadConfig(MybatisGeneratorTemplate generateConfig, DatabaseConnectionConfig databaseConfig,
                                  ProgressCallback progressCallback, List<IgnoredColumn> ignoredColumns,
                                  List<ColumnOverride> columnOverrides) {
        MybatisCodeGenerateUtil.generateConfig = generateConfig;
        MybatisCodeGenerateUtil.selectedDatabaseConfig = databaseConfig;
        MybatisCodeGenerateUtil.progressCallback = progressCallback;
        MybatisCodeGenerateUtil.ignoredColumns = ignoredColumns;
        MybatisCodeGenerateUtil.columnOverrides = columnOverrides;
    }

    /**
     * 构建xml文件
     * by itcrazy0717
     *
     * @return
     */
    private static File buildXmlFile() {
        // 构建xml目录路径
        String dirPath = generateConfig.getProjectFolder() + "/" + generateConfig.getMapperXMLTargetFolder() + "/" + generateConfig.getMapperXMLPackage().replace(".", "/");
        File directory = new File(dirPath);
        String fileName = generateConfig.getMapperName() + ".xml";
        return new File(directory, fileName);
    }
}
