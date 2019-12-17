package sample.Server;

import sample.FileWrapper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Container used as interface to access and change discs representations(folders) and
 * configuration content of discs
 */
public class DiscController
{
    private ConcurrentHashMap<Integer, Disc> discs = new ConcurrentHashMap<>();
    private File history;
    private BufferedWriter archivist;

    /**
     * Constructor, registers discs and creates them if needed
     * @param directory Path to main directory where discs are or will be located
     * @throws IOException when creating or opening of discs, their configuration or
     * history file failed
     */
    public DiscController(Path directory) throws IOException
    {
        for (int i = 0; i < 5; ++i)
        {
            discs.put(i, new Disc(directory.toString() + File.separator + "disc" + i, i));
        }

        this.history = new File(directory.toString() + File.separator + "history.txt");
        this.history.createNewFile();
        archivist = new BufferedWriter(new FileWriter(this.history, true));
        archivist.write("=== "+ Calendar.getInstance().getTime()+" ===");
        archivist.newLine();
        archivist.write(" ========== SERVER STARTED ==========");
        archivist.newLine();
    }

    /**
     * Dumbs content of configuration files to actual files
     * @throws IOException when writing fails
     */
    public void DumpConfig() throws IOException
    {
        for(Disc disc : discs.values())
        {
            disc.DumpConfig();
        }
        archivist.newLine();
        archivist.flush();
    }

    /**
     * Returns map of files that belongs to *user*
     * @param user user whose files are listed
     * @return map of relative paths of *users* files with numbers of discs in which they are located
     */
    public HashMap<String, Integer> usersOwnedFiles(String user)
    {
        HashMap<String, Integer> owned = new HashMap<>();
        for(Disc disc : discs.values())
        {
            for(String file : disc.usersOwnedFiles(user))
            {
                owned.put(file, disc.discNumber);
            }
        }
        return owned;
    }

    /**
     * Returns map of files that *user* is permitted to have access to
     * @param user user whose permitted files are listed
     * @return map of relative paths of *users* permitted files with numbers of discs in which they are located
     */
    public HashMap<String, Integer> usersPermsFiles(String user)
    {
        HashMap<String, Integer> perms = new HashMap<>();
        for(Disc disc : discs.values())
        {
            for(String file : disc.usersPermsFiles(user))
            {
                perms.put(file, disc.discNumber);
            }
        }
        return perms;
    }

    /**
     * Searches for least filled disc and saves file in it, also registers this file do discs configContent
     * @param fileWrapper file to save on disc
     * @return which disc file has been saved on
     */
    public Integer AllocateFile(FileWrapper fileWrapper)
    {
        Disc d = discs.get(LeastFilled());
        File file = new File(d.disc.toPath().toString() + File.separator + fileWrapper.relativePath);
        System.out.println("ADDING file: "+fileWrapper.relativePath+"; From: "+fileWrapper.owner);
        try
        {
            if(file.createNewFile()) {
                FileOutputStream os = new FileOutputStream(file);
                if(null != fileWrapper.content) {
                    os.write(fileWrapper.content);
                    d.addFile(fileWrapper.relativePath, fileWrapper.content.length, fileWrapper.owner, fileWrapper.perms);
                }
                else
                    d.addFile(fileWrapper.relativePath, 0, fileWrapper.owner, fileWrapper.perms);

                AddToHistory(d.discNumber, fileWrapper.relativePath, fileWrapper.perms, "ADD", fileWrapper.owner);
            }
            else throw new IOException();
        } catch (IOException IOExc) {
            System.out.println("discController.AllocateFile could not create file\n\t("+file.toPath()+") :"+IOExc);
            return -1;
        }
        return d.discNumber;
    }

    /**
     * Updating file function, used also to add permissions
     * @param fileWrapper file to update
     * @param discNumber indicator which disc is file located in
     * @throws IOException thrown when file failed to save
     */
    public void UpdateFile(FileWrapper fileWrapper, int discNumber) throws IOException
    {
        if(discNumber < 0)
        {
            AllocateFile(fileWrapper);
        }
        else
        {
            Disc d = discs.get(discNumber);
            if(d.IsOwner(fileWrapper.relativePath, fileWrapper.owner))
            {
                System.out.println("UPDATING file: " + fileWrapper.relativePath + "; From: " + fileWrapper.owner);
                File file = new File(d.disc.toPath().toString() + File.separator + fileWrapper.relativePath);
                FileOutputStream os = new FileOutputStream(file);
                os.write(fileWrapper.content);
                d.UpdatePerms(fileWrapper);
                d.ReCalculateSize();
                AddToHistory(discNumber, fileWrapper.relativePath, fileWrapper.perms, "MODIFY", fileWrapper.owner);
            }
        }
    }

    /**
     * Updating file function, used also to add permissions
     * @param fileWrapper file to update
     * @throws IOException thrown when file failed to save
     */
    public void UpdateFile(FileWrapper fileWrapper) throws IOException
    {
        UpdateFile(fileWrapper, IsIn(fileWrapper.relativePath));
    }

    /**
     * Removes file from disc
     * @param fileWrapper file to remove
     * @throws IOException thrown when file failed to delete
     */
    public void DeallocateFile(FileWrapper fileWrapper) throws IOException
    {
        for(Disc d : discs.values())
        {
            if(d.RemoveFile(fileWrapper.relativePath)) d.ReCalculateSize();
            AddToHistory(d.discNumber, fileWrapper.relativePath, fileWrapper.perms, "DELETE", "");
        }
    }


    /**
     * Removes file from disc
     * @param fileWrapper file to remove
     * @param discNumber indicator which disc is file located in
     * @throws IOException thrown when file failed to delete
     */
    public void DeallocateFile(FileWrapper fileWrapper, int discNumber) throws IOException
    {
        if(discNumber < 0) return;
        Disc d = discs.get(discNumber);
        if(d.IsOwner(fileWrapper.relativePath, fileWrapper.owner)) {
            if (d.RemoveFile(fileWrapper.relativePath)) d.ReCalculateSize();
            AddToHistory(d.discNumber, fileWrapper.relativePath, fileWrapper.perms, "DELETE", fileWrapper.owner);
        }
        else
        {
            d.RemovePerm(fileWrapper.relativePath, fileWrapper.owner);
            System.out.println("Removed perm from file: "+fileWrapper.relativePath+"; for " +fileWrapper.owner);
        }
    }

    private int LeastFilled()
    {
        int toReturn = 0;
        for(int i = 1; i < discs.size(); ++i)
        {
            if(discs.get(i).size < discs.get(toReturn).size) toReturn = i;
        }
        return  toReturn;
    }

    private void AddToHistory(int DiscNumber, String file, String[] perms, String action, String who) throws IOException
    {
        archivist.write("[disc:"+DiscNumber+"] File: disc"+DiscNumber+File.separator+file+";"+action+";Permissions:");
        if(perms != null)
            for(int i = 0; i < perms.length; ++i) archivist.write(perms[i]+",");
        archivist.write("\t["+who+"]");
        archivist.newLine();
    }

    /**
     * Determines if file which relative path is represented by path is in discs and which disc is it located in
     * @param path relative path of file for which function searches
     * @return disc number of disc with file or -1 if file not found
     */
    public int IsIn(String path)
    {
        for(Disc d : discs.values())
        {
            if(d.IsIn(path) > 0) return d.discNumber;
        }
        return -1;
    }

    /**
     * Returns path of file represented by relativePath
     * @param relativePath relative path of file to search
     * @return Path of found file or null if file not found
     */
    public Path GetFromRelativePath(String relativePath)
    {
        int number = IsIn(relativePath);
        if(number >= 0)
        {
            return discs.get(number).GetFromRelativePath(relativePath);
        }
        return null;
    }

    /**
     * Class representing one of Discs
     */
    private class Disc
    {
        private File disc;
        private Integer discNumber;
        private File config;
        private ArrayList<String> configContent = new ArrayList<>();
        private long size;

        /**
         * Creates disc object of Disc class, reads content of config file or creates it if necessary
         * @param path String representing path of this newly created disc
         * @param discNumber number of this disc
         * @throws IOException when reading/writing config file fails
         */
        public Disc(String path, Integer discNumber) throws IOException
        {
            this.discNumber = discNumber;
            try {
                String line;
                this.disc = new File(path);
                System.out.println(path);
                this.disc.mkdir();
                File config = new File(path + File.separator + "config.csv");
                if(!config.createNewFile())
                {

                    this.size = calculateSize(this.disc);
                    BufferedReader reader = Files.newBufferedReader(
                            config.toPath(),
                            Charset.forName("UTF-8")
                    );
                    while ((line = reader.readLine()) != null) {
                        File tempFile = new File(path + File.separator + line.split(",")[0]);
                        if (tempFile.exists())
                            configContent.add(line);
                    }
                }
                this.config = config;
            } catch (IOException e) {
                System.out.println("(Disc class)Could not find config.csv file");
                throw e;
            }
        }

        private long calculateSize(File disc)
        {
            long size = 0;
            if(disc.isDirectory())
            {
                for(File child : disc.listFiles())
                {
                    if(!child.getName().equals("config.csv"))
                        size += child.length();
                    else
                        this.config = child;
                }
            }

            return size;
        }

        /**
         * Method called to compute size of disc
         */
        public void ReCalculateSize()
        {
            this.size = 0;
            for(File child : disc.listFiles())
            {
                if(!child.getName().equals("config.csv"))
                    this.size += child.length();
            }
        }

        /**
         * Registers file to config
         * @param name relative path of file
         * @param size size of file
         * @param owner owner of file
         * @param perms permissions given to file
         */
        public void addFile(String name, long size, String owner, String[] perms)
        {
            this.size += size;
            StringBuilder builder = new StringBuilder();
            // have to check what about last semicolon, does it affect anything or does not generate any errors
            builder.append(name+",");
            builder.append(owner);
            builder.append(",");
            if(perms != null)
            {
                for(String s : perms) builder.append(s+";");
            }
            configContent.add(new String(builder));
    }

        /**
         * Dumps configContent to file
         * @throws IOException thrown when could not read to config
         */
        public void DumpConfig() throws IOException
        {
            PrintWriter out = new PrintWriter(config);
            for(String line : configContent)
            {
                out.println(line);
            }
            out.close();
        }

        /**
         * Lists relative paths of files owned by *user*
         * @param user name of user whose files are listed
         * @return list of files
         */
        public ArrayList<String> usersOwnedFiles(String user)
        {
            ArrayList<String> owned = new ArrayList<>();
            String[] parsed;
            for(String file : configContent)
            {
                parsed = file.split(",");
                if(parsed.length > 1)
                    if(parsed[1].equals(user)) owned.add(parsed[0]);
            }
            return owned;
        }

        /**
         * Lists relative paths of files to which *user* have access
         * @param user name of user whose permitted files are listed
         * @return list of files
         */
        public ArrayList<String> usersPermsFiles(String user)
        {
            ArrayList<String> perms = new ArrayList<>();
            String[] parsed;
            for(String file : configContent)
            {
                parsed = file.split(",");
                if(parsed.length > 2)
                    if(parsed[2].contains(user)) perms.add(parsed[0]);
            }
            return perms;
        }

        /**
         * Function removes file from disc config content and from directory
         * @param fileName relative path of searched file
         * @throws IOException thrown when could not find file
         */
        // causes some issues with deleting, don't know what might be the reason for it though
        public boolean RemoveFile(String fileName) throws IOException
        {
            int index = IsIn(fileName);
            if(index >= 0)
            {
                System.out.println("DELETING file: "+fileName+"; From disc: "+discNumber+"; Owner: "+configContent.get(index).split(",")[1]);
                File toDelete = new File(disc.getPath() + File.separator + fileName);
                if(toDelete.exists())
                {
                    toDelete.delete();
                    configContent.remove(index);
                }
                else {
                    throw new IOException();
                }
                return true;
            }
            return false;
        }

        /**
         * Updates permissions to given file
         * @param fileWrapper file which permissions will be updated
         */
        public void UpdatePerms(FileWrapper fileWrapper)
        {
            int index = IsIn(fileWrapper.relativePath);
            if(configContent.get(index).split(",")[1].equals(fileWrapper.owner))
                if(index >= 0)
                {
                    String temp = configContent.remove(index);
                    StringBuilder builder = new StringBuilder(temp);
                    if(fileWrapper.perms != null)
                    {
                        for(String s : fileWrapper.perms)
                        {
                            if(!temp.contains(s))
                                builder.append(s+";");
                        }
                    }
                    configContent.add(new String(builder));
                }
        }

        /**
         * returns in which line is given relative path in configContent
         * @param fileName searched relative path
         * @return line number where currently file is in configContent or -1 if could not find it
         */
        public int IsIn(String fileName)
        {
            for(String line : configContent)
            {
                if(line.split(",")[0].equals(fileName))
                    return configContent.indexOf(line);
            }
            return -1;
        }

        private Path GetFromRelativePath(String relativePath)
        {
            return Paths.get(disc.toString()+File.separator+relativePath);
        }

        private boolean IsOwner(String relativePath, String clientName)
        {
            int index = IsIn(relativePath);
            if(index >= 0)
            {
                for(String line : configContent)
                {
                    if(line.split(",")[0].equals(relativePath))
                        if(line.split(",")[1].equals(clientName))
                            return true;
                }
            }
            return false;
        }

        private void RemovePerm(String relativePath, String clientName)
        {
            int index = IsIn(relativePath);
            if(index >= 0)
            {
                String temp = configContent.get(index);
                if(temp.contains(clientName+";"))
                {
                    temp = temp.replace(clientName+";", "");
                    configContent.remove(index);
                    configContent.add(temp);
                }

                System.out.println("Config for disc("+discNumber+"):");
                for(String line : configContent)
                {
                    System.out.println(line);
                }
            }
        }
    }
}