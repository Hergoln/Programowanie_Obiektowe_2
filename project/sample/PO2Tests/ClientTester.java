package sample.PO2Tests;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.junit.*;
import sample.Action;
import sample.Client.Receiver;
import sample.FileWrapper;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Client testing class
 */
public class ClientTester
{
    private static String mainFolder = Paths.get("").toAbsolutePath().toString() + File.separator + "ProjectTest";
    @BeforeClass
    public static void beforeClass()
    {
            System.out.println("Client tests started");
            File file = new File(mainFolder);
            file.mkdir();
    }

    @Test (expected = NullPointerException.class)
    public void testReceiverAdding_fail_nulls()
    {
        sample.Client.Receiver rec = new Receiver(null, null, null);
        rec.run();
    }

    @Test
    public void testReceiverAdding_success()
    {
        try{
            String testFile = null;
            testFile = CreateTestFile(1, "Test text");
            FileWrapper temp = new FileWrapper("testGuy", testFile, mainFolder);
            File toDelete = new File(mainFolder + File.separator + "clientTestFile"+1);
            toDelete.delete();
            sample.Client.Receiver rec = new Receiver(temp, Paths.get(mainFolder), Action.CREATE);
            rec.run();

            File again = new File(mainFolder + File.separator + "clientTestFile"+1);
            assertTrue(again.exists());
        } catch (IOException e) {System.out.println("something with creation");}
    }

    @Test
    public void testReceiverDeleting_success()
    {
        try{
            String testFile = null;
            testFile = CreateTestFile(2, "Test text");
            FileWrapper temp = new FileWrapper("testGuy", testFile, "");
            sample.Client.Receiver rec = new Receiver(temp, Paths.get(mainFolder), Action.DELETE);
            rec.run();

            File again = new File(mainFolder + File.separator + "clientTestFile"+2);
            assertTrue(!again.exists());
        } catch (IOException e) {System.out.println("something with creation");}
    }

    @Test
    public void testReceiverUpdating()
    {
        try{
            String testFile = null;
            testFile = CreateTestFile(3, "Test text AFTER update");
            FileWrapper temp = new FileWrapper("testGuy", testFile, mainFolder);

            File file = new File(mainFolder + File.separator + testFile);
            BufferedWriter buff = new BufferedWriter(new FileWriter(file));
            buff.write("Test text BEFORE update");
            buff.newLine();
            buff.flush();

            sample.Client.Receiver rec = new Receiver(temp, Paths.get(mainFolder), Action.MODIFY);
            rec.run();

            BufferedReader buffe = new BufferedReader(new FileReader(file));
            assertEquals("Test text AFTER update", buffe.readLine());
        } catch (IOException e) {System.out.println("something with creation");}
    }

    @AfterClass
    public static void afterClass()
    {
        try {
            deleteDirectoryRecursion(new File(mainFolder));
            System.out.println("Client tests ended");
        }
        catch (IOException e)
        {
            System.out.println("Cleaning went wrong");
        }

    }

    public static String CreateTestFile(int number, String text) throws IOException
    {
        File testFile = new File(mainFolder + File.separator + "clientTestFile"+number);
        testFile.createNewFile();
        BufferedWriter buff = new BufferedWriter(new FileWriter(testFile));
        buff.write(text);
        buff.newLine();
        buff.flush();
        return "clientTestFile"+number;
    }

    public static void deleteDirectoryRecursion(File file) throws IOException {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectoryRecursion(entry);
                }
            }
        }
        if (!file.delete()) {
            System.out.println("don't know why it don't want to delete this files");
            throw new IOException("Failed to delete " + file);
        }
    }
}
