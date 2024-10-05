package com.itcrazy.mybatis.generator.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.model.DatabaseConfig;
import com.itcrazy.mybatis.generator.util.ConfigHelper;
import com.itcrazy.mybatis.generator.util.DataBaseUtil;
import com.itcrazy.mybatis.generator.view.AlertUtil;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

/**
 * @author: itcrazy0717
 * @version: $ DbConnectionController.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class DbConnectionController extends BaseFXController {

    private static final Logger _LOG = LoggerFactory.getLogger(DbConnectionController.class);

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
    private MainUIController mainUIController;
    private boolean isUpdate = false;
    private Integer primayKey;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    void saveConnection() {
        DatabaseConfig config = extractConfigForUI();
        if (config == null) {
            return;
        }
        try {
            ConfigHelper.saveDatabaseConfig(this.isUpdate, primayKey, config);
            getDialogStage().close();
            mainUIController.loadLeftDBTree();
        } catch (Exception e) {
            _LOG.error(e.getMessage(), e);
            AlertUtil.showErrorAlert(e.getMessage());
        }
    }

    @FXML
    void testConnection() {
        DatabaseConfig config = extractConfigForUI();
        if (config == null) {
            return;
        }
        try {
            DataBaseUtil.getConnection(config);
            AlertUtil.showInfoAlert("连接成功");
        } catch (Exception e) {
            _LOG.error(e.getMessage(), e);
            AlertUtil.showWarnAlert("连接失败");
        }

    }

    @FXML
    void cancel() {
        getDialogStage().close();
    }

    void setMainUIController(MainUIController controller) {
        this.mainUIController = controller;
    }

    private DatabaseConfig extractConfigForUI() {
        String name = nameField.getText();
        String host = hostField.getText();
        String port = portField.getText();
        String userName = userNameField.getText();
        String password = passwordField.getText();
        String encoding = encodingChoice.getValue();
        String dbType = dbTypeChoice.getValue();
        String schema = schemaField.getText();
        DatabaseConfig config = new DatabaseConfig();
        config.setName(name);
        config.setDataBaseType(dbType);
        config.setHostUrl(host);
        config.setPort(port);
        config.setUserName(userName);
        config.setPassword(password);
        config.setSchemaName(schema);
        config.setEncoding(encoding);
        if (StringUtils.isAnyEmpty(name, host, port, userName, encoding, dbType, schema)) {
            AlertUtil.showWarnAlert("密码以外其他字段必填");
            return null;
        }
        return config;
    }

    public void setConfig(DatabaseConfig config) {
        isUpdate = true;
        primayKey = config.getId(); // save id for update config
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
