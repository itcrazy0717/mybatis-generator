package com.itcrazy.mybatis.generator.controller;

/**
 * @author: dengxin.chen
 * @version: $ FXMLPage.java,v0.1 2024-09-30 17:15 dengxin.chen Exp $
 * @description:
 */
public enum FXMLPage {

    NEW_CONNECTION("fxml/newConnection.fxml"),
    SELECT_TABLE_COLUMN("fxml/selectTableColumn.fxml"),
    GENERATOR_CONFIG("fxml/generatorConfigs.fxml"),;

    private String fxml;

    FXMLPage(String fxml) {
        this.fxml = fxml;
    }

    public String getFxml() {
        return this.fxml;
    }


}
