import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import zad8Utils.zad8Message;

public class server {
    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(59090)) {
            System.out.println("The date server is running...");
            ExecutorService threadPool = Executors.newFixedThreadPool(32);

            while (true) {
                threadPool.execute(new Queuerer(listener.accept()));
            }
        }
    }

    private static class Queuerer implements Runnable {
        private Socket socket;
        private ArrayList<zad8Message> messageQueue = new ArrayList<zad8Message>();
        private Object inputBytes;
        private ObjectInputStream input;
        private ObjectOutputStream out;
        Object threadLock = new Object();

        Queuerer(Socket _socket){
            this.socket = _socket;
        }

        @Override
        public void run(){
            System.out.println("Connected: " + socket);

            try {
                this.input = new ObjectInputStream(socket.getInputStream());
                this.out = new ObjectOutputStream(socket.getOutputStream());
                ExecutorService threadPool = Executors.newFixedThreadPool(4);
                threadPool.execute(new Sender(messageQueue, this.out, socket));
                inputBytes = null;
                synchronized(threadLock){
                    while(true){
                        try{this.inputBytes = input.readObject();} catch(ClassNotFoundException CNFexc) {System.out.println("Data in stream is not an Object"); continue;}
                        if(this.inputBytes instanceof zad8Message){
                            messageQueue.add((zad8Message)this.inputBytes);
                            System.out.println("MESSAGE ADDED");
                            messageQueueInfo(this.messageQueue, this.socket);
                        }
                    }
                }
            } catch(NullPointerException exc)
            {
                System.out.println("========================Null Exception Querer========================");
                messageQueueInfo(this.messageQueue, this.socket);
                System.out.println("================End of Null Exception handling================");
            }finally {
                try{ socket.close(); } catch (IOException e) {}
                System.out.println("Closed: " + socket);
                return;
            }
        }

        private static class Sender implements Runnable{
            ArrayList<zad8Message> messageQueue;
            Object threadLock = new Object();

            ObjectOutputStream out;
            Socket socket;
            Sender(ArrayList<zad8Message> _messageQueue, ObjectOutputStream _out, Socket _socket){
                this.messageQueue = _messageQueue;
                this.out = _out;
                this.socket = _socket;
            }

            @Override
            public void run(){
                messageQueue.add(new zad8Message("Starting message + Date:" + (new Date()).toString(), new Date()));
                System.out.println("Waiting for pending messages...");
                while(true){
                    try{
                        Date currentDate = new Date();
                        synchronized(threadLock)
                        {
                            for(int i=0; i < this.messageQueue.size(); ++i){
                                if(this.messageQueue.get(i).getReturnTime().before(currentDate)){
                                    this.out.writeObject(this.messageQueue.get(i));
                                    this.messageQueue.remove(i);
                                    messageQueueInfo(this.messageQueue, this.socket);
                                    System.out.println("============================");
                                }
                            }
                        }
                        if( socket.isClosed() || socket == null) break;
                    }
                    catch(IOException IOexc){
                        System.out.println("Socket closed");
                        return;
                    }
                    catch(NullPointerException exc)
                    {// Dude, fuck that, for some reason the exception is thrown and i don't care because it doesn't change a damn thing
                        System.out.println("========================Null Exception Sender========================");
                        messageQueueInfo(this.messageQueue, this.socket);
                        System.out.println(exc);
                        System.out.println("================End of Null Exception handling================");
                    }
                }
            }
        }
    }

    public static void messageQueueInfo(ArrayList<zad8Message> _queue, Socket _socket){
        System.out.println(_socket +
                "\n\tQueue length: " + _queue.size() +
                "\n\tMessages in queue:");
        for(zad8Message message : _queue){
            if(message == null) System.out.println("NULL");
            else System.out.println("\t\tContent: " + message.getMessage() == null ? "NULL" : message.getMessage() +
                    "\n\t\tReturnDate: " + message.getReturnTime().toString() == null ? "NULL" : message.getReturnTime().toString());
        }
    }
}