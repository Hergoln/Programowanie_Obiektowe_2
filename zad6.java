import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.nio.file.*;

class VectorsDifferentLengthException extends Exception{
    int AA, BB;
    public VectorsDifferentLengthException(int _AA, int _BB){
        AA = _AA;
        BB = _BB;
    }

    public String toString(){
        return "First Vector has length " + AA + ", second vector has length " + BB;
    }
}

class zad6 {
    static Vector<Integer> compute(Vector<Integer> first, Vector<Integer> second) throws VectorsDifferentLengthException {
        if(first.size() != second.size())
            throw new VectorsDifferentLengthException(first.size(), second.size());
        Vector<Integer> out = new Vector<>();
        for(int i=0; i<first.size(); ++i) {
            out.add(first.elementAt(i) + second.elementAt(i));
        }
        return out;
    }

    public static void main(String[] args) {
        Vector<Integer> firstVector = new Vector<Integer>();
        Vector<Integer> secondVector = new Vector<Integer>();

        Scanner input = new Scanner(System.in);
        String bufferLine;
        String[] buffer;

        System.out.println("Enter first vector: ");
        while (true) {
            bufferLine = input.nextLine();
            if(bufferLine.isEmpty())
            {
                System.out.println("Empty line");
            }
            buffer = bufferLine.split(" ");
            for (String i : buffer) {
                if (!i.isEmpty()) {
                    try {
                        firstVector.add(Integer.parseInt(i));
                    } catch (NumberFormatException nfExc) {
                        // jump in case
                    }
                }
            }
            if (firstVector.size() != 0) {
                break;
            }
            System.out.println("Sequence is not number vector\nenter first vector again");
        }

        System.out.println("Enter second vector: ");
        while (true) {
            bufferLine = input.nextLine();
            if(bufferLine.isEmpty())
            {
                System.out.println("Empty line");
            }
            buffer = bufferLine.split(" ");
            for (String i : buffer) {
                if (!i.isEmpty()) {
                    try {
                        secondVector.add(Integer.parseInt(i));
                    } catch (NumberFormatException nfExc) {
                        // jump in case
                    }
                }
            }
            if (secondVector.size() != 0) {
                break;
            }
            System.out.println("Sequence is not number vector\nenter first vector again");
        }

        try(BufferedWriter buffWrite = Files.newBufferedWriter(Paths.get("zad6uot.txt"), Charset.forName("UTF-8"))){
            Vector<Integer> computed = compute(firstVector, secondVector);
            for(Integer i: computed){
                System.out.print(i+" ");
                buffWrite.write(i+" ");
            }
        }
        catch(VectorsDifferentLengthException vdlExc){
            System.out.println(vdlExc);
        }
        catch(IOException ioExc){
            System.out.println("Couldn't open/create file");
        }
        catch(InvalidPathException pathExc)
        {
            System.out.println("Incorrect Path");
        }
    }
}