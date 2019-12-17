package sample.GUI;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import sample.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Controller class responsible for definition and management of Files structure representation.
 */
public class FileTreeView
{
    private TreeView<File> fileTree;
    private FileView fileView;
    private TimerTask updater;
    /**
     * Timer class used for refreshing file structure
     */
    public static Timer updateScheduler;
    /**
     * WatchService responsible for monitoring changes in file structure within local folder
     */
    public WatchService watcher;
    /**
     * WatchKey registered as watcher for local folder
     */
    public WatchKey dirWatcher;

    /**
     * Constructor for FileTreeView class, sets SelectionModel e.g. "Display chosen file content" and CellFactory
     * method used for constructing TreeItems, rows of the JavaFX TreeView element
     * @param tree JavaFX's TreeView object which will represent Files structure
     * @param fileViewContainer JavaFX's TextArea element used as container to display file content
     * @throws IOException thrown when could not create/read files
     */
    public FileTreeView(TreeView<File> tree, TextArea fileViewContainer) throws IOException
    {
        this.fileTree = tree;
        this.fileView = new FileView(fileViewContainer);
        this.fileView.setText("File has not been chosen yet");

        watcher = FileSystems.getDefault().newWatchService();
        dirWatcher = main.localFolder.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        this.
        fileTree.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                        {
                            if(newValue != null)
                            {
                                if(!newValue.getValue().isDirectory()) {
                                    this.fileView.displayFile(newValue.getValue().toPath());
                                }
                            }
                        });


        // Filling treeView is actually taking place in CellFactory, together with refresh() method called in 10 seconds intervals
        fileTree.setCellFactory(
                        (e) -> new TreeCell<File>() {
                            @Override
                            protected void updateItem(File item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                if(item != null)
                                {
                                    setText(item.getName());
                                    setGraphic(getTreeItem().getGraphic());
                                    File[] children = item.listFiles();
                                    if (children != null) {
                                        for (File child : children) {
                                            TreeItem<File> temp = new TreeItem<>(child);
                                            if (!isInTree(this.getTreeItem(), temp)) {
                                                this.getTreeItem().getChildren().add(temp);
                                            }
                                        }
                                    }

                                    ArrayList<Integer> toDelete = new ArrayList<>();
                                    for(TreeItem<File> child : this.getTreeItem().getChildren())
                                    {
                                        if(!child.getValue().exists())
                                        {
                                            toDelete.add(this.getTreeItem().getChildren().indexOf(child));
                                        }
                                    }

                                    for(int i = toDelete.size()-1; i >= 0; --i)
                                    {
                                        this.getTreeItem().getChildren().remove(toDelete.get(i).intValue());
                                    }
                                    toDelete.clear();

                                } else
                                {
                                    setText("");
                                    setGraphic(null);
                                }
                            }
                        });

        fileTree.setRoot(new TreeItem<>(new File(main.localFolder.toString())));
        fileTree.getRoot().setExpanded(true);
        updateScheduler = new Timer();
        updater = new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            Platform.runLater(() -> {
                            if(fileTree != null)
                            {
                                fileTree.refresh();
                            }
                            else
                                this.cancel();});
                        }
                    };
        updateScheduler.schedule( updater, 1000,1000);
    }

    private boolean isInTree(TreeItem<File> root, TreeItem<File> wanted)
    {
        if(wanted != null) {
            if(root.getValue().toPath().toString().equals(wanted.getValue().toPath().toString())) return true;
            ObservableList<TreeItem<File>> children = root.getChildren();
            if (children != null) {
                for (TreeItem<File> child : children) {
                    if(isInTree(child, wanted)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Method constructing file structure in form of file list omitting directories
     * @return created list of files in flat structure from *this* FileTreeView
     */
    public ArrayList<File> FlatFileStructure()
    {
        ArrayList<File> files = new ArrayList<>();
        RecFlat(fileTree.getRoot().getValue(), files);
        return files;
    }

    private void RecFlat(File item, ArrayList<File> list)
    {
        File[] children = item.listFiles();
        if(children == null && !item.isDirectory()) {
            list.add(item);
        }
        else
            if(children != null)
                for(File child : children)
                {
                    RecFlat(child, list);
                }
    }
}
