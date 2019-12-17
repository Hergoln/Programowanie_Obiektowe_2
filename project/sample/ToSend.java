package sample;

import sample.GUI.FilesStructure;

/**
 * Secondary class, sent via socket to inform how many files will be sent
 */
public class ToSend extends FilesStructure
{
    /**
     * On how many files client have to wait on
     */
    public int await;

    /**
     * ToSend constructor
     * @param filesToSend collection of files that have to be sent back by the other party
     * @param await int specifying how many files will be sent after this object
     */
    public ToSend(FilesStructure filesToSend, int await)
    {
        super(filesToSend.list);
        this.await = await;
    }
}
