import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import zad8utils.zad8Message;

public class server {
    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(59090)) {
            System.out.println("The date server is running...");
            ExecutorService threadPool = Executors.newFixedThreadPool(8);

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

        Queuerer(Socket _socket){
            this.socket = _socket;
        }

        @Override
        public void run(){
            System.out.println("Connected: " + socket);

            try {
                this.input = new ObjectInputStream(socket.getInputStream());
                this.out = new ObjectOutputStream(socket.getOutputStream());
                ExecutorService threadPool = Executors.newFixedThreadPool(2);
                threadPool.execute(new Sender(messageQueue, this.out, socket));
                inputBytes = null;

                while(true){
                    try{this.inputBytes = input.readObject();} catch(ClassNotFoundException CNFexc) {System.out.println("Data in stream is not an Object"); continue;}
                    if(this.inputBytes instanceof zad8Message){
                        messageQueue.add((zad8Message)this.inputBytes);
                        System.out.println("MESSAGE ADDED");
                        messageQueueInfo(this.messageQueue, this.socket);
                    }
                }
            } finally {
                try{ socket.close(); } catch (IOException e) {}
                System.out.println("Closed: " + socket);
                return;
            }
        }

        private static class Sender implements Runnable{
            ArrayList<zad8Message> messageQueue;
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
                try{
                    //don't know why but I have to write something in console in order to make this thread work properly xD
                    System.out.println("Waiting for pending messages...");
                    while(true){
                        Date currentDate = new Date();
                        for(int i=0; i < this.messageQueue.size(); ++i){
                            if(this.messageQueue.get(i).getReturnTime().before(currentDate)){
                                this.out.writeObject(this.messageQueue.get(i));
                                this.messageQueue.remove(i);
                                messageQueueInfo(this.messageQueue, this.socket);
                            }
                        }
                        if( socket.isClosed() || socket == null) break;
                    }
                }
                catch(IOException IOexc){
                    // there is no need to check other exceptions, they are related to object being serializable
                    // all we send are messages from zad8Message wich implements Serializable interface already
                    System.out.println("Socket closed");
                    return;
                }
            }
        }
    }

    public static void messageQueueInfo(ArrayList<zad8Message> _queue, Socket _socket){
        System.out.println(_socket + 
                        "\n\tQueue length: " + _queue.size() + 
                        "\n\tMessages in queue:");
        for(zad8Message message : _queue){
            System.out.println("\t\tContent: " + message.getMessage() +
                                "\n\t\tReturnDate: " + message.getReturnTime().toString());
        }
    }
}