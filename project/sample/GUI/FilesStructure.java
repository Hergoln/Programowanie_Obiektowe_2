package sample.GUI;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Class used for storing files paths relative to main/local folder
 */
public class FilesStructure implements Serializable
{
    /**
     * list of relative paths of sent files
     */
    public ArrayList<String> list;
    /**
     * list of disc number associated with paths from *list*, used only on server side of an app
     */
    public ArrayList<Integer> discs;

    public FilesStructure(ArrayList<String> list)
    {
        this.list = list;
    }

    /**
     * Method converting list of files into FilesStructure
     * @param list list of files converted to FilesStructure
     * @param localFolder path to local folder, used for determining relative paths from list of files
     * @return newly created object of this class
     */
    public static FilesStructure newFilesStructureFromFilesArray(ArrayList<File> list, Path localFolder)
    {
        return new FilesStructure(new ArrayList<>(list
                                                    .stream()
                                                    .map(f -> f.getAbsolutePath().replace(localFolder.toString()+File.separator, ""))
                                                    .collect(Collectors.toList())));
    }

    /**
     * @param list list of relative paths of sent files
     * @param filesDiscsList list of discs numbers associated with files from *list*
     * @return newly created object of this class
     */
    public static FilesStructure newFilesStructureFromFilesArray(ArrayList<String> list, ArrayList<Integer> filesDiscsList)
    {
        FilesStructure toReturn = new FilesStructure(list);
        toReturn.discs = filesDiscsList;
        return toReturn;
    }
}
