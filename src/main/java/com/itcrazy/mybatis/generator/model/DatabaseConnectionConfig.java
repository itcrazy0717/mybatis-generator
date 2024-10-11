package com.itcrazy.mybatis.generator.model;

import lombok.Data;

/**
 * @author: itcrazy0717
 * @version: $ DatabaseConnectionConfig.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
@Data
public class DatabaseConnectionConfig {

    /**
     * The primary key in the sqlite db
     */
    private Integer id;

    /**
     * 数据库类型
     */
    private String dataBaseType;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 链接地址
     */
    private String hostUrl;

    /**
     * 端口号
     */
    private String port;

    /**
     * 数据库名称
     */
    private String schemaName;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 编码类型
     */
    private String encoding;
}
