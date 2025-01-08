package com.itcrazy.mybatis.generator.controller;

import java.io.File;
import java.net.URL;
import java.sql.SQLRecoverableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.IgnoredColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.constant.IconConstants;
import com.itcrazy.mybatis.generator.enums.FxmlPageEnum;
import com.itcrazy.mybatis.generator.model.DatabaseConnectionConfig;
import com.itcrazy.mybatis.generator.model.MybatisGeneratorTemplate;
import com.itcrazy.mybatis.generator.model.TableColumn;
import com.itcrazy.mybatis.generator.util.DataBaseStringUtil;
import com.itcrazy.mybatis.generator.util.DataBaseUtil;
import com.itcrazy.mybatis.generator.util.MessageTipsUtil;
import com.itcrazy.mybatis.generator.util.MybatisCodeGenerateUtil;
import com.itcrazy.mybatis.generator.util.SqliteUtil;
import com.itcrazy.mybatis.generator.window.ShowProgressCallback;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

/**
 * @author: itcrazy0717
 * @version: $ MainApplicationController.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class MainApplicationController extends BaseFxmlPageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplicationController.class);

    private static final String FOLDER_NO_EXIST = "部分目录不存在，是否创建";

    @FXML
    private Label connectionLabel;
    @FXML
    private Label configsLabel;
    @FXML
    private TextField modelTargetPackage;
    @FXML
    private TextField mapperTargetPackage;
    @FXML
    private TextField daoTargetPackage;
    @FXML
    private TextField paramTargetPackage;
    @FXML
    private TextField tableNameField;
    @FXML
    private TextField domainObjectNameField;
    @FXML
    private TextField modelAndDaoInterfaceTargetProject;
    @FXML
    private TextField mappingTargetProject;
    @FXML
    private TextField projectFolderField;
    @FXML
    private TreeView<String> dataBaseViewTree;

    /**
     * 当前选择的数据库
     */
    private DatabaseConnectionConfig selectedDatabaseConfig;

    /**
     * 当前选择的表名
     */
    private String tableName;

    private List<IgnoredColumn> ignoredColumns;

    private List<ColumnOverride> columnOverrides;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ImageView dbImage = new ImageView(IconConstants.COMPUTER_ICON_URL);
        dbImage.setFitHeight(40);
        dbImage.setFitWidth(40);
        connectionLabel.setGraphic(dbImage);
        connectionLabel.setOnMouseClicked(event -> {
            DataBaseConnectionController controller = (DataBaseConnectionController) loadFxmlPage("新建数据库连接", FxmlPageEnum.NEW_DATABASE_CONNECTION, false);
            controller.setMainApplicationController(this);
            // 为窗口增加ico图标
            controller.getDialogStage().getIcons().add(new Image(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(IconConstants.COMPUTER_ICON_URL))));
            controller.showDialogStage();
        });
        ImageView configImage = new ImageView(IconConstants.CONFIG_ICON_URL);
        configImage.setFitHeight(40);
        configImage.setFitWidth(40);
        configsLabel.setGraphic(configImage);
        configsLabel.setOnMouseClicked(event -> {
            GenerateCodeTemplateController controller = (GenerateCodeTemplateController) loadFxmlPage("代码生成配置", FxmlPageEnum.GENERATE_CONFIG, false);
            controller.setMainApplicationController(this);
            // 为窗口增加ico图标
            controller.getDialogStage().getIcons().add(new Image(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(IconConstants.CONFIG_ICON_URL))));
            controller.showDialogStage();
        });

        dataBaseViewTree.setShowRoot(false);
        dataBaseViewTree.setRoot(new TreeItem<>());
        Callback<TreeView<String>, TreeCell<String>> defaultCellFactory = TextFieldTreeCell.forTreeView();
        dataBaseViewTree.setCellFactory((TreeView<String> tv) -> {
            TreeCell<String> cell = defaultCellFactory.call(tv);
            cell.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                int level = dataBaseViewTree.getTreeItemLevel(cell.getTreeItem());
                TreeCell<String> treeCell = (TreeCell<String>) event.getSource();
                TreeItem<String> treeItem = treeCell.getTreeItem();
                if (level == 1) {
                    final ContextMenu contextMenu = new ContextMenu();
                    MenuItem item1 = new MenuItem("关闭连接");
                    item1.setOnAction(event1 -> treeItem.getChildren().clear());
                    MenuItem item2 = new MenuItem("编辑连接");
                    item2.setOnAction(event1 -> {
                        DatabaseConnectionConfig selectedConfig = (DatabaseConnectionConfig) treeItem.getGraphic().getUserData();
                        DataBaseConnectionController controller = (DataBaseConnectionController) loadFxmlPage("编辑数据库连接", FxmlPageEnum.NEW_DATABASE_CONNECTION, false);
                        controller.setMainApplicationController(this);
                        controller.fillDataBaseConnectionConfig(selectedConfig);
                        controller.showDialogStage();
                    });
                    MenuItem item3 = new MenuItem("删除连接");
                    item3.setOnAction(event1 -> {
                        DatabaseConnectionConfig selectedConfig = (DatabaseConnectionConfig) treeItem.getGraphic().getUserData();
                        try {
                            SqliteUtil.deleteDatabaseConnectionConfig(selectedConfig);
                            this.loadDataBaseViewList();
                        } catch (Exception e) {
                            MessageTipsUtil.showErrorInfo("Delete connection failed! Reason: " + e.getMessage());
                        }
                    });
                    contextMenu.getItems().addAll(item1, item2, item3);
                    cell.setContextMenu(contextMenu);
                }
                if (event.getClickCount() == 2) {
                    treeItem.setExpanded(true);
                    if (level == 1) {
                        System.out.println("index: " + dataBaseViewTree.getSelectionModel().getSelectedIndex());
                        DatabaseConnectionConfig selectedConfig = (DatabaseConnectionConfig) treeItem.getGraphic().getUserData();
                        try {
                            List<String> tableNameList = DataBaseUtil.getTableNameList(selectedConfig);
                            if (CollectionUtils.isNotEmpty(tableNameList)) {
                                ObservableList<TreeItem<String>> children = cell.getTreeItem().getChildren();
                                children.clear();
                                for (String tableName : tableNameList) {
                                    TreeItem<String> newTreeItem = new TreeItem<>();
                                    ImageView imageView = new ImageView(IconConstants.TABLE_ICON_URL);
                                    imageView.setFitHeight(16);
                                    imageView.setFitWidth(16);
                                    newTreeItem.setGraphic(imageView);
                                    newTreeItem.setValue(tableName);
                                    children.add(newTreeItem);
                                }
                            }
                        } catch (SQLRecoverableException e) {
                            LOGGER.error(e.getMessage(), e);
                            MessageTipsUtil.showErrorInfo("数据库连接超时");
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                            MessageTipsUtil.showErrorInfo(e.getMessage());
                        }
                    } else if (level == 2) { // left DB tree level3
                        String tableName = treeCell.getTreeItem().getValue();
                        selectedDatabaseConfig = (DatabaseConnectionConfig) treeItem.getParent().getGraphic().getUserData();
                        this.tableName = tableName;
                        tableNameField.setText(tableName);
                        domainObjectNameField.setText(DataBaseStringUtil.tableNameToCamelStyle(tableName) + "DO");
                    }
                }
            });
            return cell;
        });
        loadDataBaseViewList();
    }

    /**
     * 导入数据库视图列表
     * by itcrazy0717
     */
    public void loadDataBaseViewList() {
        TreeItem<String> rootTreeItem = dataBaseViewTree.getRoot();
        rootTreeItem.getChildren().clear();
        try {
            List<DatabaseConnectionConfig> dbConfigList = SqliteUtil.loadDatabaseConnectionConfig();
            for (DatabaseConnectionConfig dbConfig : dbConfigList) {
                TreeItem<String> treeItem = new TreeItem<>();
                treeItem.setValue(dbConfig.getName());
                ImageView dbImage = new ImageView(IconConstants.COMPUTER_ICON_URL);
                dbImage.setFitHeight(16);
                dbImage.setFitWidth(16);
                dbImage.setUserData(dbConfig);
                treeItem.setGraphic(dbImage);
                rootTreeItem.getChildren().add(treeItem);
            }
        } catch (Exception e) {
            LOGGER.error("connect db failed", e);
            MessageTipsUtil.showErrorInfo(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    @FXML
    public void chooseProjectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedFolder = directoryChooser.showDialog(getMainStage());
        if (selectedFolder != null) {
            projectFolderField.setText(selectedFolder.getAbsolutePath());
        }
    }

    @FXML
    public void generateCode() {
        if (StringUtils.isBlank(tableName)) {
            MessageTipsUtil.showWarnInfo("请先在左侧选择数据库表");
            return;
        }
        String validateResult = validateConfigValue();
        if (validateResult != null) {
            MessageTipsUtil.showErrorInfo(validateResult);
            return;
        }
        MybatisGeneratorTemplate generatorConfig = buildGeneratorTemplateContent();
        if (!checkDirs(generatorConfig)) {
            return;
        }

        ShowProgressCallback progressCallback = new ShowProgressCallback(Alert.AlertType.INFORMATION);
        MybatisCodeGenerateUtil.loadConfig(generatorConfig, selectedDatabaseConfig, progressCallback, ignoredColumns, columnOverrides);
        progressCallback.show();
        try {
            MybatisCodeGenerateUtil.generateCode();
        } catch (Exception e) {
            LOGGER.error("generate code failed", e);
            MessageTipsUtil.showErrorInfo(e.getMessage());
        }
    }

    /**
     * 校验配置值
     * by itcrazy0717
     *
     * @return
     */
    private String validateConfigValue() {
        if (StringUtils.isBlank(domainObjectNameField.getText())) {
            return "实体类名不能为空";
        }
        if (StringUtils.isBlank(projectFolderField.getText())) {
            return "项目目录不能为空";
        }
        if (StringUtils.isBlank(modelAndDaoInterfaceTargetProject.getText())) {
            return "实体与接口对象存放目录为空";
        }
        if (StringUtils.isBlank(modelTargetPackage.getText())) {
            return "实体包全名为空";
        }
        if (StringUtils.isBlank(daoTargetPackage.getText())) {
            return "DAO接口包全名为空";
        }
        if (StringUtils.isBlank(mappingTargetProject.getText())) {
            return "XML文件存放目录为空";
        }
        if (StringUtils.isBlank(mapperTargetPackage.getText())) {
            return "XML文件包全名为空";
        }
        return null;
    }

    /**
     * 保存生成代码模板
     * by itcrazy0717
     */
    @FXML
    public void saveGenerateCodeTemplate() {
        String validateResult = validateConfigValue();
        if (validateResult != null) {
            MessageTipsUtil.showErrorInfo(validateResult);
            return;
        }
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("保存当前配置");
        dialog.setContentText("请输入配置名称");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String templateName = result.get();
            if (StringUtils.isBlank(templateName)) {
                MessageTipsUtil.showErrorInfo("名称不能为空");
                return;
            }
            try {
                boolean exist = SqliteUtil.existGeneratorTemplate(templateName);
                if (exist) {
                    MessageTipsUtil.showErrorInfo("已存在相同名称的配置");
                    return;
                }
                MybatisGeneratorTemplate generatorTemplate = buildGeneratorTemplateContent();
                generatorTemplate.setName(templateName);
                SqliteUtil.saveGeneratorTemplate(generatorTemplate);
            } catch (Exception e) {
                MessageTipsUtil.showErrorInfo("配置保存异常，请检查必填项是否完整");
            }
        }
    }

    /**
     * 构建生成器模板内容
     * by itcrazy0717
     *
     * @return
     */
    public MybatisGeneratorTemplate buildGeneratorTemplateContent() {
        MybatisGeneratorTemplate config = new MybatisGeneratorTemplate();
        config.setProjectFolder(projectFolderField.getText());
        config.setModelPackage(modelTargetPackage.getText());
        config.setModelAndDaoInterfacePackageTargetFolder(modelAndDaoInterfaceTargetProject.getText());
        config.setDaoPackage(daoTargetPackage.getText());
        config.setMapperName(DataBaseStringUtil.tableNameToCamelStyle(tableName) + "DAO");
        config.setMapperXMLPackage(mapperTargetPackage.getText());
        config.setMapperXMLTargetFolder(mappingTargetProject.getText());
        config.setTableName(tableNameField.getText());
        config.setDomainObjectName(buildDomainObjectName(domainObjectNameField.getText()));
        config.setParamModelPackage(paramTargetPackage.getText());
        return config;
    }

    /**
     * 组装代码生成配置
     * by itcrazy0717
     *
     * @param template
     */
    public void assembleGeneratorTemplate(MybatisGeneratorTemplate template) {
        projectFolderField.setText(template.getProjectFolder());
        modelTargetPackage.setText(template.getModelPackage());
        modelAndDaoInterfaceTargetProject.setText(template.getModelAndDaoInterfacePackageTargetFolder());
        daoTargetPackage.setText(template.getDaoPackage());
        paramTargetPackage.setText(template.getParamModelPackage());
        mapperTargetPackage.setText(template.getMapperXMLPackage());
        mappingTargetProject.setText(template.getMapperXMLTargetFolder());
    }

    @FXML
    public void openTableColumnCustomizationPage() {
        if (StringUtils.isBlank(tableName)) {
            MessageTipsUtil.showWarnInfo("请先在左侧选择数据库表");
            return;
        }
        SelectTableColumnController controller = (SelectTableColumnController) loadFxmlPage("定制列", FxmlPageEnum.SELECT_TABLE_COLUMN, true);
        // 为定制项窗口增加ico图标
        controller.getDialogStage().getIcons().add(new Image(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(IconConstants.TABLE_ICON_URL))));
        controller.setMainApplicationController(this);
        try {
            // If select same schema and another table, update table data
            if (!tableName.equals(controller.getTableName())) {
                List<TableColumn> tableColumns = DataBaseUtil.getTableColumns(selectedDatabaseConfig, tableName);
                controller.setColumnList(FXCollections.observableList(tableColumns));
                controller.setTableName(tableName);
            }
            controller.showDialogStage();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            MessageTipsUtil.showErrorInfo(e.getMessage());
        }
    }

    public void setIgnoredColumns(List<IgnoredColumn> ignoredColumns) {
        this.ignoredColumns = ignoredColumns;
    }

    public void setColumnOverrides(List<ColumnOverride> columnOverrides) {
        this.columnOverrides = columnOverrides;
    }

    /**
     * 检查并创建不存在的文件夹
     *
     * @return
     */
    private boolean checkDirs(MybatisGeneratorTemplate config) {
        List<String> dirs = new ArrayList<>();
        dirs.add(config.getProjectFolder());
        dirs.add(FilenameUtils.normalize(config.getProjectFolder().concat("/").concat(config.getModelAndDaoInterfacePackageTargetFolder())));
        dirs.add(FilenameUtils.normalize(config.getProjectFolder().concat("/").concat(config.getMapperXMLTargetFolder())));
        boolean haveNotExistFolder = false;
        for (String dir : dirs) {
            File file = new File(dir);
            if (!file.exists()) {
                haveNotExistFolder = true;
            }
        }
        if (haveNotExistFolder) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText(FOLDER_NO_EXIST);
            Optional<ButtonType> optional = alert.showAndWait();
            if (optional.isPresent()) {
                if (ButtonType.OK == optional.get()) {
                    try {
                        for (String dir : dirs) {
                            FileUtils.forceMkdir(new File(dir));
                        }
                        return true;
                    } catch (Exception e) {
                        MessageTipsUtil.showErrorInfo("创建目录失败，请检查目录是否是文件而非目录");
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 构建实体类名-自动补齐DO后缀
     * by itcrazy0717
     *
     * @param domainObjecName
     * @return
     */
    private String buildDomainObjectName(String domainObjecName) {
        String result = DataBaseStringUtil.tableNameToCamelStyle(domainObjecName);
        if (StringUtils.isBlank(result)) {
            throw new RuntimeException("实体类名称为空");
        }
        // 判断实体类名是否以DO结尾，如果不是则补齐
        Pattern pattern = Pattern.compile("DO$");
        Matcher matcher = pattern.matcher(domainObjecName);
        if (!matcher.find()) {
            return domainObjecName + "DO";
        }
        return domainObjecName;
    }
}
