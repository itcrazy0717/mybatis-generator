package com.itcrazy.mybatis.generator.plugins;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.ibatis2.Ibatis2FormattingUtilities;

import com.itcrazy.mybatis.generator.util.CommentUtil;

/**
 * @author: itcrazy0717
 * @version: $ SortPlugin.java,v0.1 2024-10-04 20:07 itcrazy0717 Exp $
 * @description:排序插件
 */
public class SortPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 增加排序条件枚举
        topLevelClass.addInnerEnum(generateSortOrderConditionEnum(introspectedTable));
        // 增加排序类型枚举
        topLevelClass.addInnerEnum(generateSortTypeEnum(introspectedTable));
        // 移除setOrderByClause方法
        topLevelClass.getMethods().removeIf(e -> StringUtils.equals("setOrderByClause", e.getName()));
        // 增加排序追加方法
        topLevelClass.addMethod(generateAppendOrderByClauseMethod(introspectedTable));
        return true;
    }

    /**
     * 生成排序枚举
     * by itcrazy0717
     *
     * @param introspectedTable
     * @return
     */
    private InnerEnum generateSortTypeEnum(IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType sortTypeInstance = new FullyQualifiedJavaType("SortType");
        InnerEnum result = new InnerEnum(sortTypeInstance);
        result.addJavaDocLine("/**");
        result.addJavaDocLine(" * @mbg.generated");
        result.addJavaDocLine(" */");
        result.setVisibility(JavaVisibility.PUBLIC);
        result.setStatic(true);
        context.getCommentGenerator().addEnumComment(result, introspectedTable);

        StringBuilder ascString = new StringBuilder();
        OutputUtilities.newLine(ascString);
        OutputUtilities.javaIndent(ascString, 2);
        ascString.append("/**");
        OutputUtilities.newLine(ascString);
        OutputUtilities.javaIndent(ascString, 2);
        ascString.append(" * 升序");
        OutputUtilities.newLine(ascString);
        OutputUtilities.javaIndent(ascString, 2);
        ascString.append(" */");
        OutputUtilities.newLine(ascString);
        OutputUtilities.javaIndent(ascString, 2);
        ascString.append("ASC(\"asc\")");
        result.addEnumConstant(ascString.toString());

        StringBuilder descString = new StringBuilder();
        OutputUtilities.newLine(descString);
        OutputUtilities.javaIndent(descString, 2);
        descString.append("/**");
        OutputUtilities.newLine(descString);
        OutputUtilities.javaIndent(descString, 2);
        descString.append(" * 降序");
        OutputUtilities.newLine(descString);
        OutputUtilities.javaIndent(descString, 2);
        descString.append(" */");
        OutputUtilities.newLine(descString);
        OutputUtilities.javaIndent(descString, 2);
        descString.append("DESC(\"desc\")");
        result.addEnumConstant(descString.toString());

        Field value = new Field();
        value.setVisibility(JavaVisibility.PRIVATE);
        value.setType(FullyQualifiedJavaType.getStringInstance());
        value.setName("value");
        result.addField(value);

        // 构造函数
        Method sortTypeConstructor = new Method();
        sortTypeConstructor.setVisibility(JavaVisibility.DEFAULT);
        sortTypeConstructor.setName("SortType");
        sortTypeConstructor.setConstructor(true);
        sortTypeConstructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "value"));
        sortTypeConstructor.addBodyLine("this.value = value;");
        result.addMethod(sortTypeConstructor);

        Method getValue = new Method();
        getValue.setVisibility(JavaVisibility.PUBLIC);
        getValue.setReturnType(FullyQualifiedJavaType.getStringInstance());
        getValue.setName("getValue");
        getValue.addBodyLine("return value;");
        result.addMethod(getValue);

        Method getByName = new Method();
        getByName.setVisibility(JavaVisibility.PUBLIC);
        getByName.setStatic(Boolean.TRUE);
        getByName.setReturnType(sortTypeInstance);
        getByName.setName("getByName");
        getByName.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "name"));
        getByName.addBodyLine("SortType[] sortTypes = SortType.values();");
        getByName.addBodyLine("for (SortType sortType : sortTypes) {");
        getByName.addBodyLine("if (sortType.name().equals(name)) {");
        getByName.addBodyLine("return sortType;");
        getByName.addBodyLine("}");
        getByName.addBodyLine("}");
        getByName.addBodyLine("throw new RuntimeException(\"This SortTypeEnum of \" + name + \" does not exist\");");
        result.addMethod(getByName);

        Method toString = new Method();
        toString.addAnnotation("@Override");
        toString.setVisibility(JavaVisibility.PUBLIC);
        toString.setReturnType(FullyQualifiedJavaType.getStringInstance());
        toString.setName("toString");
        toString.addBodyLine("return value;");
        result.addMethod(toString);
        return result;
    }

    /**
     * 生成排序字段枚举
     * by itcrazy0717
     *
     * @param introspectedTable
     * @return
     */
    private InnerEnum generateSortOrderConditionEnum(IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType orderConditionInstance = new FullyQualifiedJavaType("OrderCondition");
        InnerEnum result = new InnerEnum(orderConditionInstance);
        result.addJavaDocLine("/**");
        result.addJavaDocLine(" * @mbg.generated");
        result.addJavaDocLine(" */");
        result.setVisibility(JavaVisibility.PUBLIC);
        result.setStatic(true);
        context.getCommentGenerator().addEnumComment(result, introspectedTable);
        for (IntrospectedColumn column : introspectedTable.getNonBLOBColumns()) {
            StringBuilder sb = new StringBuilder();
            OutputUtilities.newLine(sb);
            OutputUtilities.javaIndent(sb, 2);
            sb.append("/**");
            OutputUtilities.newLine(sb);
            OutputUtilities.javaIndent(sb, 2);

            sb.append(" * ").append(column.getRemarks());
            OutputUtilities.newLine(sb);
            OutputUtilities.javaIndent(sb, 2);

            sb.append(" */");
            OutputUtilities.newLine(sb);
            OutputUtilities.javaIndent(sb, 2);
            // 获取真实列名称
            String actualColumnName = Ibatis2FormattingUtilities.getAliasedActualColumnName(column);
            sb.append(actualColumnName.toUpperCase()).append("(").append("\"").append(actualColumnName).append("\"").append(")");
            result.addEnumConstant(sb.toString());
        }

        Field columnNamefield = new Field();
        columnNamefield.setVisibility(JavaVisibility.PRIVATE);
        columnNamefield.setType(FullyQualifiedJavaType.getStringInstance());
        columnNamefield.setName("columnName");
        result.addField(columnNamefield);

        // 构造函数
        Method orderConditionConstructor = new Method();
        orderConditionConstructor.setVisibility(JavaVisibility.DEFAULT);
        orderConditionConstructor.setName("OrderCondition");
        orderConditionConstructor.setConstructor(true);
        orderConditionConstructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "columnName"));
        orderConditionConstructor.addBodyLine("this.columnName = columnName;");
        result.addMethod(orderConditionConstructor);

        Method getColumnName = new Method();
        getColumnName.setVisibility(JavaVisibility.PUBLIC);
        getColumnName.setReturnType(FullyQualifiedJavaType.getStringInstance());
        getColumnName.setName("getColumnName");
        getColumnName.addBodyLine("return columnName;");
        result.addMethod(getColumnName);

        Method getByName = new Method();
        getByName.setVisibility(JavaVisibility.PUBLIC);
        getByName.setStatic(Boolean.TRUE);
        getByName.setReturnType(orderConditionInstance);
        getByName.setName("getByName");
        getByName.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "name"));
        getByName.addBodyLine("OrderCondition[] orderConditions = OrderCondition.values();");
        getByName.addBodyLine("for (OrderCondition orderCondition : orderConditions) {");
        getByName.addBodyLine("if (orderCondition.name().equals(name)) {");
        getByName.addBodyLine("return orderCondition;");
        getByName.addBodyLine("}");
        getByName.addBodyLine("}");
        getByName.addBodyLine("throw new RuntimeException(\"The OrderConditionEnum of \" + name + \" does not exist\");");
        result.addMethod(getByName);

        Method toString = new Method();
        toString.addAnnotation("@Override");
        toString.setVisibility(JavaVisibility.PUBLIC);
        toString.setReturnType(FullyQualifiedJavaType.getStringInstance());
        toString.setName("toString");
        toString.addBodyLine("return columnName;");
        result.addMethod(toString);
        return result;
    }

    /**
     * 生成appendOrderByClause方法
     * by itcrazy0717
     *
     * @param introspectedTable
     * @return
     */
    private Method generateAppendOrderByClauseMethod(IntrospectedTable introspectedTable) {
        Method appendOrderByClause = new Method();
        appendOrderByClause.setVisibility(JavaVisibility.PUBLIC);
        appendOrderByClause.setName("appendOrderByClause");
        FullyQualifiedJavaType orderConditionInstance = new FullyQualifiedJavaType("OrderCondition");
        FullyQualifiedJavaType sortTypeInstance = new FullyQualifiedJavaType("SortType");
        appendOrderByClause.setReturnType(new FullyQualifiedJavaType(introspectedTable.getExampleType()));
        appendOrderByClause.addParameter(new Parameter(orderConditionInstance, "orderCondition"));
        appendOrderByClause.addParameter(new Parameter(sortTypeInstance, "sortType"));
        appendOrderByClause.addBodyLine("if (orderByClause != null) {");
        appendOrderByClause.addBodyLine("orderByClause = orderByClause + \", \" + orderCondition.getColumnName() + \" \" + sortType" + ".getValue();");
        appendOrderByClause.addBodyLine("} else {");
        appendOrderByClause.addBodyLine("orderByClause = orderCondition.getColumnName() + \" \" + sortType.getValue();");
        appendOrderByClause.addBodyLine("}");
        appendOrderByClause.addBodyLine("return this;");
        // 增加注释
        CommentUtil.addMethodComment(appendOrderByClause, " * 增加排序条件");
        return appendOrderByClause;
    }

}
