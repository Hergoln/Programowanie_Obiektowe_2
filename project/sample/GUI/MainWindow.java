package sample.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;
import sample.Client.ClientThread;
import sample.Server.ServerThread;
import sample.main;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main controller, contains ids of all objects in fxml window
 */
public class MainWindow {
    /**
     * Reference to Console controller
     */
    public static Console console;
    private static FileTreeView fileTreeView;
    /**
     * Reference to ExecutorService with main thread running in it
     */
    public static ExecutorService executor;
    /**
     * Reference to UsersList controller, controller of choice box responsible for choosing to whom file
     * will be shared
     */
    public static UsersList usersList;
    private Runnable mainThread;
    private ShareButton shareButton;

    @FXML
    private TreeView fileTree;
    @FXML
    private TextArea consoleArea;
    @FXML
    private TextArea fileContent;
    @FXML
    private Label username;
    @FXML
    private Button logOutBtn;
    @FXML
    private ChoiceBox<String> permChoice;
    @FXML
    private Button shareBtn;

    /**
     * Method set as onAction function for logOut button
     */
    @FXML
    public void  LogOut()
    {
        main.primaryStage
            .getOnCloseRequest()
            .handle(new WindowEvent(
                        main.primaryStage,
                        WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    /**
     * Method that initialize window from fxml file
     */
    @FXML
    public void initialize()
    {
        try
        {
            fileTreeView = new FileTreeView(fileTree, fileContent);
            username.setText(main.username.toUpperCase());
            console = new Console(consoleArea);
            executor = Executors.newFixedThreadPool(2);
            usersList = new UsersList(permChoice);
            shareButton = new ShareButton(shareBtn, fileTree, usersList);

            if(main.username.equalsIgnoreCase("server"))
            {
                consoleArea.setPrefHeight(250);
                permChoice.setDisable(true);
                permChoice.setVisible(false);
                shareButton.setDisable(true);
                shareButton.setVisible(false);

                mainThread = new ServerThread(main.localFolder);
                System.out.println("Starting server...");
            }
            else
            {
                logOutBtn.setText("Log Out");
                mainThread = new ClientThread(main.localFolder, fileTreeView.watcher, shareButton);
            }

            executor.execute(mainThread);
        }
        catch(IOException IOExc)
        {
            System.out.println("Some files couldn't be created and/or some things do not exist");
        }
    }

    public MainWindow() {}

    /**
     * @return reference to FileTreeView from this class
     */
    public static FileTreeView getFileTreeView() {
        return fileTreeView;
    }
}
