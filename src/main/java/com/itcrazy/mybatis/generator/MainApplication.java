package com.itcrazy.mybatis.generator;

import java.net.URL;
import java.util.Objects;

import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.constant.IconConstants;
import com.itcrazy.mybatis.generator.controller.MainApplicationController;
import com.itcrazy.mybatis.generator.util.SqliteUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * @author: itcrazy0717
 * @version: $ MainApplication.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class MainApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        SqliteUtil.createConfigSqlite();
        URL url = Thread.currentThread().getContextClassLoader().getResource("fxml/mainApplication.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(url);
        Parent root = fxmlLoader.load();
        // 调整窗口ico
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(IconConstants.MAIN_ICON_URL))));
        primaryStage.setResizable(true);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        MainApplicationController controller = fxmlLoader.getController();
        controller.setMainStage(primaryStage);
    }

    public static void main(String[] args) {
        String version = System.getProperty("java.version");
        if (StringUtils.isBlank(version)) {
            showMessageBox("无JDK运行环境", "请安装JDK1.8再运行此软件，该软件不支持过高的JDK版本，使用JDK1.8运行效果最佳");
            return;
        }
        int jdkVersion = Integer.parseInt(version.substring(2, 3));
        // jdk版本校验
        if (jdkVersion >= 8 && jdkVersion <= 11 && Integer.parseInt(version.substring(6)) >= 60) {
            launch(args);
        } else {
            showMessageBox("JDK版本错误", "JDK的版本不能低于1.8.0.60，请升级至最近的JDK1.8再运行此软件，该软件不支持过高的JDK版本，使用JDK1.8运行效果最佳");
        }
    }

    /**
     * 显示提示信息
     * by itcrazy0717
     *
     * @param title
     * @param showText
     */
    private static void showMessageBox(String title, String showText) {
        JFrame frame = new JFrame(title);
        if (StringUtils.equals("无JDK运行环境", title)) {
            frame.setSize(600, 100);
        } else {
            frame.setSize(800, 100);
        }
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        JLabel label = new JLabel(showText);
        panel.add(label);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
