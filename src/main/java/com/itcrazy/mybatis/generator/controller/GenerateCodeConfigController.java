package com.itcrazy.mybatis.generator.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.model.MybatisCodeGenerateConfig;
import com.itcrazy.mybatis.generator.util.LocalSqliteUtil;
import com.itcrazy.mybatis.generator.view.AlertUtil;

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
public class GenerateCodeConfigController extends BaseFXController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateCodeConfigController.class);

    @FXML
    private TableView<MybatisCodeGenerateConfig> configTable;

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
                        Button btn1 = new Button("应用");
                        Button btn2 = new Button("删除");
                        HBox hBox = new HBox();
                        hBox.setSpacing(10);
                        hBox.getChildren().add(btn1);
                        hBox.getChildren().add(btn2);
                        btn1.setOnAction(event -> {
                            try {
                                // 应用配置
                                MybatisCodeGenerateConfig generatorConfig = LocalSqliteUtil.loadGeneratorConfig(item.toString());
                                mainApplicationController.assembleCodeGenerateConfig(generatorConfig);
                                controller.closeDialogStage();
                            } catch (Exception e) {
                                AlertUtil.showErrorAlert(e.getMessage());
                            }
                        });
                        btn2.setOnAction(event -> {
                            try {
                                // 删除配置
                                LOGGER.debug("item: {}", item);
                                LocalSqliteUtil.deleteCodeGenerateConfig(item.toString());
                                refreshTableView();
                            } catch (Exception e) {
                                AlertUtil.showErrorAlert(e.getMessage());
                            }
                        });
                        setGraphic(hBox);
                    }
                }
            };
        });
        refreshTableView();
    }

    public void refreshTableView() {
        try {
            List<MybatisCodeGenerateConfig> configs = LocalSqliteUtil.loadGeneratorConfigs();
            configTable.setItems(FXCollections.observableList(configs));
        } catch (Exception e) {
            AlertUtil.showErrorAlert(e.getMessage());
        }
    }

    void setMainApplicationController(MainApplicationController mainApplicationController) {
        this.mainApplicationController = mainApplicationController;
    }

}
