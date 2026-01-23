package com.itcrazy.mybatis.generator.plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.xml.XmlElement;

import com.itcrazy.mybatis.generator.enums.DataBaseTypeEnum;

import static com.itcrazy.mybatis.generator.constant.CommonConstants.PROPERTY_DATABASE_TYPE;

/**
 * @author: itcrazy0717
 * @version: $ GenerateInsertMethodPlugin.java,v0.1 2026-01-23 11:24 itcrazy0717 Exp $
 * @description:
 */
public class GenerateInsertMethodPlugin extends PluginAdapter {

    /**
     * 数据库类型
     */
    private String dataBaseType;

    @Override
    public boolean validate(List<String> warnings) {
        dataBaseType = properties.getProperty(PROPERTY_DATABASE_TYPE);
        return true;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze,
                                               IntrospectedTable introspectedTable) {
        // 达梦数的自增字段，如果在sql语句中出现，但不进行赋值，在插入的时候会报错
        // 因此进行判断，若果是达梦则不生成普通insert方法
        return !DataBaseTypeEnum.DM8.name().equals(dataBaseType);
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return !DataBaseTypeEnum.DM8.name().equals(dataBaseType);
    }
}
