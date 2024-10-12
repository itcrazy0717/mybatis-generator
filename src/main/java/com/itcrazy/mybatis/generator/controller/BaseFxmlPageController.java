package com.itcrazy.mybatis.generator.controller;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itcrazy.mybatis.generator.enums.FxmlPageEnum;
import com.itcrazy.mybatis.generator.util.MessageTipsUtil;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author: itcrazy0717
 * @version: $ BaseFxmlPageController.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public abstract class BaseFxmlPageController implements Initializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseFxmlPageController.class);

	/**
	 * 主窗口
	 */
	private Stage mainStage;

	/**
	 * 弹窗
	 */
	private Stage dialogStage;


	private static final Map<FxmlPageEnum, SoftReference<? extends BaseFxmlPageController>> FXML_PAGE_CONTROLLER_MAP = new HashMap<>();

	/**
	 * 导入对应page，并进行缓存
	 * by itcrazy0717
	 *
	 * @param title
	 * @param fxmlPage
	 * @param cache
	 * @return
	 */
    public BaseFxmlPageController loadFxmlPage(String title, FxmlPageEnum fxmlPage, boolean cache) {
        SoftReference<? extends BaseFxmlPageController> fxmlPageReference = FXML_PAGE_CONTROLLER_MAP.get(fxmlPage);
	    if (cache && Objects.nonNull(fxmlPageReference)) {
		    return fxmlPageReference.get();
	    }
        URL skeletonResource = Thread.currentThread().getContextClassLoader().getResource(fxmlPage.getFxml());
        FXMLLoader loader = new FXMLLoader(skeletonResource);
        Parent loginNode;
        try {
            loginNode = loader.load();
            BaseFxmlPageController controller = loader.getController();
            dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(getMainStage());
            dialogStage.setScene(new Scene(loginNode));
            dialogStage.setMaximized(false);
            dialogStage.setResizable(false);
            dialogStage.show();
            controller.setDialogStage(dialogStage);
            // put into cache map
            SoftReference<BaseFxmlPageController> softReference = new SoftReference<>(controller);
            FXML_PAGE_CONTROLLER_MAP.put(fxmlPage, softReference);
            return controller;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            MessageTipsUtil.showErrorInfo(e.getMessage());
        }
        return null;
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

	public void showDialogStage() {
		if (Objects.nonNull(dialogStage)) {
			dialogStage.show();
		}
	}

	public void closeDialogStage() {
		if (Objects.nonNull(dialogStage)) {
			dialogStage.close();
		}
	}

}
