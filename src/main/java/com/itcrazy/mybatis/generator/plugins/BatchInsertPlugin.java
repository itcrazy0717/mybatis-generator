package com.itcrazy.mybatis.generator.plugins;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.config.MergeConstants;

import static javax.swing.UIManager.getString;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author: itcrazy0717
 * @version: $ BatchInsertPlugin.java,v0.1 2024-09-30 21:15 itcrazy0717 Exp $
 * @description:批量插入插件
 */
public class BatchInsertPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
                                   IntrospectedTable introspectedTable) {
        String configurationType = this.properties.getProperty("configurationType");
        if (StringUtils.isNotBlank(configurationType) && "ANNOTATEDMAPPER".equalsIgnoreCase(configurationType)) {
            return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
        }
        // 设置需要导入的类
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

        Method batchInsertMethod = new Method();

        batchInsertMethod.addJavaDocLine("/**");
        batchInsertMethod.addJavaDocLine(" * 批量插入");
        batchInsertMethod.addJavaDocLine(" * @param records");
        batchInsertMethod.addJavaDocLine(" * @return");
        batchInsertMethod.addJavaDocLine(" *");
        batchInsertMethod.addJavaDocLine(" * @mbg.generated");
        batchInsertMethod.addJavaDocLine(" */");
        // 1.设置方法可见性
        batchInsertMethod.setVisibility(JavaVisibility.PUBLIC);
        // 2.设置返回值类型
        // int型
        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getIntInstance();
        batchInsertMethod.setReturnType(returnType);
        // 3.设置方法名
        batchInsertMethod.setName("batchInsert");
        // 4.设置参数列表
        FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType paramListType;
        if (introspectedTable.getRules().generateBaseRecordClass()) {
            paramListType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        } else if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            paramListType = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
        } else {
            throw new RuntimeException(getString("RuntimeError.12"));
        }
        paramType.addTypeArgument(paramListType);

        batchInsertMethod.addParameter(new Parameter(paramType, "records"));

        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(batchInsertMethod);
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        // 获得要自增的列名
        String incrementField = null;
        String isGenerateKey = properties.getProperty("isGenerateKey");
        if (Boolean.parseBoolean(isGenerateKey)) {
            incrementField = properties.getProperty("generateKey");
        }

        StringBuilder dbcolumnsName = new StringBuilder();
        StringBuilder javaPropertyAndDbType = new StringBuilder();
        for (IntrospectedColumn introspectedColumn : columns) {
            String columnName = introspectedColumn.getActualColumnName();
            // 不是自增字段的才会出现在批量插入中
            if (!columnName.equalsIgnoreCase(incrementField)) {
                dbcolumnsName.append(columnName).append(",");
                javaPropertyAndDbType.append("#{item.").append(introspectedColumn.getJavaProperty()).append(",jdbcType=").append(introspectedColumn.getJdbcTypeName());
                if (stringHasValue(introspectedColumn.getTypeHandler())) {
                    javaPropertyAndDbType.append(",typeHandler=");
                    javaPropertyAndDbType.append(introspectedColumn.getTypeHandler());
                    javaPropertyAndDbType.append(",javaType=");
                    javaPropertyAndDbType.append(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedNameWithoutTypeParameters());
                }
                javaPropertyAndDbType.append("},");
            }
        }

        XmlElement insertBatchElement = new XmlElement("insert");
        insertBatchElement.addAttribute(new Attribute("id", "batchInsert"));
        insertBatchElement.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable
                    .getColumn(gk.getColumn());
            if (introspectedColumn != null) {
                if (gk.isJdbcStandard()) {
                    insertBatchElement.addAttribute(new Attribute(
                            "useGeneratedKeys", "true"));
                    insertBatchElement.addAttribute(new Attribute(
                            "keyProperty", introspectedColumn.getJavaProperty()));
                }
            }
        }

        insertBatchElement.addElement(new TextElement("<!--"));

        StringBuilder sb = new StringBuilder();
        sb.append("  WARNING - ");
        sb.append(MergeConstants.NEW_ELEMENT_TAG);
        insertBatchElement.addElement(new TextElement(sb.toString()));
        insertBatchElement
                .addElement(new TextElement(
                        "  This element is automatically generated by MyBatis Generator, do not modify."));

        String nowTimeString = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(new Date());
        sb.setLength(0);
        sb.append("  This element was generated on ");
        sb.append(nowTimeString);
        sb.append('.');
        insertBatchElement.addElement(new TextElement(sb.toString()));

        insertBatchElement.addElement(new TextElement("-->"));

        insertBatchElement.addElement(new TextElement("insert into " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        XmlElement trim1Element = new XmlElement("trim");
        trim1Element.addAttribute(new Attribute("prefix", "("));
        trim1Element.addAttribute(new Attribute("suffix", ")"));
        trim1Element.addAttribute(new Attribute("suffixOverrides", ","));
        trim1Element.addElement(new TextElement(dbcolumnsName.toString()));
        insertBatchElement.addElement(trim1Element);

        insertBatchElement.addElement(new TextElement("values"));

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("separator", ","));
        foreachElement.addElement(new TextElement("("));
        XmlElement trim2Element = new XmlElement("trim");
        trim2Element.addAttribute(new Attribute("suffixOverrides", ","));
        trim2Element.addElement(new TextElement(javaPropertyAndDbType.toString()));
        foreachElement.addElement(trim2Element);
        foreachElement.addElement(new TextElement(")"));
        insertBatchElement.addElement(foreachElement);
        document.getRootElement().addElement(insertBatchElement);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }
}
