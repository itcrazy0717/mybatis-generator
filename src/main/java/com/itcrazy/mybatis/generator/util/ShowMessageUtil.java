package com.itcrazy.mybatis.generator.util;

import javafx.scene.control.Alert;

/**
 * @author: itcrazy0717
 * @version: $ ShowMessageUtil.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class ShowMessageUtil {

	/**
	 * 显示正常信息
	 * by itcrazy0717
	 *
	 * @param message
	 */
	public static void showNormalInfo(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setContentText(message);
		alert.show();
	}

	/**
	 * 显示警告信息
	 * by itcrazy0717
	 *
	 * @param message
	 */
	public static void showWarnInfo(String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setContentText(message);
		alert.show();
	}

	/**
	 * 显示错误信息
	 * by itcrazy0717
	 *
	 * @param message
	 */
	public static void showErrorInfo(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setContentText(message);
		alert.show();
	}

}
