package com.itcrazy.mybatis.generator.typeresolver;

import java.sql.Types;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

/**
 * @author: itcrazy0717
 * @version: $ TinyIntToIntegerResolver.java,v0.1 2024-09-30 22:13 itcrazy0717 Exp $
 * @description:tinyint->Integer类型解析器
 */
public class TinyIntToIntegerResolver extends JavaTypeResolverDefaultImpl {

    public TinyIntToIntegerResolver() {
        super();
        // TINYINT 转换成 Integer
        typeMap.put(Types.TINYINT, new JdbcTypeInformation("TINYINT", new FullyQualifiedJavaType(Integer.class.getName())));
        // 防止bit干扰
        typeMap.remove(Types.BIT);
        typeMap.put(Types.BIT, new JdbcTypeInformation("BIT", new FullyQualifiedJavaType(Integer.class.getName())));
    }
}
