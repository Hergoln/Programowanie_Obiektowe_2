package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.Client.ClientThread;
import sample.GUI.FileTreeView;
import sample.GUI.MainWindow;
import sample.Server.ServerThread;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;

import static javafx.application.Platform.exit;

/**
 * Class used only to start an Application
 */
public class main extends Application
{
    public static String username;
    public static Path localFolder;
    public static Stage primaryStage;
    private static boolean client = false;

    @Override
    public void start(Stage primaryStage)
    {
        this.primaryStage  = primaryStage;

        primaryStage.setOnCloseRequest(event ->
        {
                if(MainWindow.console != null) MainWindow.console.Inform("Logging out");
                if(MainWindow.executor != null)
                {
                    try
                    {
                        if(client) ClientThread.socketClient.close();
                        else ServerThread.listener.close();
                    }
                    catch (IOException IOExc) {}
                    MainWindow.executor.shutdownNow();
                }

                System.out.println("Logging out");
                if(FileTreeView.updateScheduler != null)
                {
                    FileTreeView.updateScheduler.cancel();
                    FileTreeView.updateScheduler.purge();
                }
                primaryStage.close();
                exit();
        });

        try{
            Parent root = FXMLLoader.load(getClass().getResource("GUI/MainWindow.fxml"));
            primaryStage.setTitle(username);
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();
        }
        catch(IOException ioexc)
        {
            System.out.println(ioexc+"\nCouldn't load \"MainWindow.fxml\" scene.");
            System.exit(-2147);
        }
    }

    public static void main(String[] args)
    {
        try
        {
            localFolder = Paths.get(args[1]);
            File checker = new File(localFolder.toString());
            if(!checker.isDirectory())
            {
                System.out.println(localFolder + " is not a directory");
                System.exit(-8);
            }
        }
        catch(InvalidPathException IPexc)
        {
            System.out.println(args[1] + " is not a correct path");
            System.exit(-8);
        }

        username = args[0];
        try(Socket socket = new Socket("localhost", 8888))
        {
            if(main.username.equalsIgnoreCase("server"))
            {
                System.out.println("Server is already running");
                socket.close();
                System.exit(-8);
            }
            client = true;
        }
        catch(IOException IOExc) {
            if(!main.username.equalsIgnoreCase("server"))
            {
                System.out.println("Server does not exist (you can start it though)");
                System.exit(0);
            }
        }

        System.out.println("Username: " + username + ", file path: " + localFolder.toString());
        launch(args);
    }
}
