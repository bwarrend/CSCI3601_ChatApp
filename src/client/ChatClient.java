package client;

import java.net.*;
import java.io.*;
import java.util.*;
 
public class ChatClient {
    private String hostname;
    private int port;
    private String userName;
 
    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
    
    //Run 
    public void execute() {
        try {            
            //Create socket and set up a read and write thread 
            Socket socket = new Socket(hostname, port); 
            System.out.println("Connected to the chat server");
 
            new ReadThread(socket, this).start();
            new WriteThread(socket, this).start();
 
        } catch (UnknownHostException ex) {
            System.err.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("I/O Error: " + ex.getMessage());
        }
 
    }
 
    void setUserName(String userName) {
        this.userName = userName;
    }
 
    String getUserName() {
        return this.userName;
    }
 
 
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        
        //Create a chatclient based on user inputted IP and port
        System.out.println("Enter IP: "); 
        String hostname = in.nextLine();
        
        System.out.println("Enter port: ");
        int port = in.nextInt();
 
        ChatClient client = new ChatClient(hostname, port);
        client.execute();
    }
}

class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private ChatClient client;
 
    public ReadThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;
 
        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.err.println("Error getting input stream: " + ex.getMessage());
        }
    }
 
    public void run() {
        while (true) {
            try {
                String response = reader.readLine();
                System.out.println("\n" + response);
                
            } catch (IOException ex) {
                System.err.println("Error reading from server: " + ex.getMessage());
                break;
            }
        }
    }
}

class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private ChatClient client;
 
    public WriteThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;
 
        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.err.println("Error getting output stream: " + ex.getMessage());
        }
    }
 
    public void run() {
        
        Scanner in = new Scanner(System.in);
 
        System.out.println("Enter username: ");
        String userName = in.nextLine();
        client.setUserName(userName);
        writer.println(userName);
 
        String text;
 
        do {
            text =  userName + ": " + in.nextLine();
            writer.println(text);
 
        } while (!text.equals("bye"));
 
        try {
            socket.close();
        } catch (IOException ex) {
 
            System.err.println("Error writing to server: " + ex.getMessage());
        }
    }
}
