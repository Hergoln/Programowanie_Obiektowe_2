package sample.Client;

import sample.Action;
import sample.FileWrapper;
import sample.GUI.MainWindow;
import sample.GUI.ShareButton;
import sample.main;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client handler of output stream, responsible for sending files from client while
 * sharing files or change in file structure was detected
 */
public class OutHandler extends TimerTask
{
    private final WatchService watchService;
    private final Path localFolder;
    private final ObjectOutputStream out;

    private final ShareButton shareButton;
    private ConcurrentHashMap<Path, ArrayList<String>> sharedFiles = new ConcurrentHashMap<>();

    @Override
    public void run()
    {
        try {
            WatchKey key;
            key = watchService.poll();
            if (key != null) {
                Action action = Action.DELETE;
                List<WatchEvent<?>> events = key.pollEvents();
                HashMap<Path, Action> uniqueEvents = new HashMap<>();
                for (WatchEvent<?> event : events) {
                    switch (event.kind().name()) {
                        case "ENTRY_CREATE":
                            action = Action.CREATE;
                            break;
                        case "ENTRY_MODIFY":
                            action = Action.MODIFY;
                            break;
                        case "ENTRY_DELETE":
                            action = Action.DELETE;
                            break;
                    }
                    Path path = ((WatchEvent<Path>) event).context();

                    if (uniqueEvents.containsKey(path)) {
                        uniqueEvents.replace(path, action);
                    } else uniqueEvents.put(path, action);
                }

                for (Map.Entry<Path, Action> entry : uniqueEvents.entrySet()) {
                    File temp = new File(entry.getKey().toString());
                    if (false == temp.isDirectory()) {
                        synchronized (out) {
                            out.writeObject(entry.getValue());
                            FileWrapper sendFile;
                            if (entry.getValue().name().equals("DELETE"))
                                sendFile = new FileWrapper(main.username, entry.getKey().toString(), "");
                            else {
                                sendFile = new FileWrapper(main.username, entry.getKey().toString(), localFolder.toString());
                                if (sharedFiles.contains(entry.getKey())) {
                                    sendFile.perms = new String[sharedFiles.get(entry.getKey()).size()];
                                }
                            }
                            out.writeObject(sendFile);
                        }
                    } else System.out.println("Entry is directory");
                }

                if (!key.reset()) {
                    System.out.println("Key watching main dir reset, dunno why and what does it mean :/");
                }
            }

            for(Path path : sharedFiles.keySet())
            {
                String relativePath = path.toString().replace(localFolder.toString()+File.separator, "");
                String[] perms = new String[sharedFiles.get(path).size()];
                System.out.println("Shared files: " + path.getFileName() + "; " + sharedFiles.get(path));
                for(int i = sharedFiles.get(path).size()-1; i >= 0; --i)
                {
                    perms[i] = sharedFiles.get(path).get(i);
                    sharedFiles.get(path).remove(i);
                }

                FileWrapper sendFile = new FileWrapper(main.username, relativePath, localFolder.toString(), perms);
                out.writeObject(Action.MODIFY);
                out.writeObject(sendFile);
            }

            sharedFiles.clear();

        }
        catch(IOException IOExc)
        {
            System.out.println("Handler IOException");
            return;
        }
    }

    /**
     *
     * @param watchService swatch service responsible for monitoring changes in file structure
     * @param localFolder path to local main folder given at the start of an app
     * @param out output stream of socket connected with server
     * @param shareButton reference to button responsible for sharing files action
     */
    public OutHandler(WatchService watchService, Path localFolder, ObjectOutputStream out, ShareButton shareButton) {
        this.watchService = watchService;
        this.localFolder = localFolder;
        this.out = out;

        this.shareButton = shareButton;

        shareButton.setDisable(false);
        shareButton.setVisible(true);

        shareButton.setOnAction(event ->
                {
                    if(shareButton.selectedFile().isDirectory())
                    {
                        synchronized (MainWindow.console)
                        {
                            MainWindow.console.Inform("Selected File is a directory");
                        }
                    }
                    else {
                        if (sharedFiles.containsKey(shareButton.selectedFile().toPath())) {
                            if (false == sharedFiles.get(shareButton.selectedFile().toPath()).contains(shareButton.selectedUser()))
                                sharedFiles.get(shareButton.selectedFile().toPath()).add(shareButton.selectedUser());
                        } else {
                            ArrayList<String> temp = new ArrayList<>();
                            temp.add(shareButton.selectedUser());
                            sharedFiles.put(shareButton.selectedFile().toPath(), temp);
                        }
                    }
                });
    }

}
