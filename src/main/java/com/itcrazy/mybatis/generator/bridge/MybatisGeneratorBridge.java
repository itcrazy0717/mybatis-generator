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

import com.itcrazy.mybatis.generator.dto.DataBaseType;
import com.itcrazy.mybatis.generator.dto.DatabaseConfig;
import com.itcrazy.mybatis.generator.dto.MybatisCodeGenerateConfig;
import com.itcrazy.mybatis.generator.plugins.CustomCommentGenerator;
import com.itcrazy.mybatis.generator.typeresolver.TinyIntTypeResolver;
import com.itcrazy.mybatis.generator.util.DataBaseUtil;
import com.itcrazy.mybatis.generator.util.MybatisCodeGenerateConfigUtil;


/**
 * @author: itcrazy0717
 * @version: $ MybatisGeneratorBridge.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class MybatisGeneratorBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisGeneratorBridge.class);

    private MybatisCodeGenerateConfig generateConfig;

    private DatabaseConfig selectedDatabaseConfig;

    private ProgressCallback progressCallback;

    private List<IgnoredColumn> ignoredColumns;

    private List<ColumnOverride> columnOverrides;

    public MybatisGeneratorBridge() {
    }

    public void generate() throws Exception {
        Configuration configuration = new Configuration();
        Context context = new Context(ModelType.CONDITIONAL);
        configuration.addContext(context);
        context.addProperty("javaFileEncoding", "UTF-8");
        String connectorLibPath = MybatisCodeGenerateConfigUtil.findConnectorLibPath(selectedDatabaseConfig.getDataBaseType());
        LOGGER.info("connectorLibPath: {}", connectorLibPath);
        configuration.addClasspathEntry(connectorLibPath);
        // Table configuration
        TableConfiguration tableConfig = new TableConfiguration(context);
        tableConfig.setTableName(generateConfig.getTableName());
        tableConfig.setDomainObjectName(generateConfig.getDomainObjectName());

        // 针对postgresql单独配置
        if (DataBaseType.valueOf(selectedDatabaseConfig.getDataBaseType()).getDriverClass() == "org.postgresql.Driver") {
            tableConfig.setDelimitIdentifiers(true);
        }

       /*
       // 添加GeneratedKey主键生成
        if (StringUtils.isNoneEmpty(generatorConfig.getGenerateKeys())) {
            tableConfig.setGeneratedKey(new GeneratedKey(generatorConfig.getGenerateKeys(), selectedDatabaseConfig.getDataBaseType(), true, null));
        }
        */

	    if (StringUtils.isNotBlank(generateConfig.getMapperName())) {
		    tableConfig.setMapperName(generateConfig.getMapperName());
	    }
        // add ignore columns
        if (ignoredColumns != null) {
            ignoredColumns.forEach(tableConfig::addIgnoredColumn);
        }
        if (columnOverrides != null) {
            columnOverrides.forEach(tableConfig::addColumnOverride);
        }
        JDBCConnectionConfiguration jdbcConfig = new JDBCConnectionConfiguration();
        jdbcConfig.setDriverClass(DataBaseType.valueOf(selectedDatabaseConfig.getDataBaseType()).getDriverClass());
        jdbcConfig.setConnectionURL(DataBaseUtil.getConnectionUrlWithSchema(selectedDatabaseConfig));
        jdbcConfig.setUserId(selectedDatabaseConfig.getUserName());
        jdbcConfig.setPassword(selectedDatabaseConfig.getPassword());
        // 实体类路径配置
        JavaModelGeneratorConfiguration modelConfig = new JavaModelGeneratorConfiguration();
        modelConfig.setTargetPackage(generateConfig.getModelPackage());
        modelConfig.setTargetProject(generateConfig.getProjectFolder() + "/" + generateConfig.getModelPackageTargetFolder());

	    // 设置param对象包路径，单独命名为xxx.parm,便于管理
	    String paramPackage = generateConfig.getParamModelPackage();
	    if (StringUtils.isBlank(paramPackage)) {
		    // 未自定义param包路径，则默认使用实体包路径，兜底
		    String modelPackage = modelConfig.getTargetPackage();
		    paramPackage = modelPackage.substring(0, modelPackage.lastIndexOf(".")) + ".param";
	    }
        // Mapper文件路径配置
        SqlMapGeneratorConfiguration mapperConfig = new SqlMapGeneratorConfiguration();
        mapperConfig.setTargetPackage(generateConfig.getMapperXMLPackage());
        mapperConfig.setTargetProject(generateConfig.getProjectFolder() + "/" + generateConfig.getMapperXMLTargetFolder());
        // DAO接口文件路径配置
        JavaClientGeneratorConfiguration daoConfig = new JavaClientGeneratorConfiguration();
        daoConfig.setConfigurationType("XMLMAPPER");
        daoConfig.setTargetPackage(generateConfig.getDaoPackage());
        daoConfig.setTargetProject(generateConfig.getProjectFolder() + "/" + generateConfig.getDaoTargetFolder());

        context.setId("myid");
        context.addTableConfiguration(tableConfig);
        context.setJdbcConnectionConfiguration(jdbcConfig);
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
	    addCustomPlugin(context, paramPackage);

	    context.setTargetRuntime("MyBatis3");

        List<String> warnings = new ArrayList<>();
        Set<String> fullyqualifiedTables = new HashSet<>();
        Set<String> contexts = new HashSet<>();
        ShellCallback shellCallback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, shellCallback, warnings);
        myBatisGenerator.generate(progressCallback, contexts, fullyqualifiedTables);
    }

	/**
	 * 增加自定义插件
	 * by itcrazy0717
	 *
	 * @param context
	 * @param paramPackage parm参数包路径
	 */
	private void addCustomPlugin(Context context, String paramPackage) {
		// 默认设置TINYINT->Boolean类型
		JavaTypeResolverConfiguration typeResolverConfiguration = new JavaTypeResolverConfiguration();
		typeResolverConfiguration.setConfigurationType(TinyIntTypeResolver.class.getName());
		context.setJavaTypeResolverConfiguration(typeResolverConfiguration);
		/**
		 // 序列化插件
		 PluginConfiguration serializablePlugin = new PluginConfiguration();
		 serializablePlugin.addProperty("type", "org.mybatis.generator.plugins.SerializablePlugin");
		 serializablePlugin.setConfigurationType("org.mybatis.generator.plugins.SerializablePlugin");
		 context.addPluginConfiguration(serializablePlugin);
		 */

		// toString插件
		PluginConfiguration toStringPlugin = new PluginConfiguration();
		toStringPlugin.addProperty("type", "org.mybatis.generator.plugins.ToStringPlugin");
		toStringPlugin.setConfigurationType("org.mybatis.generator.plugins.ToStringPlugin");
		context.addPluginConfiguration(toStringPlugin);
		// 分页插件
		if (DataBaseType.MySQL.name().equals(selectedDatabaseConfig.getDataBaseType()) || DataBaseType.PostgreSQL.name().equals(selectedDatabaseConfig.getDataBaseType())) {
			PluginConfiguration pluginConfiguration = new PluginConfiguration();
			pluginConfiguration.addProperty("", "com.itcrazy.mybatis.generator.plugins.PagePlugin");
			pluginConfiguration.setConfigurationType("com.itcrazy.mybatis.generator.plugins.PagePlugin");
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
		replaeceExampleContentPlugin.addProperty("paramPackage", paramPackage);
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
		// 排序插件
		PluginConfiguration sortPlugin = new PluginConfiguration();
		sortPlugin.addProperty("type", "com.itcrazy.mybatis.generator.plugins.SortPlugin");
		sortPlugin.setConfigurationType("com.itcrazy.mybatis.generator.plugins.SortPlugin");
		context.addPluginConfiguration(sortPlugin);
	}

	public void setGenerateConfig(MybatisCodeGenerateConfig generateConfig) {
		this.generateConfig = generateConfig;
	}

	public void setDatabaseConfig(DatabaseConfig databaseConfig) {
		this.selectedDatabaseConfig = databaseConfig;
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
