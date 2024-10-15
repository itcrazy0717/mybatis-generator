package com.itcrazy.mybatis.generator.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.model.MybatisCodeGenerateConfig;
import com.itcrazy.mybatis.generator.util.LocalSqliteUtil;
import com.itcrazy.mybatis.generator.util.MessageTipsUtil;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

/**
 * @author: itcrazy0717
 * @version: $ GenerateCodeConfigController.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class GenerateCodeConfigController extends BaseFxmlPageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateCodeConfigController.class);

    @FXML
    private TableView<MybatisCodeGenerateConfig> codeGenerateView;

    @FXML
    private TableColumn nameColumn;

    @FXML
    private TableColumn opsColumn;

    private MainApplicationController mainApplicationController;

    private GenerateCodeConfigController controller;

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
                        Button btnDelete = new Button("删除");
                        HBox hBox = new HBox();
                        hBox.setSpacing(10);
                        hBox.getChildren().add(btnApply);
                        hBox.getChildren().add(btnDelete);
                        btnApply.setOnAction(event -> {
                            try {
                                // 应用配置
                                MybatisCodeGenerateConfig codeGenerateConfig = LocalSqliteUtil.loadCodeGenerateConfigByName(item.toString());
                                mainApplicationController.assembleCodeGenerateConfig(codeGenerateConfig);
                                controller.closeDialogStage();
                            } catch (Exception e) {
                                MessageTipsUtil.showErrorInfo(e.getMessage());
                            }
                        });
                        btnDelete.setOnAction(event -> {
                            try {
                                // 删除配置
                                LOGGER.debug("item: {}", item);
                                LocalSqliteUtil.deleteCodeGenerateConfigByName(item.toString());
                                refreshTableView();
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
            List<MybatisCodeGenerateConfig> configs = LocalSqliteUtil.loadCodeGenerateConfigList();
            codeGenerateView.setItems(FXCollections.observableList(configs));
        } catch (Exception e) {
            MessageTipsUtil.showErrorInfo(e.getMessage());
        }
    }

    public void setMainApplicationController(MainApplicationController mainApplicationController) {
        this.mainApplicationController = mainApplicationController;
    }

}
