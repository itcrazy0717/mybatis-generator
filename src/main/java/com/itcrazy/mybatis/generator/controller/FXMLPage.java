package com.itcrazy.mybatis.generator.controller;

/**
 * @author: itcrazy0717
 * @version: $ FXMLPage.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public enum FXMLPage {

	/**
	 * 新数据库连接
	 */
	NEW_DATA_BASE_CONNECTION("fxml/newDataBaseConnection.fxml"),

	/**
	 * 选择表对应列
	 */
	SELECT_TABLE_COLUMN("fxml/selectTableColumn.fxml"),

	/**
	 * 生产配置
	 */
	GENERATE_CONFIG("fxml/generateConfig.fxml"),
    ;

    private String fxml;

    FXMLPage(String fxml) {
        this.fxml = fxml;
    }

    public String getFxml() {
        return this.fxml;
    }


}
