package sample.Server;
import sample.Action;
import sample.FileWrapper;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.TimerTask;

/**
 * Server side output stream handling class used after first negotiation between Client and Server
 */
public class OutHandler extends TimerTask
{
    private final ObjectOutputStream out;

    private final Users.UserFiles client;
    private final DiscController discs;

    @Override
    public void run()
    {
        try
        {
            if (!client.IsSharedQueueEmpty())
            {
                Users.UserFiles.SharedQueueNode node = client.GetSharedQueueEntry();
                out.writeObject(Action.CREATE);
                FileWrapper sendFile = new FileWrapper(node.path, node.discNumber);
                out.writeObject(sendFile);
            }
        }
        catch(IOException IOExc)
        {
            System.out.println("SERVER OUTHANDLER, SOMEWHERE IS ERROR WITH FILES CONTENT GETTING: " + IOExc);
        }
    }

    /**
     * Standard constructor
     * @param out output object stream
     * @param client clients files list reference
     * @param discs reference to disc controller
     */
    public OutHandler(ObjectOutputStream out, Users.UserFiles client, DiscController discs)
    {
        this.out = out;
        this.client = client;
        this.discs = discs;
    }
}
