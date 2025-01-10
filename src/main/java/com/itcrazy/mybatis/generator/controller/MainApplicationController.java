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

    /**
     * 数据库连接label
     */
    @FXML
    private Label connectionLabel;

    /**
     * 生成代码模板配置label
     */
    @FXML
    private Label generatorTemplateLabel;

    /**
     * 实体包全名
     */
    @FXML
    private TextField modelTargetPackage;

    /**
     * XML文件包全名
     */
    @FXML
    private TextField mapperTargetPackage;

    /**
     * DAO接口包全名
     */
    @FXML
    private TextField daoTargetPackage;

    /**
     * 查询参数包全名(Param)
     */
    @FXML
    private TextField paramTargetPackage;

    /**
     * 表名
     */
    @FXML
    private TextField tableNameField;

    /**
     * 实体类名(xxxDO)
     */
    @FXML
    private TextField domainObjectNameField;

    /**
     * 实体与接口对象存放目录
     */
    @FXML
    private TextField modelAndDaoInterfaceTargetProject;

    /**
     * XML文件存放目录
     */
    @FXML
    private TextField mappingTargetProject;

    /**
     * 项目所在目录
     */
    @FXML
    private TextField projectFolderField;

    /**
     * 数据库树
     */
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
            DataBaseConnectionController controller = (DataBaseConnectionController) loadFxmlPage("新建数据库连接", FxmlPageEnum.DATABASE_CONNECTION, false);
            controller.setMainApplicationController(this);
            // 为窗口增加ico图标
            controller.getDialogStage().getIcons().add(new Image(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(IconConstants.COMPUTER_ICON_URL))));
            controller.showDialogStage();
        });
        ImageView configImage = new ImageView(IconConstants.CONFIG_ICON_URL);
        configImage.setFitHeight(40);
        configImage.setFitWidth(40);
        generatorTemplateLabel.setGraphic(configImage);
        generatorTemplateLabel.setOnMouseClicked(event -> {
            GenerateCodeTemplateController controller = (GenerateCodeTemplateController) loadFxmlPage("配置", FxmlPageEnum.GENERATE_CODE_TEMPLATE, false);
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
                // 第一层，数据库上的操作
                if (level == 1) {
                    final ContextMenu contextMenu = new ContextMenu();
                    // 关闭连接
                    MenuItem closeMenuItem = new MenuItem("关闭连接");
                    closeMenuItem.setOnAction(event1 -> treeItem.getChildren().clear());
                    // 编辑连接
                    MenuItem modifyMenuItem = new MenuItem("编辑连接");
                    modifyMenuItem.setOnAction(actionEvent -> {
                        DatabaseConnectionConfig selectedConfig = (DatabaseConnectionConfig) treeItem.getGraphic().getUserData();
                        DataBaseConnectionController controller = (DataBaseConnectionController) loadFxmlPage("编辑数据库连接", FxmlPageEnum.DATABASE_CONNECTION, false);
                        controller.setMainApplicationController(this);
                        controller.fillDataBaseConnectionConfig(selectedConfig);
                        controller.showDialogStage();
                    });
                    // 删除连接
                    MenuItem deleteMenuItem = new MenuItem("删除连接");
                    deleteMenuItem.setOnAction(actionEvent -> {
                        DatabaseConnectionConfig selectedConfig = (DatabaseConnectionConfig) treeItem.getGraphic().getUserData();
                        try {
                            SqliteUtil.deleteDatabaseConnectionConfig(selectedConfig);
                            this.loadDataBaseViewList();
                        } catch (Exception e) {
                            MessageTipsUtil.showErrorInfo("Delete connection failed! Reason: " + e.getMessage());
                        }
                    });
                    contextMenu.getItems().addAll(closeMenuItem, modifyMenuItem, deleteMenuItem);
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
            List<DatabaseConnectionConfig> connectionConfigList = SqliteUtil.loadDatabaseConnectionConfig();
            for (DatabaseConnectionConfig connectionConfig : connectionConfigList) {
                TreeItem<String> treeItem = new TreeItem<>();
                treeItem.setValue(connectionConfig.getName());
                ImageView dbImage = new ImageView(IconConstants.COMPUTER_ICON_URL);
                dbImage.setFitHeight(16);
                dbImage.setFitWidth(16);
                dbImage.setUserData(connectionConfig);
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
        String validateResult = validateGeneratorTemplateValue(true);
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
     * @param generateCode 是否是生成代码 true-是 false-不是
     * @return
     */
    private String validateGeneratorTemplateValue(boolean generateCode) {
        if (generateCode) {
            if (StringUtils.isBlank(tableNameField.getText())) {
                return "请先在左侧选择数据库表";
            }
            if (StringUtils.isBlank(domainObjectNameField.getText())) {
                return "实体类名不能为空";
            }
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
        String validateResult = validateGeneratorTemplateValue(false);
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
            templateName = templateName.trim();
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
        MybatisGeneratorTemplate template = new MybatisGeneratorTemplate();
        template.setProjectFolder(projectFolderField.getText());
        template.setModelPackage(modelTargetPackage.getText());
        template.setModelAndDaoInterfacePackageTargetFolder(modelAndDaoInterfaceTargetProject.getText());
        template.setDaoPackage(daoTargetPackage.getText());
        template.setMapperName(DataBaseStringUtil.tableNameToCamelStyle(tableName) + "DAO");
        template.setMapperXMLPackage(mapperTargetPackage.getText());
        template.setMapperXMLTargetFolder(mappingTargetProject.getText());
        template.setTableName(tableNameField.getText());
        template.setDomainObjectName(buildDomainObjectName(domainObjectNameField.getText()));
        template.setParamModelPackage(paramTargetPackage.getText());
        return template;
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
