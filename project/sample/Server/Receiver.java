package sample.Server;

import sample.Action;
import sample.FileWrapper;
import sample.GUI.MainWindow;

import java.io.IOException;

// Somewhere here shall be this "random sleep" from exercise

/**
 * Server handler for handling files from clients
 * implements Runnable for multi threading
 */
class Receiver implements Runnable
{
    private final DiscController discController;
    private final Users users;
    private final Users.UserFiles client;
    private final String clientName;
    private final Action action;
    private FileWrapper file;

    /**
     * Logic of handling file
     */
    @Override
    public void run()
    {
        try {
            Integer discNumber = -1;
            synchronized (discController)
            {
                if (action == Action.CREATE)
                {
                    discNumber = discController.AllocateFile(file);
                    synchronized (MainWindow.console)
                    {
                        MainWindow.console.Inform("["+clientName + "] Added file " + file.relativePath);
                    }
                }
                else if (action == Action.MODIFY)
                {
                    discNumber = client.WhichDisc(file.relativePath);
                    discController.UpdateFile(file, discNumber);

                    //handling permissions
                    if(file.perms != null)
                    {
                        System.out.print("Perms: " + file.perms);
                        users.QueueFilesWithPerms(discController.GetFromRelativePath(file.relativePath), discNumber, file.perms, file.relativePath);
                    }

                    synchronized (MainWindow.console)
                    {
                        MainWindow.console.Inform("["+clientName + "] Modified file " + file.relativePath);
                    }
                }
                else if (action == Action.DELETE)
                {
                    discNumber = client.WhichDisc(file.relativePath);
                    discController.DeallocateFile(file, discNumber);
                    synchronized (MainWindow.console)
                    {
                        MainWindow.console.Inform("["+clientName + "] Deleted file " + file.relativePath);
                    }
                }
            }
            synchronized (client) {
                if (action == Action.CREATE)
                    if (discNumber >= 0) client.AddFile(file.relativePath, true, discNumber);
            }
            // permission given to client handling
            if (!file.owner.equals(clientName))
                System.out.println("Permission given to File(but this client is not its owner):\n" + (file.relativePath));
        }
        catch (IOException IOExc)
        {
            System.out.println("Receiver IOException caught: " + IOExc);
        }
    }

    /**
     * @param discController reference to disc controller
     * @param client reference to list of clients files (permitted and owned)
     * @param users list of users
     * @param clientName name of handled client
     * @param file file to handle
     * @param action action indicator used to determine what to do with the file
     * @throws NullPointerException
     */
    public Receiver(DiscController discController, Users.UserFiles client, Users users, String clientName, FileWrapper file, Action action) throws NullPointerException
    {
        this.discController = discController;
        this.client = client;
        this.users = users;
        this.clientName = clientName;
        this.action = action;
        if(file == null) throw new NullPointerException();
        this.file = file;
    }
}
