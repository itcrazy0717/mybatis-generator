package com.itcrazy.mybatis.generator;

import java.net.URL;
import java.util.Objects;

import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.controller.MainApplicationController;
import com.itcrazy.mybatis.generator.util.SqliteUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static com.itcrazy.mybatis.generator.constant.CommonConstants.MAIN_ICON_URL;

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
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(MAIN_ICON_URL))));
        primaryStage.setResizable(true);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        MainApplicationController controller = fxmlLoader.getController();
        controller.setMainStage(primaryStage);
    }

    public static void main(String[] args) {
        String version = System.getProperty("java.version");
        if (StringUtils.isBlank(version)) {
            showMessageBox("无JDK运行环境", "请使用小于jdk1.8.0_451的jdk版本运行此软件，高版本jdk不再内置javaFx，需自行下载配置");
            return;
        }
        // jdk主版本号
        int jdkVersion = Integer.parseInt(version.substring(2, 3));
        // jdk更新版本号
        int jdkUpdateVersion = Integer.parseInt(version.substring(6));
        // jdk版本校验
        // jdk8 小于451的版本才内置javaFx否则需要手动下载javaFx
        // https://www.oracle.com/javase/javafx/?f_link_type=f_linkinlinenote&flow_extra=eyJpbmxpbmVfZGlzcGxheV9wb3NpdGlvbiI6MCwiZG9jX3Bvc2l0aW9uIjowLCJkb2NfaWQiOiIxMWZmZjlkMmM4YTUzZWExLTQxY2ZmOGU4Yjc3NDYwOWIifQ%3D%3D
        if (jdkVersion == 8 && jdkUpdateVersion < 451) {
            launch(args);
        } else {
            showMessageBox("JDK版本错误", "请使用小于jdk1.8.0_451的jdk版本运行此软件，高版本jdk不再内置javaFx，需自行下载配置");
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
