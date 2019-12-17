package sample.GUI;

import javafx.scene.control.TextArea;
import sample.main;

/**
 * Class controlling fxml object operating as console for app information
 */
public class Console
{
    private TextArea wrapper;

    Console(TextArea wrapper)
    {
        this.wrapper = wrapper;
        this.wrapper.setWrapText(true);
        this.wrapper.setEditable(false);
        Inform("Welcome in app " + main.username +"\nYour local folder: " + main.localFolder);
    }

    /**
     * @param message String to be displayed in console
     */
    public void Inform(String message)
    {
        wrapper.appendText(message + "\n");
    }
}
