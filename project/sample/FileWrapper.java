package sample;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;

/**
 * Class used to wrap and send SMALL files via sockets. byte[] is not a proper structure that should be used to read large files
 */
public class FileWrapper implements Serializable
{
    /**
     * Owner of sent file
     */
    public String owner;
    /**
     * Relative path of sent file
     */
    public String relativePath;
    /**
     * Array of permissions to this sent file
     */
    public String[] perms;
    /**
     * Content of sent file
     */
    public byte[] content;
    /**
     * Disc number of file
     */
    public Integer discNumber = -1;

    /**
     * Client side constructor
     * @param owner owner of file
     * @param relativePath relative path of file
     * @param mainFolder local folder used for retrieving content from actual
     * file if not given no content will be provided
     */
    public FileWrapper(String owner, String relativePath, String mainFolder) {
        this.owner = owner;
        this.relativePath = relativePath;

        if(false == mainFolder.isEmpty())
        {
            try {
                content = Files.readAllBytes(Paths.get(mainFolder + File.separator + relativePath));
            } catch (IOException IOExc) {
                System.out.println("Error while reading file : " + IOExc);
            }
        }
    }

    /**
     * Server side constructor
     * @param owner owner of file
     * @param relativePath relative path of file
     * @param mainFolder local folder used for retrieving content from actual
     * file if not given no content will be provided
     * @param discNumber disc number where file is located
     */
    public FileWrapper(String owner, String relativePath, String mainFolder, Integer discNumber) {
        this.owner = owner;
        this.relativePath = relativePath;
        this.discNumber = discNumber;

        try {
            content = Files.readAllBytes(Paths.get(mainFolder +File.separator+"disc"+discNumber+File.separator+ relativePath));
        }
        catch(IOException IOExc)
        {
            System.out.println("Error while reading file : " + IOExc);
        }
    }

    /**
     * Client side constructor
     * @param owner owner of file
     * @param relativePath relative path of file
     * @param mainFolder local folder used for retrieving content from actual
     * file if not given no content will be provided
     * @param perms permissions given to file
     */
    public FileWrapper(String owner, String relativePath, String mainFolder, String[] perms) {
        this(owner, relativePath, mainFolder);
        if(perms == null) this.perms = new String[0];
        else this.perms = perms;
    }

    /**
     * Server side constructor
     * @param owner owner of file
     * @param relativePath relative path of file
     * @param mainFolder local folder used for retrieving content from actual
     * file if not given no content will be provided
     * @param perms permissions given to file
     * @param discNumber disc number where file is located
     */
    public FileWrapper(String owner, String relativePath, String mainFolder, String[] perms, Integer discNumber) {
        this(owner, relativePath, mainFolder);
        this.discNumber = discNumber;
        if(perms == null) this.perms = new String[0];
        else this.perms = perms;
    }

    /**
     * Server side constructor used with absolute path of file
     * @param filePath path of file
     * @param discNumber disc number where file is located
     * @throws IOException thrown when file could not be read
     */
    public FileWrapper(Path filePath, Integer discNumber) throws IOException
    {
        this.owner = null;
        this.relativePath = filePath.toString().split("disc"+discNumber)[1];
        this.content = Files.readAllBytes(filePath);
    }
}
