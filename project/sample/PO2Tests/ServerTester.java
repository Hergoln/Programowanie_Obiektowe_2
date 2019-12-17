package sample.PO2Tests;

import org.junit.*;
import sample.FileWrapper;
import sample.Server.DiscController;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Server testing class
 */
public class ServerTester
{
    private static DiscController discController;
    private static String mainFolder = null;
    private static String testFilePathOutOfDisc = null;

    @BeforeClass
    public static void beforeClass()
    {
        System.out.println("Start Server tests");
        mainFolder = Paths.get("").toAbsolutePath().toString() + File.separator + "ProjectTest";
        testFilePathOutOfDisc = mainFolder + File.separator + "testFile.txt";
    }

    @Test
    public void testDiscControllerConstructor()
    {
        File file = null;
        try {
            file = new File(mainFolder);
            file.mkdir();
            discController = new DiscController(file.toPath());
        }
        catch (IOException IOExc)
        {
            System.out.println("IOException in tests: " + IOExc);
        }

        assertTrue(file.isDirectory());
        for(File f : file.listFiles())
        {
            if(f.getName().contains("disc")) assertTrue(f.isDirectory());
        }
    }

    @Test
    public void testAllocateFile_success()
    {
        try {
            File testFile = new File(testFilePathOutOfDisc);
            testFile.createNewFile();
            BufferedWriter buff = new BufferedWriter(new FileWriter(testFile));
            buff.write("Test text");
            buff.newLine();
            buff.flush();
            buff = null;
            discController.AllocateFile(new FileWrapper("testUser", "testFile.txt", mainFolder));

            File readTestFile = new File(Paths.get("").toAbsolutePath().toString()
                                            + File.separator + "ProjectTest" + File.separator + "disc0" + File.separator + "testFile.txt");
            BufferedReader buffe = new BufferedReader(new FileReader(readTestFile));
            assertEquals("Test text", buffe.readLine());
        }
        catch (IOException IOExc)
        {
            System.out.println("Could not create file");
        }
    }

    @Test
    public void testUpdateFile_defeat_allocateNew()
    {
        try {
            File testFile = new File(testFilePathOutOfDisc);
            BufferedWriter buff = new BufferedWriter(new FileWriter(testFile));
            buff.write("Test text for test UpdateFile method");
            buff.newLine();
            buff.flush();
            buff = null;
            discController.UpdateFile(new FileWrapper("testUser", "testFile.txt", mainFolder));

            File readTestFile = new File(Paths.get("").toAbsolutePath().toString()
                    + File.separator + "ProjectTest" + File.separator + "disc0" + File.separator + "testFile.txt");
            BufferedReader buffe = new BufferedReader(new FileReader(readTestFile));
            assertEquals("Test text for test UpdateFile method", buffe.readLine());
        }
        catch (IOException IOExc)
        {
            System.out.println("I'm in update file test");
            System.out.println("Could not do some shiet: " + IOExc);
        }
    }

    @Test
    public void testUpdateFile_success()
    {
        try {
            File testFile = new File(mainFolder + File.separator + "updateFile.txt");
            BufferedWriter buff = new BufferedWriter(new FileWriter(testFile));
            buff.write("Test text for test UpdateFile2 method");
            buff.newLine();
            buff.flush();
            buff = null;
            discController.UpdateFile(new FileWrapper("testUser", "updateFile.txt", mainFolder));

            File readTestFile = new File(Paths.get("").toAbsolutePath().toString()
                    + File.separator + "ProjectTest" + File.separator + "disc1" + File.separator + "updateFile.txt");
            BufferedReader buffe = new BufferedReader(new FileReader(readTestFile));
            assertEquals("Test text for test UpdateFile2 method", buffe.readLine());
        }
        catch (IOException IOExc)
        {
            System.out.println("I'm in update file test");
            System.out.println("Could not do some shiet: " + IOExc);
        }
    }


    @Test
    public void testIsIn_success()
    {
        try
        {
            File testFile = new File(mainFolder + File.separator + "testFile2.txt");
            BufferedWriter buff = new BufferedWriter(new FileWriter(testFile));
            buff.write("Test text for test IsIn method");
            buff.newLine();
            buff.flush();
            discController.AllocateFile(new FileWrapper("testUser", "testFile2.txt", mainFolder));
            assertTrue(0 <= discController.IsIn("testFile2.txt"));
        }
        catch (IOException IOExc)
        {
            System.out.println("jebac cie IsIn");
        }
    }

    @Test
    public void testIsIn_defeat()
    {
        try
        {
            File testFile = new File(mainFolder + File.separator + "testFile3.txt");
            BufferedWriter buff = new BufferedWriter(new FileWriter(testFile));
            buff.write("Test text for test IsIn method");
            buff.newLine();
            buff.flush();
            discController.AllocateFile(new FileWrapper("testUser", "testFile3.txt", mainFolder));
            assertTrue(0 > discController.IsIn("wrongFileName.txt"));
        }
        catch (IOException IOExc)
        {
            System.out.println("jebac cie IsIn");
        }
    }

    @Test
    public void testDeallocateFile()
    {
        try
        {
            File testFile = new File(mainFolder + File.separator + "testFile4.txt");
            BufferedWriter buff = new BufferedWriter(new FileWriter(testFile));
            buff.write("Test text for test IsIn method");
            buff.newLine();
            buff.flush();
            discController.AllocateFile(new FileWrapper("testUser", "testFile4.txt", mainFolder));
            discController.DeallocateFile(new FileWrapper("testUser", "testFile4.txt", ""));
            assertTrue(0 > discController.IsIn("testFile4.txt"));
        }
        catch (IOException IOExc)
        {
            System.out.println("jebac cie deallocate");
        }
    }

    @AfterClass
    public static void afterClass()
    {
        try {
            discController = null;
            File file = new File(Paths.get("").toAbsolutePath().toString() + File.separator + "ProjectTest");
            deleteDirectoryRecursion(file);
            System.out.println("Server tests ended");
        }
        catch(IOException IOExc)
        {
            System.out.println("IOException in afterClasss: "+IOExc);
        }
    }

    // something wrong with this one :/
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
