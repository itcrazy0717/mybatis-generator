package com.itcrazy.mybatis.generator.typeresolver;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

/**
 * @author: dengxin.chen
 * @version: $ TinyIntTypeResolver.java,v0.1 2024-09-30 22:13 dengxin.chen Exp $
 * @description:TINYINT->Boolean类型解析器
 */
public class TinyIntTypeResolver extends JavaTypeResolverDefaultImpl {

    public TinyIntTypeResolver() {
        super();
        // 直接覆盖父类的默认转换器，调整为Boolean类型
        this.typeMap.put(-6, new JdbcTypeInformation("TINYINT", new FullyQualifiedJavaType(Boolean.class.getName())));
    }
}
