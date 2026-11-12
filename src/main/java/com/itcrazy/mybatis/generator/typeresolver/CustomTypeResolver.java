package com.itcrazy.mybatis.generator.typeresolver;

import java.sql.Types;
import java.util.Properties;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

/**
 * @author: itcrazy0717
 * @version: $ CustomTypeResolver.java,v0.1 2026-11-11 itcrazy0717 Exp $
 * @description:自定义类型解析器
 * 1. TINYINT/BIT 统一映射成 Integer（TINYINT(1)不转Boolean）
 * 2. NUMERIC/DECIMAL 统一强制映射成 BigDecimal（含Oracle不带精度的NUMBER），不再按精度降级为Short/Integer/Long
 */
public class CustomTypeResolver extends JavaTypeResolverDefaultImpl {

    public CustomTypeResolver() {
        super();
        // TINYINT 转换成 Integer
        typeMap.put(Types.TINYINT, new JdbcTypeInformation("TINYINT", new FullyQualifiedJavaType(Integer.class.getName())));
        // 防止bit干扰
        typeMap.remove(Types.BIT);
        typeMap.put(Types.BIT, new JdbcTypeInformation("BIT", new FullyQualifiedJavaType(Integer.class.getName())));
    }

    /**
     * 强制NUMERIC/DECIMAL映射为BigDecimal
     * by itcrazy0717
     *
     * @param properties
     */
    @Override
    public void addConfigurationProperties(Properties properties) {
        // 注意：core的实现是无条件按属性值重置forceBigDecimals，生成时ObjectFactory会再次注入配置属性，
        // 因此必须在每次属性注入前强制写入，否则构造期设置会被冲掉
        properties.setProperty("forceBigDecimals", "true");
        super.addConfigurationProperties(properties);
    }
}
