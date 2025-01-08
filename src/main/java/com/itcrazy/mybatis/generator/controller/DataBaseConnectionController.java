package com.itcrazy.mybatis.generator.controller;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.model.DatabaseConnectionConfig;
import com.itcrazy.mybatis.generator.util.DataBaseUtil;
import com.itcrazy.mybatis.generator.util.SqliteUtil;
import com.itcrazy.mybatis.generator.util.MessageTipsUtil;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

/**
 * @author: itcrazy0717
 * @version: $ DataBaseConnectionController.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class DataBaseConnectionController extends BaseFxmlPageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseConnectionController.class);

    @FXML
    private TextField nameField;
    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField userNameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField schemaField;
    @FXML
    private ChoiceBox<String> encodingChoice;
    @FXML
    private ChoiceBox<String> dbTypeChoice;

	private MainApplicationController mainApplicationController;

	private boolean isUpdate = false;

	private Integer primayKey;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    void saveConnection() {
	    DatabaseConnectionConfig config = loadDataBaseConnectionConfig();
	    if (Objects.isNull(config)) {
		    return;
	    }
        try {
	        SqliteUtil.saveDatabaseConnectionConfig(config, primayKey, this.isUpdate);
            getDialogStage().close();
            mainApplicationController.loadDataBaseViewList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            MessageTipsUtil.showErrorInfo(e.getMessage());
        }
    }

    @FXML
    void testConnection() {
        DatabaseConnectionConfig config = loadDataBaseConnectionConfig();
        if (Objects.isNull(config)) {
            return;
        }
        try {
            DataBaseUtil.getConnection(config);
            MessageTipsUtil.showNormalInfo("连接成功");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            MessageTipsUtil.showWarnInfo("连接失败");
        }

    }

    @FXML
    void cancel() {
        getDialogStage().close();
    }

    void setMainApplicationController(MainApplicationController controller) {
        this.mainApplicationController = controller;
    }

	/**
	 * 导入数据库连接配置
	 * by itcrazy0717
	 *
	 * @return
	 */
    private DatabaseConnectionConfig loadDataBaseConnectionConfig() {
        String name = nameField.getText();
        String host = hostField.getText();
        String port = portField.getText();
        String userName = userNameField.getText();
        String password = passwordField.getText();
        String encoding = encodingChoice.getValue();
        String dbType = dbTypeChoice.getValue();
        String schema = schemaField.getText();
        DatabaseConnectionConfig config = new DatabaseConnectionConfig();
        config.setName(name);
        config.setDataBaseType(dbType);
        config.setHostUrl(host);
        config.setPort(port);
        config.setUserName(userName);
        config.setPassword(password);
        config.setSchemaName(schema);
        config.setEncoding(encoding);
        if (StringUtils.isAnyBlank(name, host, port, userName, encoding, dbType, schema)) {
            MessageTipsUtil.showWarnInfo("密码以外其他字段必填");
            return null;
        }
        return config;
    }

	/**
	 * 填充数据库连接配置
	 * by itcrazy0717
	 *
	 * @param config
	 */
	public void fillDataBaseConnectionConfig(DatabaseConnectionConfig config) {
		isUpdate = true;
		primayKey = config.getId();
		nameField.setText(config.getName());
		hostField.setText(config.getHostUrl());
		portField.setText(config.getPort());
		userNameField.setText(config.getUserName());
		passwordField.setText(config.getPassword());
		encodingChoice.setValue(config.getEncoding());
		dbTypeChoice.setValue(config.getDataBaseType());
		schemaField.setText(config.getSchemaName());
	}

}
