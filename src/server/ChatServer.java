package server;
import java.io.*;
import java.net.*;
import java.util.*;
 
public class ChatServer {
    private int port;
    private Set<String> userNames = new HashSet<>();
    private Set<UserThread> userThreads = new HashSet<>();
 
    public ChatServer(int port) {
        this.port = port;
    }
    
    
    //Run
    public void execute() {
        
        //Runs the server, creates a new thread for every user and adds
        // the user to a hash set of new users
        try (ServerSocket serverSocket = new ServerSocket(port)) { 
            System.out.println("Server is listening on port " + port);
 
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");
 
                UserThread newUser = new UserThread(socket, this);
                userThreads.add(newUser);
                newUser.start();
 
            }
 
        } catch (IOException ex) {
            System.err.println("Error in the server: " + ex.getMessage());
        }
    }
 
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.println("Enter a port number: ");
        
        int port = in.nextInt();
        ChatServer server = new ChatServer(port);
        server.execute();
    }
 
    //Broadcast message to all users but the sending user
    void broadcast(String message, UserThread excludeUser) {
        for (UserThread aUser : userThreads) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
    }
 
    //Adds user name to list of users
    void addUserName(String userName) {
        userNames.add(userName);
    }
 
    //Removes user from userThreads hash set
    void removeUser(String userName, UserThread aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            userThreads.remove(aUser);
            System.out.println("The user " + userName + " has quit");
        }
    }
    
    
    //Return connected users
    Set<String> getUserNames() {
        return this.userNames;
    }
 
    //Return whether we have any connected users
    boolean hasUsers() {
        return !this.userNames.isEmpty();
    }
}


//Handles each user in a seperate thread
class UserThread extends Thread {
    private Socket socket;
    private ChatServer server;
    private PrintWriter writer;
 
    public UserThread(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }
 
    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
 
            printUsers();
 
            String userName = reader.readLine();
            server.addUserName(userName);
 
            String serverMessage = "New user connected: " + userName;
            server.broadcast(serverMessage, this);
 
            String clientMessage;
 
            do {
                clientMessage = reader.readLine();
                serverMessage = clientMessage;
                server.broadcast(serverMessage, this);
 
            } while (!clientMessage.equals("bye"));
 
            server.removeUser(userName, this);
            socket.close();
 
            serverMessage = userName + " has quitted.";
            server.broadcast(serverMessage, this);
 
        } catch (IOException ex) {
            System.err.println("Error in UserThread: " + ex.getMessage());
        }
    }
 
    //Sends a list of all connected users
    void printUsers() {
        if (server.hasUsers()) {
            writer.println("Connected users: " + server.getUserNames());
        } else {
            writer.println("No other users connected");
        }
    }
 
    //Sends a message
    void sendMessage(String message) {
        writer.println(message);
    }
}

