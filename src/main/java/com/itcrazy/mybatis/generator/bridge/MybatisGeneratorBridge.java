package com.itcrazy.mybatis.generator.bridge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.model.DatabaseConfig;
import com.itcrazy.mybatis.generator.model.DbType;
import com.itcrazy.mybatis.generator.model.GeneratorConfig;
import com.itcrazy.mybatis.generator.plugins.CustomCommentGenerator;
import com.itcrazy.mybatis.generator.typeresolver.TinyIntTypeResolver;
import com.itcrazy.mybatis.generator.util.ConfigHelper;
import com.itcrazy.mybatis.generator.util.DbUtil;


/**
 * @author: dengxin.chen
 * @version: $ MybatisGeneratorBridge.java,v0.1 2024-09-30 17:15 dengxin.chen Exp $
 * @description:
 */
public class MybatisGeneratorBridge {

    private static final Logger _LOG = LoggerFactory.getLogger(MybatisGeneratorBridge.class);

    private GeneratorConfig generatorConfig;

    private DatabaseConfig selectedDatabaseConfig;

    private ProgressCallback progressCallback;

    private List<IgnoredColumn> ignoredColumns;

    private List<ColumnOverride> columnOverrides;

    public MybatisGeneratorBridge() {
    }

    public void setGeneratorConfig(GeneratorConfig generatorConfig) {
        this.generatorConfig = generatorConfig;
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig) {
        this.selectedDatabaseConfig = databaseConfig;
    }

    public void generate() throws Exception {
        Configuration configuration = new Configuration();
        Context context = new Context(ModelType.CONDITIONAL);
        configuration.addContext(context);
        context.addProperty("javaFileEncoding", "UTF-8");
        String connectorLibPath = ConfigHelper.findConnectorLibPath(selectedDatabaseConfig.getDbType());
        _LOG.info("connectorLibPath: {}", connectorLibPath);
        configuration.addClasspathEntry(connectorLibPath);
        // Table configuration
        TableConfiguration tableConfig = new TableConfiguration(context);
        tableConfig.setTableName(generatorConfig.getTableName());
        tableConfig.setDomainObjectName(generatorConfig.getDomainObjectName());

        // 针对 postgresql 单独配置
        if (DbType.valueOf(selectedDatabaseConfig.getDbType()).getDriverClass() == "org.postgresql.Driver") {
            tableConfig.setDelimitIdentifiers(true);
        }

        // 添加GeneratedKey主键生成
        if (StringUtils.isNoneEmpty(generatorConfig.getGenerateKeys())) {
            tableConfig.setGeneratedKey(new GeneratedKey(generatorConfig.getGenerateKeys(), selectedDatabaseConfig.getDbType(), true, null));
        }

        if (generatorConfig.getMapperName() != null) {
            tableConfig.setMapperName(generatorConfig.getMapperName());
        }
        // add ignore columns
        if (ignoredColumns != null) {
            ignoredColumns.forEach(tableConfig::addIgnoredColumn);
        }
        if (columnOverrides != null) {
            columnOverrides.forEach(tableConfig::addColumnOverride);
        }
        if (generatorConfig.isUseActualColumnNames()) {
            tableConfig.addProperty("useActualColumnNames", "true");
        }
        JDBCConnectionConfiguration jdbcConfig = new JDBCConnectionConfiguration();
        jdbcConfig.setDriverClass(DbType.valueOf(selectedDatabaseConfig.getDbType()).getDriverClass());
        jdbcConfig.setConnectionURL(DbUtil.getConnectionUrlWithSchema(selectedDatabaseConfig));
        jdbcConfig.setUserId(selectedDatabaseConfig.getUsername());
        jdbcConfig.setPassword(selectedDatabaseConfig.getPassword());
        // java model
        JavaModelGeneratorConfiguration modelConfig = new JavaModelGeneratorConfiguration();
        modelConfig.setTargetPackage(generatorConfig.getModelPackage());
        modelConfig.setTargetProject(generatorConfig.getProjectFolder() + "/" + generatorConfig.getModelPackageTargetFolder());
        // Mapper configuration
        SqlMapGeneratorConfiguration mapperConfig = new SqlMapGeneratorConfiguration();
        mapperConfig.setTargetPackage(generatorConfig.getMappingXMLPackage());
        mapperConfig.setTargetProject(generatorConfig.getProjectFolder() + "/" + generatorConfig.getMappingXMLTargetFolder());
        // DAO
        JavaClientGeneratorConfiguration daoConfig = new JavaClientGeneratorConfiguration();
        daoConfig.setConfigurationType("XMLMAPPER");
        daoConfig.setTargetPackage(generatorConfig.getDaoPackage());
        daoConfig.setTargetProject(generatorConfig.getProjectFolder() + "/" + generatorConfig.getDaoTargetFolder());

        context.setId("myid");
        context.addTableConfiguration(tableConfig);
        context.setJdbcConnectionConfiguration(jdbcConfig);
        context.setJdbcConnectionConfiguration(jdbcConfig);
        context.setJavaModelGeneratorConfiguration(modelConfig);
        context.setSqlMapGeneratorConfiguration(mapperConfig);
        context.setJavaClientGeneratorConfiguration(daoConfig);
        // Comment
        CommentGeneratorConfiguration commentConfig = new CommentGeneratorConfiguration();
        commentConfig.setConfigurationType(CustomCommentGenerator.class.getName());
        if (generatorConfig.isComment()) {
            commentConfig.addProperty("columnRemarks", "true");
        }
        if (generatorConfig.isAnnotation()) {
            commentConfig.addProperty("annotations", "true");
        }
        context.setCommentGeneratorConfiguration(commentConfig);

        // 默认设置TINYINT->Boolean类型
        JavaTypeResolverConfiguration typeResolverConfiguration = new JavaTypeResolverConfiguration();
        typeResolverConfiguration.setConfigurationType(TinyIntTypeResolver.class.getName());
        context.setJavaTypeResolverConfiguration(typeResolverConfiguration);
        // 序列化插件
        PluginConfiguration serializablePlugin = new PluginConfiguration();
        serializablePlugin.addProperty("type", "org.mybatis.generator.plugins.SerializablePlugin");
        serializablePlugin.setConfigurationType("org.mybatis.generator.plugins.SerializablePlugin");
        context.addPluginConfiguration(serializablePlugin);

        // toString插件
        PluginConfiguration toStringPlugin = new PluginConfiguration();
        toStringPlugin.addProperty("type", "org.mybatis.generator.plugins.ToStringPlugin");
        toStringPlugin.setConfigurationType("org.mybatis.generator.plugins.ToStringPlugin");
        context.addPluginConfiguration(toStringPlugin);

        // 分页插件
        if (DbType.MySQL.name().equals(selectedDatabaseConfig.getDbType()) || DbType.PostgreSQL.name().equals(selectedDatabaseConfig.getDbType())) {
            PluginConfiguration pluginConfiguration = new PluginConfiguration();
            pluginConfiguration.addProperty("", "com.itcrazy.mybatis.generator.plugins.MySQLLimitPlugin");
            pluginConfiguration.setConfigurationType("com.itcrazy.mybatis.generator.plugins.MySQLLimitPlugin");
            context.addPluginConfiguration(pluginConfiguration);
        }
        // 覆写xml文件插件
        PluginConfiguration overWiriteXmlPlugin = new PluginConfiguration();
        overWiriteXmlPlugin.addProperty("type", "org.mybatis.generator.plugins.UnmergeableXmlMappersPlugin");
        overWiriteXmlPlugin.setConfigurationType("org.mybatis.generator.plugins.UnmergeableXmlMappersPlugin");
        context.addPluginConfiguration(overWiriteXmlPlugin);
        // 替换example内容插件
        PluginConfiguration replaeceExampleContentPlugin = new PluginConfiguration();
        replaeceExampleContentPlugin.addProperty("type", "com.itcrazy.mybatis.generator.plugins.ReplaceExampleContentPlugin");
        replaeceExampleContentPlugin.setConfigurationType("com.itcrazy.mybatis.generator.plugins.ReplaceExampleContentPlugin");
        replaeceExampleContentPlugin.addProperty("searchString", "Example");
        replaeceExampleContentPlugin.addProperty("replaceString", "Param");
        replaeceExampleContentPlugin.addProperty("simpleMethod", "True");
        context.addPluginConfiguration(replaeceExampleContentPlugin);
        // 方法注释插件
        PluginConfiguration addMethodComentPlugin = new PluginConfiguration();
        addMethodComentPlugin.addProperty("type", "com.itcrazy.mybatis.generator.plugins.AddMethodCommentPlugin");
        addMethodComentPlugin.setConfigurationType("com.itcrazy.mybatis.generator.plugins.AddMethodCommentPlugin");
        context.addPluginConfiguration(addMethodComentPlugin);
        // 批量插入插件
        PluginConfiguration batchInsertPlugin = new PluginConfiguration();
        batchInsertPlugin.addProperty("type", "com.itcrazy.mybatis.generator.plugins.BatchInsertPlugin");
        batchInsertPlugin.setConfigurationType("com.itcrazy.mybatis.generator.plugins.BatchInsertPlugin");
        context.addPluginConfiguration(batchInsertPlugin);

        context.setTargetRuntime("MyBatis3");

        List<String> warnings = new ArrayList<>();
        Set<String> fullyqualifiedTables = new HashSet<>();
        Set<String> contexts = new HashSet<>();
        ShellCallback shellCallback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, shellCallback, warnings);
        myBatisGenerator.generate(progressCallback, contexts, fullyqualifiedTables);
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public void setIgnoredColumns(List<IgnoredColumn> ignoredColumns) {
        this.ignoredColumns = ignoredColumns;
    }

    public void setColumnOverrides(List<ColumnOverride> columnOverrides) {
        this.columnOverrides = columnOverrides;
    }
}
