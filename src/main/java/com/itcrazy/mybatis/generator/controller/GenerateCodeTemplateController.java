package com.itcrazy.mybatis.generator.controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.model.MybatisGeneratorTemplate;
import com.itcrazy.mybatis.generator.util.MessageTipsUtil;
import com.itcrazy.mybatis.generator.util.SqliteUtil;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

/**
 * @author: itcrazy0717
 * @version: $ GenerateCodeTemplateController.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class GenerateCodeTemplateController extends BaseFxmlPageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateCodeTemplateController.class);

    @FXML
    private TableView<MybatisGeneratorTemplate> codeGenerateView;

    @FXML
    private TableColumn nameColumn;

    @FXML
    private TableColumn opsColumn;

    private MainApplicationController mainApplicationController;

    private GenerateCodeTemplateController controller;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        controller = this;
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        // 自定义操作列
        opsColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        opsColumn.setCellFactory(cell -> {
            return new TableCell() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Button btnApply = new Button("应用");
                        Button btnModifyName = new Button("修改配置名称");
                        Button btnDelete = new Button("删除");
                        HBox hBox = new HBox();
                        hBox.setSpacing(10);
                        hBox.getChildren().add(btnApply);
                        hBox.getChildren().add(btnModifyName);
                        hBox.getChildren().add(btnDelete);
                        btnApply.setOnAction(event -> {
                            try {
                                // 应用配置
                                MybatisGeneratorTemplate template = SqliteUtil.loadGeneratorTemplateByName(item.toString());
                                mainApplicationController.assembleGeneratorTemplate(template);
                                controller.closeDialogStage();
                            } catch (Exception e) {
                                MessageTipsUtil.showErrorInfo(e.getMessage());
                            }
                        });
                        btnModifyName.setOnAction(event -> {
                            TextInputDialog dialog = new TextInputDialog(item.toString());
                            dialog.setTitle("修改配置名称");
                            dialog.setContentText("请输入配置名称");
                            Optional<String> result = dialog.showAndWait();
                            if (result.isPresent()) {
                                String newTemplateName = result.get();
                                if (StringUtils.isBlank(newTemplateName)) {
                                    MessageTipsUtil.showErrorInfo("配置名称不能为空");
                                    return;
                                }
                                if (StringUtils.equals(item.toString(), newTemplateName)) {
                                    MessageTipsUtil.showErrorInfo("配置名称未更改");
                                    return;
                                }
                                try {
                                    boolean exist = SqliteUtil.existGeneratorTemplate(newTemplateName);
                                    if (exist) {
                                        MessageTipsUtil.showErrorInfo("已存在相同名称的配置");
                                        return;
                                    }
                                    SqliteUtil.updateGeneratorTemplateName(newTemplateName, item.toString());
                                    refreshTableView();
                                } catch (Exception e) {
                                    MessageTipsUtil.showErrorInfo(e.getMessage());
                                }
                            }
                        });
                        btnDelete.setOnAction(event -> {
                            try {
                                // 二次确认弹窗
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("确认");
                                alert.setHeaderText("确认操作");
                                alert.setContentText("确定要删除当前配置");
                                ButtonType buttonTypeOk = new ButtonType("是", ButtonBar.ButtonData.OK_DONE);
                                ButtonType buttonTypeCancel = new ButtonType("否", ButtonBar.ButtonData.CANCEL_CLOSE);
                                alert.getButtonTypes().setAll(buttonTypeOk, buttonTypeCancel);

                                ButtonType result = alert.showAndWait().orElse(buttonTypeCancel);
                                // 确认后才进行删除
                                if (result == buttonTypeOk) {
                                    SqliteUtil.deleteCodeGenerateConfigByName(item.toString());
                                    refreshTableView();
                                }
                            } catch (Exception e) {
                                MessageTipsUtil.showErrorInfo(e.getMessage());
                            }
                        });
                        setGraphic(hBox);
                    }
                }
            };
        });
        refreshTableView();
    }

    /**
     * 刷新表视图
     * by itcrazy0717
     */
    public void refreshTableView() {
        try {
            List<MybatisGeneratorTemplate> configs = SqliteUtil.loadGeneratorTemplateList();
            codeGenerateView.setItems(FXCollections.observableList(configs));
        } catch (Exception e) {
            MessageTipsUtil.showErrorInfo(e.getMessage());
        }
    }

    public void setMainApplicationController(MainApplicationController mainApplicationController) {
        this.mainApplicationController = mainApplicationController;
    }

}
