package com.itcrazy.mybatis.generator.plugins;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
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
import org.mybatis.generator.config.MergeConstants;

import static java.sql.JDBCType.VARCHAR;
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

        // 构建字段名的trim标签（带if判空）
        XmlElement trimColumNameElement = new XmlElement("trim");
        trimColumNameElement.addAttribute(new Attribute("prefix", "("));
        trimColumNameElement.addAttribute(new Attribute("suffix", ")"));
        trimColumNameElement.addAttribute(new Attribute("suffixOverrides", ","));

        // 构建字段值的trim标签（带if判空）
        XmlElement trimColumValueElement = new XmlElement("trim");
        trimColumValueElement.addAttribute(new Attribute("suffixOverrides", ","));

        // 遍历所有字段，逐个生成带判空的if标签
        for (IntrospectedColumn introspectedColumn : columns) {
            String columnName = introspectedColumn.getActualColumnName();
            // 跳过自增字段（自增字段不参与插入）
            if (introspectedColumn.isAutoIncrement()) {
                continue;
            }
            String javaProperty = introspectedColumn.getJavaProperty();
            String jdbcTypeName = introspectedColumn.getJdbcTypeName();

            // ========== 1. 生成字段名的if判空标签 ==========
            XmlElement columnIfElement = new XmlElement("if");
            // 拼接判空条件：VARCHAR类型需额外判断空字符串
            String columnTestCondition = buildTestCondition(javaProperty, jdbcTypeName);
            columnIfElement.addAttribute(new Attribute("test", columnTestCondition));
            columnIfElement.addElement(new TextElement(columnName + ","));
            trimColumNameElement.addElement(columnIfElement);

            // ========== 2. 生成字段值的if判空标签 ==========
            XmlElement valueIfElement = new XmlElement("if");
            valueIfElement.addAttribute(new Attribute("test", columnTestCondition));
            // 拼接赋值语句：#{item.字段,jdbcType=XXX,...}
            StringBuilder valueSb = new StringBuilder();
            valueSb.append("#{item.").append(javaProperty).append(",jdbcType=").append(jdbcTypeName);

            // 追加类型处理器（若有）
            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                valueSb.append(",typeHandler=");
                valueSb.append(introspectedColumn.getTypeHandler());
                valueSb.append(",javaType=");
                valueSb.append(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedNameWithoutTypeParameters());
            }
            valueSb.append("},");
            valueIfElement.addElement(new TextElement(valueSb.toString()));
            trimColumValueElement.addElement(valueIfElement);
        }

        // ========== 构建批量插入的XML节点 ==========
        XmlElement insertBatchElement = new XmlElement("insert");
        insertBatchElement.addAttribute(new Attribute("id", "batchInsert"));
        insertBatchElement.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));

        // 生成注释
        insertBatchElement.addElement(new TextElement("<!--"));
        StringBuilder sb = new StringBuilder();
        sb.append("  WARNING - ");
        sb.append(MergeConstants.NEW_ELEMENT_TAG);
        insertBatchElement.addElement(new TextElement(sb.toString()));
        insertBatchElement.addElement(new TextElement("  This element is automatically generated by MyBatis Generator, do not modify."));

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTimeString = df.format(new Date());
        sb.setLength(0);
        sb.append("  This element was generated on ");
        sb.append(nowTimeString);
        sb.append('.');
        insertBatchElement.addElement(new TextElement(sb.toString()));
        insertBatchElement.addElement(new TextElement("-->"));

        // 拼接INSERT INTO语句
        insertBatchElement.addElement(new TextElement("insert into " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        insertBatchElement.addElement(trimColumNameElement); // 添加字段名的trim标签
        insertBatchElement.addElement(new TextElement("values"));

        // 构建foreach循环节点
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("separator", ","));
        foreachElement.addElement(new TextElement("("));
        foreachElement.addElement(trimColumValueElement); // 添加字段值的trim标签
        foreachElement.addElement(new TextElement(")"));
        insertBatchElement.addElement(foreachElement);

        // 将批量插入节点添加到XML根节点
        document.getRootElement().addElement(insertBatchElement);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    /**
     * 构建判空条件：
     * - VARCHAR类型：item.字段 != null and item.字段 != ''
     * - 非VARCHAR类型：item.字段 != null
     * by itcrazy0717
     *
     * @param javaProperty
     * @param jdbcTypeName
     * @return
     */
    private String buildTestCondition(String javaProperty, String jdbcTypeName) {
        StringBuilder condition = new StringBuilder("item.").append(javaProperty).append(" != null");
        // 针对VARCHAR类型，额外添加空字符串判断
        if (VARCHAR.name().equalsIgnoreCase(jdbcTypeName)) {
            condition.append(" and item.").append(javaProperty).append(" != ''");
        }
        return condition.toString();
    }
}
