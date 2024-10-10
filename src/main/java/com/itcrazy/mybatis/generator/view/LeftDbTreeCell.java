package com.itcrazy.mybatis.generator.view;

import java.lang.ref.WeakReference;

import com.itcrazy.mybatis.generator.model.DatabaseConfig;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;

/**
 * @author: itcrazy0717
 * @version: $ LeftDbTreeCell.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class LeftDbTreeCell extends TreeCell<DatabaseConfig> {
    private HBox hbox;

    private WeakReference<TreeItem<DatabaseConfig>> treeItemRef;

    private InvalidationListener treeItemGraphicListener = observable -> updateDisplay(getItem(), isEmpty());

    private InvalidationListener treeItemListener = new InvalidationListener() {

        @Override
        public void invalidated(Observable observable) {
            TreeItem<DatabaseConfig> oldTreeItem = treeItemRef == null ? null : treeItemRef.get();
            if (oldTreeItem != null) {
                oldTreeItem.graphicProperty().removeListener(weakTreeItemGraphicListener);
            }

            TreeItem<DatabaseConfig> newTreeItem = getTreeItem();
            if (newTreeItem != null) {
                newTreeItem.graphicProperty().addListener(weakTreeItemGraphicListener);
                treeItemRef = new WeakReference<>(newTreeItem);
            }
        }
    };

    private WeakInvalidationListener weakTreeItemGraphicListener =
            new WeakInvalidationListener(treeItemGraphicListener);

    private WeakInvalidationListener weakTreeItemListener =
            new WeakInvalidationListener(treeItemListener);

    public LeftDbTreeCell() {
        treeItemProperty().addListener(weakTreeItemListener);

        if (getTreeItem() != null) {
            getTreeItem().graphicProperty().addListener(weakTreeItemGraphicListener);
        }
    }

    void updateDisplay(DatabaseConfig item, boolean empty) {
        if (item == null || empty) {
            hbox = null;
            setText(null);
            setGraphic(null);
        } else {
            // update the graphic if one is set in the TreeItem
            TreeItem<DatabaseConfig> treeItem = getTreeItem();
            if (treeItem != null && treeItem.getGraphic() != null) {
                hbox = null;
                setText(item.toString());
                setGraphic(treeItem.getGraphic());
            } else {
                hbox = null;
                setText(item.getName());
                setGraphic(null);
            }
        }
    }

    @Override
    public void updateItem(DatabaseConfig item, boolean empty) {
        super.updateItem(item, empty);
        updateDisplay(item, empty);
    }
}
