package com.itcrazy.mybatis.generator.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.itcrazy.mybatis.generator.model.MybatisGeneratorTemplate;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * @author: dengxin.chen
 * @version: $ TemplateDetailController.java,v0.1 2025-01-09 10:42 dengxin.chen Exp $
 * @description:
 */
public class TemplateDetailController extends BaseFxmlPageController {

    /**
     * 项目所在目录
     */
    @FXML
    private TextField projectFolderField;

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
        projectFolderField.setText(template.getProjectFolder());
        modelTargetPackage.setText(template.getModelPackage());
        modelAndDaoInterfaceTargetProject.setText(template.getModelAndDaoInterfacePackageTargetFolder());
        daoTargetPackage.setText(template.getDaoPackage());
        paramTargetPackage.setText(template.getParamModelPackage());
        mapperTargetPackage.setText(template.getMapperXMLPackage());
        mappingTargetProject.setText(template.getMapperXMLTargetFolder());
    }
}
