import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.JOptionPane;
import java.text.*;
import zad8Utils.*;

public class client {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Pass server IP and server port");
            return;
        }

        try (Socket socket = new Socket(args[0], Integer.parseInt(args[1])) ) {
          System.out.println("connected to server");
          SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          Scanner scanner = new Scanner(System.in);
          ExecutorService threadPool = Executors.newFixedThreadPool(2);
          String content = null;
          Date date = null;
          ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
          threadPool.execute(new NoteListener(socket));
          
          while(true){
            System.out.println("Enter your message:");
            while(true){
              try{
                content = scanner.nextLine();
                checkContent(content);
              } catch(zad8Exception zad8exc){
                System.out.println(zad8exc + "\nNow enter message without throw.");
                continue;
              }
              break;
            }
            
            System.out.println("When do you want to receive the message?(yyyy-MM-dd HH:mm:ss):");
            while(true){
              try{
                date = format.parse(scanner.nextLine());
                break;
              } catch(ParseException e){
                System.out.println("Wrong date format, use (yyyy-MM-dd HH:mm:ss) format: ");
              }
            }
            zad8Message mess = new zad8Message(content, date);
            out.writeObject(mess);
            System.out.println("sent at: " + (new Date()).toString());
          }
        } catch(NumberFormatException NFexc){
          System.out.println("Second Argument have to be port number");
        }
    }

    private static class NoteListener implements Runnable {
      private Socket socket;
      Object inputBytes;
      NoteListener(Socket _socket){
        this.socket = _socket;
      }

      @Override
      public void run(){
        try(ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){
          while(!socket.isClosed()){
            this.inputBytes = in.readObject();
            if(this.inputBytes instanceof zad8Message){
              JOptionPane.showMessageDialog(null, ((zad8Message)inputBytes).content, ((zad8Message)inputBytes).returnTime.toString(), JOptionPane.PLAIN_MESSAGE);
            }
          }
        } catch(Exception e){System.out.println("wuntek:: Error: " + e);}
      }
    }

    public static void checkContent(String toCheck) throws zad8Exception{
      if(toCheck.indexOf("throw") >= 0) throw new zad8Exception();
    }
}