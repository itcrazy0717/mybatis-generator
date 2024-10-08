/*
 *  Copyright 2008 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.itcrazy.mybatis.generator.plugins;

import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.FastDateFormat;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

/**
 * @author: itcrazy0717
 * @version: $ CustomCommentGenerator.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:自定义注释插件
 */
public class CustomCommentGenerator extends DefaultCommentGenerator {

	/**
	 * 配置
	 */
	private final Properties properties;

	/**
	 * 是否使用注解
	 * 目前未用
	 */
	private boolean isAnnotations;

    /**
     * Param对象正则表达式
     */
    private final static Pattern paramPattern = Pattern.compile("Param");

    public CustomCommentGenerator() {
        super();
        properties = new Properties();
    }


    public void addJavaFileComment(CompilationUnit compilationUnit) {
        if (isAnnotations) {
            compilationUnit.addImportedType(new FullyQualifiedJavaType("javax.persistence.Table"));
            compilationUnit.addImportedType(new FullyQualifiedJavaType("javax.persistence.Id"));
            compilationUnit.addImportedType(new FullyQualifiedJavaType("javax.persistence.Column"));
            compilationUnit.addImportedType(new FullyQualifiedJavaType("javax.persistence.GeneratedValue"));
            compilationUnit.addImportedType(new FullyQualifiedJavaType("org.hibernate.validator.constraints.NotEmpty"));
        }
        // 当对象为接口类型的时候，则表明为Dao接口对象
        if (compilationUnit instanceof Interface) {
            Interface daoInterface = (Interface) compilationUnit;
            daoInterface.addJavaDocLine("/**");
            daoInterface.addJavaDocLine("* This class is automatically generated by MyBatis Generator");
            daoInterface.addJavaDocLine("*");
            daoInterface.addJavaDocLine("* @author mbg.generated");
            daoInterface.addJavaDocLine("*/");
        }
        // 匹配Param对象
        Matcher matcher = paramPattern.matcher(compilationUnit.getType().getShortName());
        // 为Param对象增加注释
	    if (matcher.find() && (compilationUnit instanceof TopLevelClass)) {
            TopLevelClass paramClass = (TopLevelClass) compilationUnit;
            paramClass.addJavaDocLine("/**");
            paramClass.addJavaDocLine("* This class is automatically generated by MyBatis Generator");
            paramClass.addJavaDocLine("*");
            paramClass.addJavaDocLine("* @author mbg.generated");
            paramClass.addJavaDocLine("*/");
        }
    }

    /**
     * Adds a suitable comment to warn users that the element was generated, and
     * when it was generated.
     */
    public void addComment(XmlElement xmlElement) {
        xmlElement.addElement(new TextElement("<!--"));
        StringBuilder sb = new StringBuilder();
        sb.append("  WARNING - ");
        sb.append("@mbg.generated");
        xmlElement.addElement(new TextElement(sb.toString()));
        xmlElement.addElement(new TextElement("  This element is automatically generated by MyBatis Generator, do not modify."));
        String nowTimeString = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(new Date());
        sb.setLength(0);
        sb.append("  This element was generated on ");
        sb.append(nowTimeString);
        sb.append('.');
        xmlElement.addElement(new TextElement(sb.toString()));
        xmlElement.addElement(new TextElement("-->"));
    }

	@Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> set) {

    }

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> set) {

    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> set) {

    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> set) {

    }

    @Override
    public void addClassAnnotation(InnerClass innerClass, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> set) {

    }

    public void addConfigurationProperties(Properties properties) {
        this.properties.putAll(properties);
        isAnnotations = isTrue(properties.getProperty("annotations"));
    }

    public void addClassComment(InnerClass innerClass,
                                IntrospectedTable introspectedTable) {
    }

    public void addModelClassComment(TopLevelClass topLevelClass,
                                     IntrospectedTable introspectedTable) {
        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine("* This class is automatically generated by MyBatis Generator");
        topLevelClass.addJavaDocLine("*");
        topLevelClass.addJavaDocLine("* @author mbg.generated");
        topLevelClass.addJavaDocLine("*/");
        if (isAnnotations) {
            topLevelClass.addAnnotation("@Table(name=\"" + introspectedTable.getFullyQualifiedTableNameAtRuntime() + "\")");
        }
    }

    public void addEnumComment(InnerEnum innerEnum,
                               IntrospectedTable introspectedTable) {
    }

    public void addFieldComment(Field field,
                                IntrospectedTable introspectedTable,
                                IntrospectedColumn introspectedColumn) {
        if (StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * " + introspectedColumn.getRemarks());
            field.addJavaDocLine(" *");
            field.addJavaDocLine(" * @mbg.generated");
            field.addJavaDocLine(" */");
        }
        if (isAnnotations) {
            boolean isId = false;
            for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
                if (introspectedColumn == column) {
                    isId = true;
                    field.addAnnotation("@Id");
                    field.addAnnotation("@GeneratedValue");
                    break;
                }
            }
            if (!introspectedColumn.isNullable() && !isId) {
                field.addAnnotation("@NotEmpty");
            }
            if (introspectedColumn.isIdentity()) {
                if (introspectedTable.getTableConfiguration().getGeneratedKey().getRuntimeSqlStatement().equals("JDBC")) {
                    field.addAnnotation("@GeneratedValue(generator = \"JDBC\")");
                } else {
                    field.addAnnotation("@GeneratedValue(strategy = GenerationType.IDENTITY)");
                }
            } else if (introspectedColumn.isSequenceColumn()) {
                field.addAnnotation("@SequenceGenerator(name=\"\",sequenceName=\"" + introspectedTable.getTableConfiguration().getGeneratedKey().getRuntimeSqlStatement() + "\")");
            }
        }
    }

    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
        field.addJavaDocLine("/**");
        field.addJavaDocLine(" * @mbg.generated");
        field.addJavaDocLine(" */");
    }

    public void addGeneralMethodComment(Method method,
                                        IntrospectedTable introspectedTable) {
    }

    public void addGetterComment(Method method,
                                 IntrospectedTable introspectedTable,
                                 IntrospectedColumn introspectedColumn) {
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" * @mbg.generated");
        method.addJavaDocLine(" */");
    }

    public void addSetterComment(Method method,
                                 IntrospectedTable introspectedTable,
                                 IntrospectedColumn introspectedColumn) {
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" * @mbg.generated");
        method.addJavaDocLine(" */");
    }

    public void addClassComment(InnerClass innerClass,
                                IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {
        innerClass.addJavaDocLine("/**");
        innerClass.addJavaDocLine(" * @mbg.generated");
        innerClass.addJavaDocLine(" */");
    }
}
