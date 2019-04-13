import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

class myKeyListener implements KeyListener {
    public void keyTyped(KeyEvent e) {
        System.out.println("Key typed " + e.getKeyCode());
        return;
    }

    public void keyPressed(KeyEvent e) {
        System.out.println("Key pressed " + e.getKeyCode());
        return;
    }

    public void keyReleased(KeyEvent e) {
        System.out.println("Key released " + e.getKeyCode());
        return;
    }
}

class zad5 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Random r = new Random();
        int rval;
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

            while(true){

                if(input.hasNext()) {
                    input.next();
                    rval = r.nextInt(content.length() - 5);
                    System.out.println(content.substring(rval, rval + 5));
                }
            }
        }
        catch(InvalidPathException pathExc){
            System.out.println("Incorrect Path");
        }
        catch (IOException ioExc){
            System.out.println("Couldn't open/create file");
        }
    }
}