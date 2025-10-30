package com.itcrazy.mybatis.generator.typeresolver;

import java.sql.Types;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

/**
 * @author: itcrazy0717
 * @version: $ TinyIntToBooleanTypeResolver.java,v0.1 2024-09-30 22:13 itcrazy0717 Exp $
 * @description:TINYINT->Boolean类型解析器
 */
public class TinyIntToBooleanTypeResolver extends JavaTypeResolverDefaultImpl {

    public TinyIntToBooleanTypeResolver() {
        super();
        this.typeMap.put(Types.TINYINT, new JdbcTypeInformation("TINYINT", new FullyQualifiedJavaType(Boolean.class.getName())));

    }
}
