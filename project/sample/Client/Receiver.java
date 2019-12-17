package sample.Client;

import sample.Action;
import sample.FileWrapper;

import java.io.*;
import java.nio.file.Path;

/**
 * Client class responsible for dealing with file from server, implements Runnable. Multiple instances of
 * this class are created in order to handle multiple files send from server
 */
public class Receiver implements Runnable
{
    private FileWrapper fileWrapper;
    private final Action action;
    private final Path localFolder;

    @Override
    public void run()
    {
        try
        {
            System.out.println("File = (" +
                    fileWrapper.relativePath +
                    ")");
            File file = new File(localFolder + File.separator + fileWrapper.relativePath);

            if(action == Action.DELETE)
            {
                if(file.exists()) file.delete();
            }
            else
            {
                if (fileWrapper.owner == null)
                    System.out.println("Permission given to File: " + fileWrapper.relativePath);
                file.createNewFile();
                if(null != fileWrapper.content) {
                    OutputStream os = new FileOutputStream(file);
                    os.write(fileWrapper.content);
                }
            }
        }
        catch(IOException ioexc)
        {
            System.out.println("Reading or writing problems (in receiver thread) :\n" + ioexc);
            return;
        }
    }

    /**
     * @param fileWrapper file read from input stream
     * @param localFolder path to local main folder given at the start of an app
     * @param action action that will be performed on received file
     * @throws NullPointerException thrown when fileWrapper has not been given to function or it was null
     */
    public Receiver(FileWrapper fileWrapper, Path localFolder, Action action) throws NullPointerException
    {
        if(fileWrapper == null) throw new NullPointerException();
        this.fileWrapper = fileWrapper;
        this.localFolder = localFolder;
        this.action = action;
    }
}
