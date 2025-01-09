package com.itcrazy.mybatis.generator.controller;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.constant.IconConstants;
import com.itcrazy.mybatis.generator.enums.FxmlPageEnum;
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
import javafx.scene.image.Image;
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
                    if (Objects.isNull(item) || empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Button btnApply = new Button("应用");
                        Button btnDetail = new Button("详情");
                        Button btnModifyName = new Button("修改名称");
                        Button btnDelete = new Button("删除");
                        HBox hBox = new HBox();
                        hBox.setSpacing(10);
                        hBox.getChildren().add(btnApply);
                        hBox.getChildren().add(btnDetail);
                        hBox.getChildren().add(btnModifyName);
                        hBox.getChildren().add(btnDelete);
                        String templateName = item.toString();
                        // 应用按钮响应事件
                        btnApply.setOnAction(event -> {
                            try {
                                // 应用配置
                                MybatisGeneratorTemplate template = SqliteUtil.loadGeneratorTemplateByName(templateName);
                                mainApplicationController.assembleGeneratorTemplate(template);
                                controller.closeDialogStage();
                            } catch (Exception e) {
                                MessageTipsUtil.showErrorInfo(e.getMessage());
                            }
                        });
                        // 详情按钮响应事件
                        btnDetail.setOnAction(event -> {
                            try {
                                TemplateDetailController detailController = (TemplateDetailController) loadFxmlPage("配置详情", FxmlPageEnum.TEMPLATE_DETAIL, false);
                                MybatisGeneratorTemplate template = SqliteUtil.loadGeneratorTemplateByName(templateName);
                                // 组装配置
                                detailController.assembleGeneratorTemplate(template);
                                // 为窗口增加ico图标
                                detailController.getDialogStage().getIcons().add(new Image(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(IconConstants.CONFIG_ICON_URL))));
                                detailController.showDialogStage();
                            } catch (Exception e) {
                                MessageTipsUtil.showErrorInfo(e.getMessage());
                            }
                        });

                        // 修改配置名称事件
                        btnModifyName.setOnAction(event -> {
                            TextInputDialog dialog = new TextInputDialog(templateName);
                            dialog.setTitle("修改配置名称");
                            dialog.setContentText("请输入配置名称");
                            Optional<String> result = dialog.showAndWait();
                            if (result.isPresent()) {
                                String newTemplateName = result.get();
                                if (StringUtils.isBlank(newTemplateName)) {
                                    MessageTipsUtil.showErrorInfo("配置名称不能为空");
                                    return;
                                }
                                if (StringUtils.equals(templateName, newTemplateName)) {
                                    MessageTipsUtil.showWarnInfo("配置名称未更改");
                                    return;
                                }
                                try {
                                    boolean exist = SqliteUtil.existGeneratorTemplate(newTemplateName);
                                    if (exist) {
                                        MessageTipsUtil.showErrorInfo("已存在相同名称的配置");
                                        return;
                                    }
                                    SqliteUtil.updateGeneratorTemplateName(newTemplateName, templateName);
                                    refreshTableView();
                                } catch (Exception e) {
                                    MessageTipsUtil.showErrorInfo(e.getMessage());
                                }
                            }
                        });
                        // 删除配置事件
                        btnDelete.setOnAction(event -> {
                            try {
                                // 二次确认弹窗
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("删除配置");
                                alert.setHeaderText("确认删除操作");
                                alert.setContentText("确定要删除当前配置");
                                ButtonType buttonTypeOk = new ButtonType("是", ButtonBar.ButtonData.OK_DONE);
                                ButtonType buttonTypeCancel = new ButtonType("否", ButtonBar.ButtonData.CANCEL_CLOSE);
                                alert.getButtonTypes().setAll(buttonTypeOk, buttonTypeCancel);

                                ButtonType result = alert.showAndWait().orElse(buttonTypeCancel);
                                // 确认后才进行删除
                                if (result == buttonTypeOk) {
                                    SqliteUtil.deleteGeneratorTemplateByName(templateName);
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
