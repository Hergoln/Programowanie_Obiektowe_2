package sample.GUI;

import javafx.event.*;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;

/**
 * Controller for ShareButton, button used to share files via server
 */
public class ShareButton
{
    private final Button shareButton;
    private final TreeView fileTreeView;
    private final UsersList usersList;

    /**
     * @param bool set share button disable
     */
    public void setDisable(boolean bool)
    {
        shareButton.setDisable(bool);
    }

    /**
     * @param bool set shareButton visible
     */
    public void setVisible(boolean bool)
    {
        shareButton.setVisible(bool);
    }

    /**
     * sets *setOnAction* method for shareButton
     * @param value EventHandler to set as *setOnAction* method
     */
    public void setOnAction(EventHandler<ActionEvent> value)
    {
        shareButton.setOnAction(value);
    }

    /**
     * @return currently chosen user
     */
    public String selectedUser()
    {
        return usersList.GetChoice();
    }

    /**
     * @return currently chosen item int tree view
     */
    public File selectedFile()
    {
        return ((TreeItem<File>)fileTreeView.getFocusModel().getFocusedItem()).getValue();
    }

    public ShareButton(Button shareButton, TreeView fileTreeView, UsersList usersList) {
        this.shareButton = shareButton;
        this.fileTreeView = fileTreeView;
        this.usersList = usersList;
    }
}
