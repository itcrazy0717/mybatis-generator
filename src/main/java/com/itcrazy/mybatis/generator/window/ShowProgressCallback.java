package com.itcrazy.mybatis.generator.window;

import org.mybatis.generator.api.ProgressCallback;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;

/**
 * @author: itcrazy0717
 * @version: $ ShowProgressCallback.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class ShowProgressCallback extends Alert implements ProgressCallback {

    private StringProperty progressText = new SimpleStringProperty();

    public ShowProgressCallback(AlertType alertType) {
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
        progressText.setValue("代码生成成功");
    }

    @Override
    public void checkCancel() throws InterruptedException {
    }
}
