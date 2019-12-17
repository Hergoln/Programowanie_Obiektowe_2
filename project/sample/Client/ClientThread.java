package sample.Client;

import javafx.stage.WindowEvent;
import sample.*;
import sample.Exceptions.WrongClassException;
import sample.GUI.FilesStructure;
import sample.GUI.MainWindow;
import sample.GUI.ShareButton;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client Thread class, implements Runnable and is used as client end in Server-Client connection
 */
public class ClientThread implements Runnable
{
    /**
     * Clients socket is static to use it during exit procedure
     */
    public static Socket socketClient;
    private final Path localFolder;

    private Object read;
    private Verified verified;

    private ExecutorService threadPool;

    private WatchService watcher;
    private Timer watchingFileScheduler;
    private final ShareButton shareButton;

    /**
     * Override run method, contains whole logic of client side of an app
     */
    @Override
    public void run()
    {
        System.out.println("Client started");
        MainWindow.console.Inform("Connecting to server...");
        try(Socket socket = new Socket("localhost", 8888))
        {
            this.socketClient = socket;
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream());

            out.writeObject(main.username);

            MainWindow.console.Inform("Waiting for handshake...");
            read = in.readObject();
            if(!(read instanceof Verified)) throw new WrongClassException();
            verified = (Verified)read;
            if(!verified.verified)
            {
                System.out.println("Username is not registered in Server.");
                main.primaryStage
                        .getOnCloseRequest()
                        .handle(new WindowEvent(
                                main.primaryStage,
                                WindowEvent.WINDOW_CLOSE_REQUEST));
                return;
            }
            if(verified.inUse)
            {
                System.out.println("User with " + main.username + " username is already running.");
                main.primaryStage
                        .getOnCloseRequest()
                        .handle(new WindowEvent(
                                main.primaryStage,
                                WindowEvent.WINDOW_CLOSE_REQUEST));
                return;
            }
            MainWindow.console.Inform("Successfully connected\nNegotiating...");
            MainWindow.usersList.SetUsersList(verified.usersList);
            // send file structure
            ArrayList<File> files = MainWindow.getFileTreeView().FlatFileStructure();
            FilesStructure temp = FilesStructure.newFilesStructureFromFilesArray(files, localFolder);
            System.out.println(temp.list);
            out.writeObject(temp);

            // read list/array of files to send back to server and files for client to wait for from server
            if(!((read = in.readObject()) instanceof  ToSend)) throw new WrongClassException();
            ToSend negotiationResult = (ToSend)read;
            System.out.println("For how many do I wait: " + negotiationResult.await+"\nWhat do I need to send back\n"+negotiationResult.list);



            // loop for sending files and receiving files
            boolean moreReceiving;
            threadPool = Executors.newFixedThreadPool(16);
//=============================================== SENDING FILES AFTER NEGOTIATION ===========================================================================
            // initial trade
            int i;
            for(i = 0;
                i < ((moreReceiving = negotiationResult.await > negotiationResult.list.size())
                        ? negotiationResult.list.size()
                        : negotiationResult.await);
                ++i)
            {
                FileWrapper sendFile = new FileWrapper(main.username, negotiationResult.list.get(i), localFolder.toString());
                System.out.println("File("+i+") = " + negotiationResult.list.get(i) + ", sent");
                out.writeObject(sendFile);
                read = in.readObject();
                if(read instanceof FileWrapper)
                    threadPool.execute(new Receiver((FileWrapper)read, localFolder, Action.CREATE));
            }
            System.out.println("======================================================================================");
            System.out.println("========================= Initial files trade completed ==============================");

            // loop for sending/receiving files after initial sending/receiving loop
            for(int j = 0; j < Math.abs(negotiationResult.await - negotiationResult.list.size()); ++j)
            {
                if(moreReceiving)
                {
                    read = in.readObject();
                    if(read instanceof FileWrapper)
                        threadPool.execute(new Receiver((FileWrapper)read, localFolder, Action.CREATE));
                    System.out.println("udalo sie?");
                }
                else
                {
                    FileWrapper sendFile = new FileWrapper(main.username, negotiationResult.list.get(i+j), localFolder.toString());
                    System.out.println("File("+j+") = " + negotiationResult.list.get(i+j) + ", sent");
                    out.writeObject(sendFile);
                    out.flush();
                }
            }
//==========================================================================================================================================================

            System.out.println("we can go further");
            MainWindow.console.Inform("Waiting for actions...");

            watchingFileScheduler = new Timer();
            watchingFileScheduler.schedule(new OutHandler(watcher, localFolder, out, shareButton), 10000, 10000);

            while(true)
            {
                read = in.readObject();
                // Action is send only for eventual change of concept
                if(read instanceof Action)
                {
                    Action action = (Action)read;
                    read = in.readObject();
                    if(read instanceof FileWrapper)
                    {
                        threadPool.execute(new Receiver((FileWrapper)read, localFolder, action));
                    }
                }
                else
                    System.out.println("Have not read Action enum");
            }
        }
        catch(IOException IOExc)
        {
            System.out.println("=====================================================================");
            System.out.println("IOException in clientThread: " + IOExc);
        }
        catch(ClassNotFoundException CNFExc)
        {
            System.out.println("=====================================================================");
            System.out.println("ClassNotFoundException handling: " + CNFExc);
        }
        catch(WrongClassException WCExc)
        {
            System.out.println("Wrong class received, check server application");
        }
        finally {
            threadPool.shutdownNow();
            watchingFileScheduler.cancel();
            try{socketClient.close();} catch (IOException exc) { System.out.println("Already closed socket");}
        }
        return;
    }

    /**
     * @param localFolder path to local main folder given at the start of an app
     * @param watcher watch service responsible for monitoring changes in file structure
     * @param shareButton reference to button responsible for sharing files action
     */
    public ClientThread(Path localFolder, WatchService watcher, ShareButton shareButton)
    {
        this.localFolder = localFolder;
        this.watcher = watcher;
        this.shareButton = shareButton;
    }
}
