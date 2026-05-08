package com.itcrazy.mybatis.generator.plugins;

import java.util.List;
import java.util.Objects;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import com.itcrazy.mybatis.generator.enums.DataBaseTypeEnum;

import static com.itcrazy.mybatis.generator.constant.CommonConstants.PROPERTY_DATABASE_TYPE;

/**
 * @author: by itcrazy0717
 * @version: $ DMLikePlugin.java,v0.1 2026-05-07 11:43 itcrazy0717 Exp $
 * @description: 达梦数据库的LIKE查询插件
 */
public class DMLikePlugin extends PluginAdapter {

    /**
     * 数据库类型
     */
    private String dataBaseType;

    @Override
    public boolean validate(List<String> list) {
        dataBaseType = properties.getProperty(PROPERTY_DATABASE_TYPE);
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 只处理DM数据库
        if (!DataBaseTypeEnum.DM8.name().equals(dataBaseType)) {
            return true;
        }
        // 找到Criteria内部类
        InnerClass criteriaClass = null;
        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("Criteria".equals(innerClass.getType().getShortName())) {
                criteriaClass = innerClass;
                break;
            }
        }
        if (Objects.isNull(criteriaClass)) {
            return true;
        }

        // 遍历所有字符串字段，生成方法
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.isStringColumn()) {
                generateLikeMethod(criteriaClass, column);
            }
        }
        return true;
    }

    /**
     * 生成like方法
     * by itcrazy0717
     *
     * @param criteriaClass
     * @param column
     */
    private void generateLikeMethod(InnerClass criteriaClass, IntrospectedColumn column) {
        String javaProperty = column.getJavaProperty();
        String columnName = column.getActualColumnName();

        // 方法名：andUserNameLikeWithEscape
        String methodName = buildMethodName(javaProperty);

        // 1. 定义方法：public Criteria andXxxLikeWithEscape(String value)
        Method method = new Method(methodName);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "value"));

        // 2. 方法注释
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" * LIKE 模糊查询，使用 ESCAPE '\\\\' 转义");
        method.addJavaDocLine(" *");
        method.addJavaDocLine(" * @param value 已拼接好%的模糊匹配值（例：%abc\\_def%）");
        method.addJavaDocLine(" * @mbg.generated");
        method.addJavaDocLine(" */");

        // 3. 方法体，内部转义，防注入
        method.addBodyLine("addCriterion(\"" + columnName + " like '\" + value.replace(\"'\", \"''\") + \"' ESCAPE '\\\\'\");");
        method.addBodyLine("return this;");

        // 4. 添加到Criteria
        criteriaClass.addMethod(method);
    }

    /**
     * 构建方法名
     * by itcrazy0717
     *
     * @param javaProperty
     * @return
     */
    private String buildMethodName(String javaProperty) {
        // 属性的首字母大写
        String javaPropertyName = javaProperty.substring(0, 1).toUpperCase() + javaProperty.substring(1);
        return "and" + javaPropertyName + "LikeWithEscape";
    }
}
