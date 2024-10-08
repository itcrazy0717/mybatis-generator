package com.itcrazy.mybatis.generator.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.collections.CollectionUtils;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.IgnoredColumn;

import com.itcrazy.mybatis.generator.dto.TableColumn;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

/**
 * @author: itcrazy0717
 * @version: $ SelectTableColumnController.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class SelectTableColumnController extends BaseFXController {

    @FXML
    private TableView<TableColumn> columnListView;
    @FXML
    private javafx.scene.control.TableColumn<TableColumn, Boolean> checkedColumn;
    @FXML
    private javafx.scene.control.TableColumn<TableColumn, String> columnNameColumn;
    @FXML
    private javafx.scene.control.TableColumn<TableColumn, String> jdbcTypeColumn;
    @FXML
    private javafx.scene.control.TableColumn<TableColumn, String> javaTypeColumn;
    @FXML
    private javafx.scene.control.TableColumn<TableColumn, String> propertyNameColumn;
    @FXML
    private javafx.scene.control.TableColumn<TableColumn, String> typeHandlerColumn;

    private MainApplicationController mainUIController;

    private String tableName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // cellvaluefactory
        checkedColumn.setCellValueFactory(new PropertyValueFactory<>("checked"));
        columnNameColumn.setCellValueFactory(new PropertyValueFactory<>("columnName"));
        jdbcTypeColumn.setCellValueFactory(new PropertyValueFactory<>("jdbcType"));
        propertyNameColumn.setCellValueFactory(new PropertyValueFactory<>("propertyName"));
        typeHandlerColumn.setCellValueFactory(new PropertyValueFactory<>("typeHandler"));
        // Cell Factory that customize how the cell should render
        checkedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkedColumn));
        javaTypeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        // handle commit event to save the user input data
        javaTypeColumn.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setJavaType(event.getNewValue());
        });
        propertyNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        propertyNameColumn.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setPropertyName(event.getNewValue());
        });
        typeHandlerColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        typeHandlerColumn.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setTypeHandle(event.getNewValue());
        });
    }

    @FXML
    public void ok() {
        ObservableList<TableColumn> items = columnListView.getItems();
        if (CollectionUtils.isNotEmpty(items)) {
            List<IgnoredColumn> ignoredColumns = new ArrayList<>();
            List<ColumnOverride> columnOverrides = new ArrayList<>();
            items.forEach(item -> {
                if (!item.getChecked()) {
                    IgnoredColumn ignoredColumn = new IgnoredColumn(item.getColumnName());
                    ignoredColumns.add(ignoredColumn);
                } else if (item.getTypeHandle() != null || item.getJavaType() != null || item.getPropertyName() != null) { // unchecked and have typeHandler value
                    ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());
                    columnOverride.setTypeHandler(item.getTypeHandle());
                    columnOverride.setJavaProperty(item.getPropertyName());
                    columnOverride.setJavaType(item.getJavaType());
                    columnOverrides.add(columnOverride);
                }
            });
            mainUIController.setIgnoredColumns(ignoredColumns);
            mainUIController.setColumnOverrides(columnOverrides);
        }
        getDialogStage().close();
    }

    @FXML
    public void cancel() {
        getDialogStage().close();
    }

    public void setColumnList(ObservableList<TableColumn> columns) {
        columnListView.setItems(columns);
    }

    public void setMainUIController(MainApplicationController mainUIController) {
        this.mainUIController = mainUIController;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
