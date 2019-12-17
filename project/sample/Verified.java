package sample;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class used for server - client communication, object of this class determines if user
 * with username client want to log in is in users list and if it is then if it is
 * already in use.
 */
public class Verified implements Serializable
{
    /**
     * Flag that determines if username given by client is in users list on the server
     */
    public Boolean verified;
    /**
     * Flag that determines if user that client want to have access to is already used
     */
    public Boolean inUse;
    /**
     * List of users from server
     */
    public ArrayList<String> usersList;

    /**
     * Standard constructor
     * @param verified indicates if client is registered as user
     * @param inUse indicates if client is in use
     * @param usersList list of registered users names
     */
    public Verified(Boolean verified, Boolean inUse, ArrayList<String> usersList) {
        this.verified = verified;
        this.inUse = inUse;
        this.usersList = usersList;
    }
}
