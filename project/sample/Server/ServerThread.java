package sample.Server;

import sample.*;
import sample.Exceptions.WrongClassException;
import sample.GUI.FilesStructure;
import sample.GUI.MainWindow;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static sample.Action.*;

/**
 * This is Server main class, it contains whole functionality of Server side of an app
 */
public class ServerThread implements Runnable
{
    /**
     * ServerSocket listening for clients
     */
    public static ServerSocket listener;
    private final DiscController discController;
    private Path directory;
    private ExecutorService threadPool;
    /**
     * Reference to Users main class
     */
    public static Users users;

    /**
     * Override run method, contains whole logic of server side of an app
     */
    @Override
    public void run()
    {
        try
        {
            listener = new ServerSocket(8888);
            threadPool = Executors.newFixedThreadPool(8);

            MainWindow.console.Inform("Server started");
            System.out.println("Server started");

            while(true)
            {
                threadPool.execute(new ClientHandler(listener.accept()));
            }
        }
        catch (IOException IOExc)
        {
            if(!listener.isClosed()) System.out.println("Users.csv folder has not been found or is not possible to read.");
        }
        catch (InvalidPathException IPExc)
        {
            System.out.println("Paths from User.csv are not proper paths.");
        }
        finally
        {
            try{listener.close();} catch (IOException exc) { System.out.println("Already closed socket");}
            try
            {
                discController.DumpConfig();
            }
            catch (IOException exc)
            {
                System.out.println("(MainServerThread)Config files do not exist or are corrupted");
            }
            threadPool.shutdownNow();
            System.out.println("Server has stopped in Main Server Thread");
        }
    }

    /**
     * Standard constructor
     * @param localFolder main directory of server
     * @throws IOException thrown if necessary files for creating DiscController or Users will be corrupted
     */
    public ServerThread(Path localFolder) throws IOException
    {
        this.directory = localFolder;
        this.discController = new DiscController(directory);
        users = new Users(directory, discController);
    }

    /**
     * Class handling clients
     */
    private class ClientHandler implements Runnable
    {
        private final Socket socket;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;

        private ExecutorService threadPool;
        private Timer permsWatcher;

        private Users.UserFiles client;
        private Object read;
        private Boolean verified = false;

        @Override
        public void run()
        {
            String clientName = null;
            MainWindow.console.Inform("Client is running on socket: " + socket);
            try
            {
                this.out = new ObjectOutputStream(socket.getOutputStream());
                this.in = new ObjectInputStream(socket.getInputStream());
                //reading username
                read = in.readObject();
                if(!(read instanceof String)) throw new WrongClassException();
                clientName = (String)read;
                MainWindow.console.Inform("["+clientName + "] Searching for user...");

                //sending handshake
                if(!users.isUserIn(clientName))
                {
                    MainWindow.console.Inform("["+clientName + "]User is not registered");
                    out.writeObject(new Verified(false, false, null));
                    return;
                }
                else
                {
                    client = users.getUser(clientName);
                    if(!client.inUse)
                    {
                        out.writeObject(new Verified(true, true, null));
                    }
                    out.writeObject(new Verified(true, false, users.getUsers(clientName)));
                    verified = true;
                    MainWindow.console.Inform("["+clientName + "]Sending handshake...");
                }
                client.inUse = true;
                MainWindow.console.Inform("["+clientName + "] Connection established\nNegotiating...");

                // read file structure
                read = in.readObject();
                if(!(read instanceof FilesStructure)) throw new WrongClassException();
                System.out.println(((FilesStructure)read).list);

                // compare structures structure that server have to send to client
                FilesStructure filesToSendBackToClient = client.NotInGivenStructure((FilesStructure) read);
                System.out.println("files that need to be sent to client\n"+filesToSendBackToClient.list);
                /**
                 * ToSend()
                 * first argument is structure that client have to send back to server
                 * second argument is size of a structure that server have to send to client
                 */
                ToSend negotiationResult = new ToSend((FilesStructure) read, filesToSendBackToClient.list.size());

                // send list/array of files to send back to server and files for client to wait for from server
                out.writeObject(negotiationResult);

                boolean moreReceiving;
                threadPool = Executors.newFixedThreadPool(16);
//=============================================== SENDING FILES AFTER NEGOTIATION ===========================================================================
                // initial trade
                int i;
                for(i = 0;
                    i < ((moreReceiving = negotiationResult.await < negotiationResult.list.size())
                            ? negotiationResult.await
                            : negotiationResult.list.size());
                    ++i)
                {
                    read = in.readObject();
                    if(read instanceof FileWrapper)
                        threadPool.execute(new Receiver(discController, client, users, clientName, (FileWrapper)read, MODIFY));
                    FileWrapper fileWrapper = new FileWrapper(clientName, filesToSendBackToClient.list.get(i), directory.toString(), filesToSendBackToClient.discs.get(i));
                    System.out.println("File = (" +
                            fileWrapper.relativePath +
                            "), owner = (" + fileWrapper.owner +
                            "), perms = (" + fileWrapper.perms +
                            "), content = (" + fileWrapper.content + ")");
                    out.writeObject(fileWrapper);
                }

                // loop for sending/receiving files after initial sending/receiving loop
                for(int j=0; j < Math.abs(negotiationResult.await - negotiationResult.list.size()); ++j)
                {
                    if(moreReceiving)
                    {
                        //receiving files
                        read = in.readObject();
                        if(read instanceof FileWrapper)
                            threadPool.execute(new Receiver(discController, client, users, clientName, (FileWrapper)read, MODIFY));
                    }
                    else
                    {
                        FileWrapper fileWrapper = new FileWrapper(clientName, filesToSendBackToClient.list.get(j), directory.toString(), filesToSendBackToClient.discs.get(j));
                        out.writeObject(fileWrapper);
                    }
                }
//==========================================================================================================================================================

                System.out.println("we can go further");
                MainWindow.console.Inform("["+clientName + "] Waiting for actions from client...");

                permsWatcher = new Timer();
                permsWatcher.schedule(new OutHandler(out, client, discController), 1000, 1000);
                while(true)
                {
                    read = in.readObject();
                    if(read instanceof Action)
                    {
                        Action action = (Action)read;
                        read = in.readObject();
                        if(read instanceof FileWrapper)
                        {
                            threadPool.execute(new Receiver(discController, client, users, clientName, (FileWrapper)read, action));
                        }
                        else
                        {
                            System.out.println("Dunno what is there");
                            throw new WrongClassException();
                        }
                    }
                    else
                        System.out.println("Have not read Action enum");
                }
            }
            catch(IOException IOExc)
            {
                System.out.println("=====================================================================");
                System.out.println("IOException in Client handling: " + IOExc);
            }
            catch(ClassNotFoundException CNFExc)
            {
                System.out.println("=====================================================================");
                System.out.println("ClassNotFoundException in Client handling: " + CNFExc);
            }
            catch(WrongClassException WCExc)
            {
                System.out.println("Wrong class received, check client application");
            }
            finally
            {
                if(MainWindow.console != null && verified) MainWindow.console.Inform("["+clientName+"] Client has disconnected");
                if(permsWatcher != null) permsWatcher.cancel();
                if(client != null) client.inUse = false;
                if(threadPool != null) threadPool.shutdown();
                try{socket.close();} catch (IOException exc) { System.out.println("Already closed socket");}
            }
        }

        /**
         * Standard constructor
         * @param socket socket connected to client
         */
        ClientHandler(Socket socket)
        {
            this.socket = socket;
        }
    }
}
