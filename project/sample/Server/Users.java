package sample.Server;

import sample.GUI.FilesStructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 *  Users class, used for storing users on server side while application is running
 */
public class Users
{
    /**
     * Concurrent list of users
     */
    public ConcurrentHashMap<String, UserFiles> list = new ConcurrentHashMap<>();

    /**
     * @param mainDirectory main server directory, should contain users.csv
     * @param discController disc controller reference
     * @throws IOException thrown when could not read users.csv
     * @throws InvalidPathException throw when user.csv does not exist
     */
    Users(Path mainDirectory, DiscController discController) throws IOException, InvalidPathException
    {
        String temp;
        BufferedReader reader = Files.newBufferedReader(
                Paths.get(mainDirectory.toString()+"/users.csv"),
                Charset.forName("UTF-8")
                );
        while((temp = reader.readLine()) != null)
        {
            list.put(temp, new UserFiles(discController.usersOwnedFiles(temp), discController.usersPermsFiles(temp)));
        }
    }

    /**
     * Checks if user *name* is in users list
     * @param name searched name
     * @return if user is in list or not
     */
    public boolean isUserIn(String name)
    {
        return list.containsKey(name);
    }

    /**
     * @return List of users in use from hashmap
     * @param askingClient name of client asking for list
     */
    public ArrayList<String> getActiveUsers(String askingClient)
    {
        return new ArrayList<>(list.keySet().stream().filter(c->list.get(c).inUse && !c.equals(askingClient)).collect(Collectors.toList()));
    }

    /**
     * @return List of users from hashmap omitting asking user
     * @param askingClient name of client asking for list
     */
    public ArrayList<String> getUsers(String  askingClient)
    {
        return new ArrayList<String>(list.keySet().stream().filter(c->!c.equals(askingClient)).collect(Collectors.toList()));
    }

    /**
     * @return List of users from hashmap
     */
    public ArrayList<String> getUsers()
    {
        return new ArrayList<>(list.keySet());
    }

    /**
     * Queues shared file for client to receive
     * @param path absolute path of file to share
     * @param discNumber disc number where file is located
     * @param perms permissions given to file
     * @param relativePath relative path of shared file
     */
    public void QueueFilesWithPerms(Path path, Integer discNumber, String[] perms, String relativePath)
    {
        if(perms != null)
            for(String s : perms)
            {
                list.get(s).AddToQueue(path, discNumber, relativePath);
            }
    }

    /**
     * @param name Name of User to be returned
     * @return Users object
     */
    public UserFiles getUser(String name)
    {
        list.get(name).inUse = true;
        return list.get(name);
    }

    /**
     * Class containing lists of users files
     */
    public class UserFiles {
        // String: relative path, Integer: disc number
        private ConcurrentHashMap<String, Integer> owned;
        private ConcurrentHashMap<String, Integer> perms;
        private ConcurrentLinkedQueue<SharedQueueNode> sharedQueue;

        /**
         * indicates if User object is active
         */
        public boolean inUse;

        /**
         * @param owned list of owned by this user files
         * @param perms list of files that this user have access to
         */
        public UserFiles(HashMap<String, Integer> owned, HashMap<String, Integer> perms)
        {
            this.owned = new ConcurrentHashMap<>(owned);
            this.perms = new ConcurrentHashMap<>(perms);
            this.sharedQueue = new ConcurrentLinkedQueue<>();
            inUse = false;
        }

        /**
         * Return disc number of disc with file of fileName in it
         * @param fileName searched name
         * @return disc number with file or -1 if not found
         */
        public int WhichDisc(String fileName)
        {
            if(owned.containsKey(fileName)) return owned.get(fileName);
            if(perms.containsKey(fileName)) return perms.get(fileName);
            return -1;
        }

        /**
         * Displays content of this object
         */
        public void MyDisplay()
        {
            System.out.println("Owned files:");
            for(String file : owned.keySet())
            {
                System.out.println("\t"+file);
            }
            System.out.println("Permitted files");
            for(String perm : perms.keySet())
            {
                System.out.println("\t"+perm);
            }
        }

        /**
         * Adds file
         * @param filePath relative path of file to add
         * @param own indicator if this user is owner of this file
         * @param discNumber disc number where file is located
         */
        public void AddFile(String filePath, boolean own, Integer discNumber)
        {
            if(own)
            {
                owned.put(filePath, discNumber);
            }
            else
            {
                perms.put(filePath, discNumber);
            }
        }

        /**
         * Returns FileStructure with files not contained within this object
         * @param files FilesStructure to compare with this structures
         * @return collection of relative paths of files from given FileStructure that
         * this object does not contain
         */
        public FilesStructure NotInUsersStructure(FilesStructure files)
        {
            ArrayList<String> toReturn = new ArrayList<>();
            for(String file : files.list)
            {
                if(!owned.containsKey(file) && !perms.containsKey(file)) toReturn.add(file);
            }
            return new FilesStructure(toReturn);
        }

        /**
         * Returns FileStructure with files from this object that are not within
         * give FilesStructure
         * @param files FileStructure to compare with
         * @return collection of relative paths of files from this object FileStructure that
         * given FilesStructure does not contain
         */
        public FilesStructure NotInGivenStructure(FilesStructure files)
        {
            ArrayList<String> filesToReturn = new ArrayList<>();
            ArrayList<Integer> discToReturn = new ArrayList<>();
            for(String file : owned.keySet())
            {
                if(!files.list.contains(file))
                {
                    filesToReturn.add(file);
                    discToReturn.add(owned.get(file));
                }
            }
            for(String file : perms.keySet())
            {
                if(!files.list.contains(file))
                {
                    filesToReturn.add(file);
                    discToReturn.add(perms.get(file));
                }
            }
            return FilesStructure.newFilesStructureFromFilesArray(filesToReturn, discToReturn);
        }

        private void AddToQueue(Path path, Integer discNumber, String relativePath)
        {
            perms.put(relativePath, discNumber);
            if(inUse)
                sharedQueue.add(new SharedQueueNode(path, discNumber));
        }

        /**
         * Checks if queue with shared files is empty
         * @return indicator if queue with shared files is empty
         */
        public boolean IsSharedQueueEmpty()
        {
            return sharedQueue.isEmpty();
        }

        /**
         * Retrieves first added entry of shared queue files
         * @return SharedQueueNode with head from shared files queue
         */
        public SharedQueueNode GetSharedQueueEntry()
        {
            return sharedQueue.poll();
        }

        /**
         * Secondary class for shared files queue nodes
         */
        public class SharedQueueNode
        {
            public Path path;
            public Integer discNumber;

            public SharedQueueNode(Path path, Integer discNumber) {
                this.path = path;
                this.discNumber = discNumber;
            }
        }
    }
}
