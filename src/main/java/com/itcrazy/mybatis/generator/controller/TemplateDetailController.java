package com.itcrazy.mybatis.generator.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.model.MybatisGeneratorTemplate;
import com.itcrazy.mybatis.generator.util.ShowMessageUtil;
import com.itcrazy.mybatis.generator.util.SqliteUtil;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * @author: itcrazy0717
 * @version: $ TemplateDetailController.java,v0.1 2025-01-09 10:42 itcrazy0717 Exp $
 * @description:
 */
public class TemplateDetailController extends BaseFxmlPageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateDetailController.class);

    /**
     * 配置名称
     */
    @FXML
    private TextField templateName;

    /**
     * 项目所在目录
     */
    @FXML
    private TextField projectFolder;

    /**
     * 实体与接口对象存放目录
     */
    @FXML
    private TextField modelAndDaoInterfaceTargetProject;

    /**
     * 实体包全名
     */
    @FXML
    private TextField modelTargetPackage;

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
     * XML文件存放目录
     */
    @FXML
    private TextField mappingTargetProject;

    /**
     * XML文件包全名
     */
    @FXML
    private TextField mapperTargetPackage;

    /**
     * 原始配置名称
     */
    private String originalTemplatName;

    /**
     * 配置列表视图
     */
    private TableView<MybatisGeneratorTemplate> codeGenerateView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * 组装代码生成配置
     * by itcrazy0717
     *
     * @param template
     */
    public void assembleGeneratorTemplate(MybatisGeneratorTemplate template) {
        templateName.setText(template.getName());
        projectFolder.setText(template.getProjectFolder());
        modelTargetPackage.setText(template.getModelPackage());
        modelAndDaoInterfaceTargetProject.setText(template.getModelAndDaoInterfacePackageTargetFolder());
        daoTargetPackage.setText(template.getDaoPackage());
        paramTargetPackage.setText(template.getParamModelPackage());
        mapperTargetPackage.setText(template.getMapperXMLPackage());
        mappingTargetProject.setText(template.getMapperXMLTargetFolder());
        originalTemplatName = template.getName();
    }

    /**
     * 修改配置
     * by itcrazy0717
     */
    @FXML
    public void updateTemplate() {
        String validateResult = validateTemplateValue();
        if (StringUtils.isNotBlank(validateResult)) {
            ShowMessageUtil.showErrorInfo(validateResult);
            return;
        }
        try {
            // 设置了新配置名称，则进行校验
            if (!StringUtils.equals(originalTemplatName, templateName.getText())) {
                boolean exist = SqliteUtil.existGeneratorTemplate(templateName.getText());
                if (exist) {
                    ShowMessageUtil.showErrorInfo("已存在相同名称的配置");
                    return;
                }
            }
            MybatisGeneratorTemplate template = buildTemplate();
            SqliteUtil.updateGeneratorTemplate(template, originalTemplatName);
            ShowMessageUtil.showNormalInfo("配置更新成功");
            this.closeDialogStage();
            refreshTableView();
        } catch (Exception e) {
            LOGGER.error("update_template_detail_error", e);
            ShowMessageUtil.showErrorInfo(e.getMessage());
        }
    }

    /**
     * 构建生成器模板内容
     * by itcrazy0717
     *
     * @return
     */
    public MybatisGeneratorTemplate buildTemplate() {
        MybatisGeneratorTemplate template = new MybatisGeneratorTemplate();
        template.setName(templateName.getText());
        template.setProjectFolder(projectFolder.getText());
        template.setModelAndDaoInterfacePackageTargetFolder(modelAndDaoInterfaceTargetProject.getText());
        template.setModelPackage(modelTargetPackage.getText());
        template.setDaoPackage(daoTargetPackage.getText());
        template.setParamModelPackage(paramTargetPackage.getText());
        template.setMapperXMLTargetFolder(mappingTargetProject.getText());
        template.setMapperXMLPackage(mapperTargetPackage.getText());
        return template;
    }

    /**
     * 校验配置值
     * by itcrazy0717
     *
     * @return
     */
    private String validateTemplateValue() {
        if (StringUtils.isBlank(templateName.getText())) {
            return "配置名称为空";
        }
        if (StringUtils.isBlank(projectFolder.getText())) {
            return "项目所在目录为空";
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
     * 设置父视图
     * 用于列表刷新
     * by itcrazy0717
     *
     * @param codeGenerateView
     */
    public void setCodeGenerateView(TableView<MybatisGeneratorTemplate> codeGenerateView) {
        this.codeGenerateView = codeGenerateView;
    }

    /**
     * 刷新视图
     * by itcrazy0717
     */
    public void refreshTableView() {
        try {
            List<MybatisGeneratorTemplate> configs = SqliteUtil.loadGeneratorTemplateList();
            codeGenerateView.setItems(FXCollections.observableList(configs));
        } catch (Exception e) {
            LOGGER.error("refresh_table_view_error", e);
            ShowMessageUtil.showErrorInfo(e.getMessage());
        }
    }

}
