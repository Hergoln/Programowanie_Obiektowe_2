import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

class zad5 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Random r = new Random();
        int rval = 0, temp = 0;
        try {
            System.out.print("Enter path: ");
            Path filePath = Paths.get(input.nextLine());
            BufferedWriter buffWrite = Files.newBufferedWriter(Paths.get("fileName.txt"), Charset.forName("UTF-8"));
            buffWrite.write(filePath.toString());
            buffWrite.close();

            BufferedReader buffRead = Files.newBufferedReader(filePath, Charset.forName("UTF-8"));
            StringBuffer contentBuff = new StringBuffer();
            while(buffRead.ready()){
                contentBuff.append((char)buffRead.read());
            }
            buffRead.close();
            String content = new String(contentBuff);

            System.out.println("Type something to get letters from file:");
            while(true){

                if(input.hasNext()) {
                    input.next();
                    temp = r.nextInt(4)+1;
                    System.out.println(content.substring(rval, rval + temp));
                    rval += temp;
                }
            }
        }
        catch(InvalidPathException pathExc){
            System.out.println("Incorrect Path");
        }
        catch (IOException ioExc){
            System.out.println("Couldn't open/create file");
        }
        catch(IndexOutOfBoundsException IOOBexc)
        {
            System.out.println("You reached end of file");
            System.exit(0);
        }
    }
}