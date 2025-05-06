package com.itcrazy.mybatis.generator.controller;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.model.DatabaseConnectionConfig;
import com.itcrazy.mybatis.generator.util.DataBaseUtil;
import com.itcrazy.mybatis.generator.util.ShowMessageUtil;
import com.itcrazy.mybatis.generator.util.SqliteUtil;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

/**
 * @author: itcrazy0717
 * @version: $ DataBaseConnectionController.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:数据库连接控制器
 */
public class DataBaseConnectionController extends BaseFxmlPageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseConnectionController.class);

    /**
     * 保存名称
     */
    @FXML
    private TextField nameField;

    /**
     * 数据库类型
     */
    @FXML
    private ChoiceBox<String> dbTypeChoice;

    /**
     * 连接地址
     */
    @FXML
    private TextField hostField;

    /**
     * 端口
     */
    @FXML
    private TextField portField;

    /**
     * 用户名
     */
    @FXML
    private TextField userNameField;

    /**
     * 密码
     */
    @FXML
    private TextField passwordField;

    /**
     * 数据库
     */
    @FXML
    private TextField schemaField;

    /**
     * 编码
     */
    @FXML
    private ChoiceBox<String> encodingChoice;

    /**
     * 主窗口控制器
     */
    private MainApplicationController mainApplicationController;

    /**
     * 是否更新操作，默认不是
     */
    private boolean update = false;

    /**
     * 主键
     */
    private Integer primayKey;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 设置数据库类型默认值
        dbTypeChoice.setValue("MySQL");
        // 设置编码默认值
        encodingChoice.setValue("utf8");
    }

    /**
     * 保存数据库连接配置
     * by itcrazy0717
     */
    @FXML
    void saveConnection() {
        DatabaseConnectionConfig config = buildDataBaseConnectionConfig();
        if (Objects.isNull(config)) {
            return;
        }
        try {
            SqliteUtil.saveDatabaseConnectionConfig(config, primayKey, this.update);
            getDialogStage().close();
            mainApplicationController.loadDataBaseViewList();
        } catch (Exception e) {
            LOGGER.error("save_db_connection_error", e);
            ShowMessageUtil.showErrorInfo(e.getMessage());
        }
    }

    /**
     * 数据连接测试
     * by itcrazy0717
     */
    @FXML
    void testConnection() {
        DatabaseConnectionConfig connectionConfig = buildDataBaseConnectionConfig();
        if (Objects.isNull(connectionConfig)) {
            return;
        }
        try {
            DataBaseUtil.getConnection(connectionConfig);
            ShowMessageUtil.showNormalInfo("连接成功");
        } catch (Exception e) {
            LOGGER.error("test_db_connection_error", e);
            ShowMessageUtil.showWarnInfo("数据连接失败");
        }

    }

    @FXML
    void cancel() {
        getDialogStage().close();
    }

    public void setMainApplicationController(MainApplicationController controller) {
        this.mainApplicationController = controller;
    }

    /**
     * 构建数据库连接配置
     * by itcrazy0717
     *
     * @return
     */
    private DatabaseConnectionConfig buildDataBaseConnectionConfig() {
        DatabaseConnectionConfig connectionConfig = new DatabaseConnectionConfig();
        connectionConfig.setName(nameField.getText());
        connectionConfig.setDataBaseType(dbTypeChoice.getValue());
        connectionConfig.setHostUrl(hostField.getText());
        connectionConfig.setPort(portField.getText());
        connectionConfig.setUserName(userNameField.getText());
        connectionConfig.setPassword(passwordField.getText());
        connectionConfig.setSchemaName(schemaField.getText());
        connectionConfig.setEncoding(encodingChoice.getValue());
        // 参数校验
        String checkResult = checkParam(connectionConfig);
        if (StringUtils.isNotBlank(checkResult)) {
            ShowMessageUtil.showWarnInfo(checkResult);
            return null;
        }
        return connectionConfig;
    }

    /**
     * 填充数据库连接配置
     * by itcrazy0717
     *
     * @param config
     */
    public void fillDataBaseConnectionConfig(DatabaseConnectionConfig config) {
        // 调用该函数时，确定为编辑操作
        update = true;
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

    /**
     * 校验数据库连接参数
     * by itcrazy0717
     *
     * @param connectionConfig
     * @return
     */
    private String checkParam(DatabaseConnectionConfig connectionConfig) {
        if (StringUtils.isBlank(connectionConfig.getName())) {
            return "保存名称为空";
        }
        if (StringUtils.isBlank(connectionConfig.getDataBaseType())) {
            return "数据库类型为空";
        }
        if (StringUtils.isBlank(connectionConfig.getHostUrl())) {
            return "主机名或IP地址为空";
        }
        if (StringUtils.isBlank(connectionConfig.getPort())) {
            return "端口号为空";
        }
        if (StringUtils.isBlank(connectionConfig.getUserName())) {
            return "用户名为空";
        }
        if (StringUtils.isBlank(connectionConfig.getPassword())) {
            return "密码为空";
        }
        if (StringUtils.isBlank(connectionConfig.getSchemaName())) {
            return "数据库名称为空";
        }
        if (StringUtils.isBlank(connectionConfig.getEncoding())) {
            return "编码类型为空";
        }
        return null;
    }

}
