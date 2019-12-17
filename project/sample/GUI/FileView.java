package sample.GUI;

import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class used as controller for displaying file content
 */
public class FileView
{
    private TextArea content;

    FileView(TextArea content)
    {
        this.content = content;
        this.content.setWrapText(true);
        this.content.setEditable(false);
    }

    /**
     * Method used to set text in TextArea content
     * @param text String to set as text
     */
    public void setText(String text)
    {
        this.content.setText(text);
    }

    /**
     * Sets content of TextArea as content of File represented by path
     * @param path path to file to display
     */
    public void displayFile(Path path)
    {
        try
        {
            BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"));
            content.setText(path.toString() + "\n\n==============================================\n");
            while(reader.ready())
            {
                content.appendText(reader.readLine() + "\n");
            }
        }
        catch(IOException ioexc)
        {
            MainWindow.console.Inform("File " + path + ", is unreadable or might contain polish alphabet characters xd");
        }
    }
}
