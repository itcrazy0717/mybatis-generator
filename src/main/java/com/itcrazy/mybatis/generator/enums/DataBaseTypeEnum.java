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
    MySQL("com.mysql.cj.jdbc.Driver", "jdbc:mysql://%s:%s/%s?useUnicode=true&useSSL=false&tinyInt1isBit=false&characterEncoding=%s", "mysql-connector-java-8.0.30.jar"),

    /**
     * Oracle数据库
     */
    Oracle("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@%s:%s:%s", "ojdbc14.jar"),

    /**
     * PostgreSQL数据库
     */
    PostgreSQL("org.postgresql.Driver", "jdbc:postgresql://%s:%s/%s", "postgresql-9.4.1209.jar"),

    /**
     * SQLServer数据库
     */
    SQLServer("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://%s:%s;databaseName=%s", "sqljdbc4-4.0.jar");

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

    DataBaseTypeEnum(String driverClass, String connectionUrlPattern, String driverJar) {
        this.driverClass = driverClass;
        this.connectionUrlPattern = connectionUrlPattern;
        this.driverJar = driverJar;
    }
}