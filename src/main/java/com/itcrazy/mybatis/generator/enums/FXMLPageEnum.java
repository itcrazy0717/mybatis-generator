package com.itcrazy.mybatis.generator.enums;

import lombok.Getter;

/**
 * @author: itcrazy0717
 * @version: $ FXMLPageEnum.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
@Getter
public enum FXMLPageEnum {

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

    private final String fxml;

    FXMLPageEnum(String fxml) {
        this.fxml = fxml;
    }

}
