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

    static void readVector(Vector<Integer> vector){
        Scanner input = new Scanner(System.in);
        String bufferLine;
        String[] buffer;
        while (true) {
            bufferLine = input.nextLine();
            if(bufferLine.isEmpty())
            {
                System.out.println("Empty line");
                continue;
            }
            buffer = bufferLine.split(" ");
            for (String i : buffer) {
                if (!i.isEmpty()) {
                    try {
                        vector.add(Integer.parseInt(i));
                    } catch (NumberFormatException nfExc) {
                        // jump in case
                    }
                }
            }
            if (vector.size() != 0) {
                break;
            }
            System.out.println("Sequence is not number vector\nenter vector again:");
        }
        return;
    }

    public static void main(String[] args) {
        Vector<Integer> firstVector = new Vector<Integer>();
        Vector<Integer> secondVector = new Vector<Integer>();
        Vector<Integer> computed = new Vector<Integer>();
        boolean done = false;
        while(!done){
            System.out.println("Enter first vector: ");
            readVector(firstVector);

            System.out.println("Enter second vector: ");
            readVector(secondVector);
            try{
                computed = compute(firstVector, secondVector);
            }
            catch(VectorsDifferentLengthException vdlExc){
                System.out.println(vdlExc);
                firstVector.clear();
                secondVector.clear();
                continue;
            }
            done = true;
        }

        System.out.print("Computed vector: ");
        try(BufferedWriter buffWrite = Files.newBufferedWriter(Paths.get("zad6uot.txt"), Charset.forName("UTF-8"))){
            for(Integer i: computed){
                System.out.print(i+" ");
                buffWrite.write(i+" ");
            }
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