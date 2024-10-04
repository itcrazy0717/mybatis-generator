package com.itcrazy.mybatis.generator.plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import com.itcrazy.mybatis.generator.util.CommentUtil;

/**
 * @author: itcrazy0717
 * @version: $ PagePlugin.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:MySQL分页插件
 */
public class PagePlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    /**
     * 生成setPagination方法
     * by itcrazy0717
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType intType = new FullyQualifiedJavaType("int");
        // page字段
        Field page = new Field();
        page.addJavaDocLine("/**");
        page.addJavaDocLine(" * @mbg.generated");
        page.addJavaDocLine(" */");
        page.setName("page");
        page.setVisibility(JavaVisibility.PROTECTED);
        page.setType(new FullyQualifiedJavaType("boolean"));
        topLevelClass.addField(page);

        // pageIndex字段
        Field pageIndex = new Field();
        pageIndex.addJavaDocLine("/**");
        pageIndex.addJavaDocLine(" * @mbg.generated");
        pageIndex.addJavaDocLine(" */");
        pageIndex.setName("pageIndex");
        pageIndex.setVisibility(JavaVisibility.PROTECTED);
        pageIndex.setType(intType);
        topLevelClass.addField(pageIndex);

        // pageSize字段
        Field pageSize = new Field();
        pageSize.addJavaDocLine("/**");
        pageSize.addJavaDocLine(" * @mbg.generated");
        pageSize.addJavaDocLine(" */");
        pageSize.setName("pageSize");
        pageSize.setVisibility(JavaVisibility.PROTECTED);
        pageSize.setType(intType);
        topLevelClass.addField(pageSize);

        Method setPagination = new Method();
        setPagination.setVisibility(JavaVisibility.PUBLIC);
        setPagination.setName("setPagination");
        setPagination.addParameter(new Parameter(intType, "pageStart"));
        setPagination.addParameter(new Parameter(intType, "pageSize"));
        setPagination.addBodyLine("this.page = true;");
        setPagination.addBodyLine("this.pageSize = pageSize < 1 ? 10 : pageSize;");
        setPagination.addBodyLine("this.pageIndex = pageStart < 1 ? 0 : (pageStart - 1) * this.pageSize;");
        // 增加注释
        CommentUtil.addMethodComment(setPagination, "");
        topLevelClass.addMethod(setPagination);
        return true;
    }

    /**
     * 生成对应分页方法
     * by itcrazy0717
     */
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        XmlElement ifPageXmlElement = new XmlElement("if");
        ifPageXmlElement.addAttribute(new Attribute("test", "page"));
        ifPageXmlElement.addElement(new TextElement("limit #{pageIndex}, #{pageSize}"));
        element.addElement(ifPageXmlElement);
        return true;
    }
}
