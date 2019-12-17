package sample.GUI;

import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;

import java.util.ArrayList;

/**
 * Wrapper class for ChoiceBox containing list of accessible users
 */
public class UsersList
{
    private ChoiceBox<String> list;

    /**
     * @param box reference to ChoiceBox
     */
    public UsersList(ChoiceBox<String> box)
    {
        this.list = box;
    }

    /**
     * Sets list of elements in *list*
     * @param list list of Strings to set as elements of *list*
     */
    public void SetUsersList(ArrayList<String> list)
    {
        this.list.setItems(FXCollections.observableArrayList(list));
        this.list.getSelectionModel().select(0);
    }

    /**
     * @return currently chosen user
     */
    public String GetChoice()
    {
        return this.list.getValue();
    }

}
