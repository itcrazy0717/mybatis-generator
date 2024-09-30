package com.itcrazy.mybatis.generator.view;

import org.mybatis.generator.api.ProgressCallback;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;

/**
 * @author: dengxin.chen
 * @version: $ UIProgressCallback.java,v0.1 2024-09-30 17:15 dengxin.chen Exp $
 * @description:
 */
public class UIProgressCallback extends Alert implements ProgressCallback {

    private StringProperty progressText = new SimpleStringProperty();

    public UIProgressCallback(AlertType alertType) {
        super(alertType);
        this.contentTextProperty().bindBidirectional(progressText);
    }

    @Override
    public void introspectionStarted(int totalTasks) {
        progressText.setValue("开始代码检查");
    }

    @Override
    public void generationStarted(int totalTasks) {
        progressText.setValue("开始代码生成");
    }

    @Override
    public void saveStarted(int totalTasks) {
        progressText.setValue("开始保存生成的文件");
    }

    @Override
    public void startTask(String taskName) {
        progressText.setValue("代码生成任务开始");
    }

    @Override
    public void done() {
        progressText.setValue("代码生成完成");
    }

    @Override
    public void checkCancel() throws InterruptedException {
    }
}
