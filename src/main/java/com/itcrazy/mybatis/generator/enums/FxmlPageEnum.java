package com.itcrazy.mybatis.generator.enums;

import lombok.Getter;

/**
 * @author: itcrazy0717
 * @version: $ FxmlPageEnum.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
@Getter
public enum FxmlPageEnum {

	/**
	 * 新数据库连接
	 */
	NEW_DATABASE_CONNECTION("fxml/newDataBaseConnection.fxml"),

	/**
	 * 选择表对应列
	 */
	SELECT_TABLE_COLUMN("fxml/selectTableColumn.fxml"),

	/**
	 * 生成配置
	 */
	GENERATE_CONFIG("fxml/generateCodeTemplate.fxml"),
    ;

    private final String fxml;

    FxmlPageEnum(String fxml) {
        this.fxml = fxml;
    }

}
