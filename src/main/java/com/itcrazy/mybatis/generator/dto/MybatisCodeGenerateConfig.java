package com.itcrazy.mybatis.generator.dto;

import lombok.Data;

/**
 * @author: itcrazy0717
 * @version: $ MybatisCodeGenerateConfig.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:Mybatis代码生成配置
 */
@Data
public class MybatisCodeGenerateConfig {

    /**
     * 配置名称
     */
    private String name;

	/**
	 * 驱动文件地址
	 */
    private String connectorJarPath;

	/**
	 * 项目所在目录
	 */
	private String projectFolder;

	/**
	 * 实体类路径
	 */
	private String modelPackage;

	/**
	 * 实体类目录
	 */
    private String modelPackageTargetFolder;

	/**
	 * dao对象包路径
	 */
	private String daoPackage;

	/**
	 * dao对象目录
	 */
    private String daoTargetFolder;

	/**
	 * mapper对象名称 DAO对象名称
	 */
	private String mapperName;

	/**
	 * mapper xml 文件路径
	 */
	private String mapperXMLPackage;

	/**
	 * mapper xml 文件目录
	 */
    private String mapperXMLTargetFolder;

	/**
	 * 表名
	 */
    private String tableName;

	/**
	 * 查询参数包路径
	 */
	private String paramModelPackage;

	/**
	 * 实体对象名称
	 */
    private String domainObjectName;

}
