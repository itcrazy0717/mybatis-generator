package com.itcrazy.mybatis.generator.plugins;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.BooleanUtils;
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

import static javax.swing.UIManager.getString;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author: itcrazy0717
 * @version: $ BatchInsertPlugin.java,v0.1 2024-09-30 21:15 itcrazy0717 Exp $
 * @description:批量插入插件
 */
public class BatchInsertPlugin extends PluginAdapter {

    /**
     * 主键id字段
     */
    private String primaryKey;

    /**
     * insert方法是否返回主键id
     */
    private Boolean insertReturnPrimaryKey;

    @Override
    public boolean validate(List<String> warnings) {
        primaryKey = properties.getProperty("primaryKey");
        insertReturnPrimaryKey = BooleanUtils.toBoolean(properties.getProperty("insertReturnPrimaryKey"));
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

        // ========== 1. 构建固定字段名（外层trim） ==========
        XmlElement trimFieldsElement = new XmlElement("trim");
        trimFieldsElement.addAttribute(new Attribute("prefix", "("));
        trimFieldsElement.addAttribute(new Attribute("suffix", ")"));
        trimFieldsElement.addAttribute(new Attribute("suffixOverrides", ","));

        StringBuilder fieldSb = new StringBuilder();
        for (IntrospectedColumn col : columns) {
            // 跳过自增字段
            if (col.isAutoIncrement()) {
                continue;
            }
            fieldSb.append(col.getActualColumnName()).append(",");
        }
        trimFieldsElement.addElement(new TextElement(fieldSb.toString()));

        // ========== 2. 构建字段值（foreach内部，按规则处理null） ==========
        XmlElement trimValuesElement = new XmlElement("trim");
        trimValuesElement.addAttribute(new Attribute("suffixOverrides", ","));

        // 主键key对应的java对象字段
        String primaryKeyProperty = primaryKey;
        for (IntrospectedColumn col : columns) {
            if (col.isAutoIncrement()) {
                primaryKeyProperty = col.getJavaProperty();
                continue;
            }

            String javaProp = col.getJavaProperty();
            String jdbcType = col.getJdbcTypeName();
            // 是否有数据库默认值
            boolean hasDefaultValue = stringHasValue(col.getDefaultValue());

            // -------------- 规则1：字段有默认值 --------------
            if (hasDefaultValue) {
                // 规则：属性为null → 用default关键字（触发数据库默认值）
                XmlElement ifNullElement = new XmlElement("if");
                ifNullElement.addAttribute(new Attribute("test", "item." + javaProp + " == null"));
                ifNullElement.addElement(new TextElement("default,"));
                trimValuesElement.addElement(ifNullElement);

                // 规则：属性不为null → 正常传值
                XmlElement ifNotNullElement = new XmlElement("if");
                ifNotNullElement.addAttribute(new Attribute("test", "item." + javaProp + " != null"));
                StringBuilder valSb = new StringBuilder();
                valSb.append("#{item.").append(javaProp).append(",jdbcType=").append(jdbcType);
                if (stringHasValue(col.getTypeHandler())) {
                    valSb.append(",typeHandler=").append(col.getTypeHandler());
                }
                valSb.append("},");
                ifNotNullElement.addElement(new TextElement(valSb.toString()));
                trimValuesElement.addElement(ifNotNullElement);
            }
            // -------------- 规则2：字段无默认值 --------------
            else {
                // 规则：属性为null → 传null；属性不为null → 正常传值
                StringBuilder valSb = new StringBuilder();
                valSb.append("#{item.").append(javaProp).append(",jdbcType=").append(jdbcType);
                if (stringHasValue(col.getTypeHandler())) {
                    valSb.append(",typeHandler=").append(col.getTypeHandler());
                }
                valSb.append("},");
                trimValuesElement.addElement(new TextElement(valSb.toString()));
            }
        }

        // ========== 3. 组装批量插入XML节点 ==========
        XmlElement batchInsertElement = new XmlElement("insert");
        batchInsertElement.addAttribute(new Attribute("id", "batchInsert"));
        batchInsertElement.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        // 勾选插入返回主键id，则插入数据时，返回主键id
        if (BooleanUtils.isTrue(insertReturnPrimaryKey) && StringUtils.isNotBlank(primaryKey)) {
            batchInsertElement.addAttribute(new Attribute("useGeneratedKeys", "true"));
            batchInsertElement.addAttribute(new Attribute("keyProperty", primaryKeyProperty));
            batchInsertElement.addAttribute(new Attribute("keyColumn", primaryKey));
        }

        // 注释
        batchInsertElement.addElement(new TextElement("<!-- 批量插入规则："));
        batchInsertElement.addElement(new TextElement("   1. 字段有默认值：属性为null → 用默认值；属性非null → 正常传值"));
        batchInsertElement.addElement(new TextElement("   2. 字段无默认值：根据字段具体内容传值即可 -->"));

        // 拼接INSERT INTO语句
        batchInsertElement.addElement(new TextElement("insert into " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        // 固定字段名
        batchInsertElement.addElement(trimFieldsElement);
        batchInsertElement.addElement(new TextElement("values"));

        // 构建foreach循环
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("separator", ","));
        foreachElement.addElement(new TextElement("("));
        // 按规则处理的字段值
        foreachElement.addElement(trimValuesElement);
        foreachElement.addElement(new TextElement(")"));

        batchInsertElement.addElement(foreachElement);
        document.getRootElement().addElement(batchInsertElement);

        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }
}
