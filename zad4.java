import java.io.*;
import java.nio.charset.Charset;
import  java.nio.file.*;
import java.util.*;

class zad4 {
    public static void main(String[] args) {
        Random r = new Random();
        String fileName = "outputIO.txt";
        StringBuilder strB = new StringBuilder();
        for(int i = 0; i < 1000; ++i) {
            strB.append((char)(r.nextInt('z'-'a')+'a'));
        }
        String toWrite = new String(strB);
        long IOtimeWrite, IOtimeRead, NIOtimeWrite, NIOtimeRead, startTime, stopTime;

        try{
            //IO read/write file functionality
            FileWriter fileW = new FileWriter(fileName);
            startTime = System.nanoTime();
            fileW.write(toWrite);
            stopTime = System.nanoTime();
            IOtimeWrite = stopTime - startTime;
            fileW.close();

            FileReader fileR = new FileReader(fileName);
            startTime = System.nanoTime();
            while(fileR.ready()) {
                System.out.print((char)fileR.read());
            }
            stopTime = System.nanoTime();
            IOtimeRead = stopTime - startTime;
            fileR.close();

            // clearing file
            FileWriter filo = new FileWriter(fileName);
            filo.write(' ');
            filo.close();
            System.out.println(' ');

            //NIO read/write file functionality
            Path newPath = Paths.get("outputIO.txt");
            BufferedWriter writer = Files.newBufferedWriter(newPath, Charset.forName("UTF-8"));

            startTime = System.nanoTime();
            writer.write(toWrite);
            stopTime = System.nanoTime();
            NIOtimeWrite = stopTime - startTime;
            writer.close();

            BufferedReader reader = Files.newBufferedReader(newPath, Charset.forName("UTF-8"));
            startTime = System.nanoTime();
            while(reader.ready()){
                System.out.print((char)reader.read());
            }
            stopTime = System.nanoTime();
            NIOtimeRead = stopTime - startTime;
            reader.close();
        }
        catch(IOException ioExc) {
            System.out.println("Can't find file to write to or read from");
            return;
        }

        System.out.println("\nTime in microseconds");
        System.out.println("Writing IO: " + IOtimeWrite/1000);
        System.out.println("Reading IO: " + IOtimeRead/1000);
        System.out.println("Writing NIO: " + NIOtimeWrite/1000);
        System.out.println("Reading NIO: " + NIOtimeRead/1000);
    }
}