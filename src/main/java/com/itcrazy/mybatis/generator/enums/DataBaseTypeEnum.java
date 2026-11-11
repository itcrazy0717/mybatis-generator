package com.itcrazy.mybatis.generator.enums;

import lombok.Getter;

/**
 * @author: itcrazy0717
 * @version: $ DataBaseTypeEnum.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
@Getter
public enum DataBaseTypeEnum {

    /**
     * MySQL数据库
     */
    MySQL("com.mysql.cj.jdbc.Driver", "jdbc:mysql://%s:%s/%s?useUnicode=true&useSSL=false&tinyInt1isBit=false&characterEncoding=%s", "mysql-connector-j-8.4.0.jar"),

    /**
     * Oracle数据库
     * extraJar: orai18n.jar提供中文字符集（如ZHS16GBK）转换器，缺失会报 ORA-17056 不支持的字符集
     */
    Oracle11g("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@%s:%s:%s", "ojdbc8.jar", "orai18n.jar"),

    /**
     * PostgreSQL数据库
     */
    PostgreSQL("org.postgresql.Driver", "jdbc:postgresql://%s:%s/%s", "postgresql-9.4.1209.jar"),

    /**
     * SQLServer数据库
     */
    SQLServer("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://%s:%s;databaseName=%s", "sqljdbc4-4.0.jar"),

    /**
     * 达梦8数据库
     */
    DM8("dm.jdbc.driver.DmDriver", "jdbc:dm://%s:%s/%s", "DmJdbcDriver8.jar"),

    ;

    /**
     * 驱动类
     */
    private final String driverClass;

    /**
     * 数据连接url
     */
    private final String connectionUrlPattern;

    /**
     * 驱动jar文件
     */
    private final String driverJar;

    /**
     * 附加jar文件（如Oracle的orai18n字符集转换jar），可为空
     */
    private final String extraJar;

    DataBaseTypeEnum(String driverClass, String connectionUrlPattern, String driverJar) {
        this(driverClass, connectionUrlPattern, driverJar, null);
    }

    DataBaseTypeEnum(String driverClass, String connectionUrlPattern, String driverJar, String extraJar) {
        this.driverClass = driverClass;
        this.connectionUrlPattern = connectionUrlPattern;
        this.driverJar = driverJar;
        this.extraJar = extraJar;
    }
}