package com.AppServer;

import com.SharedClasses.AuthenticationData;
import com.SharedClasses.ChatThread;
import com.SharedClasses.Message;
import com.SharedClasses.User;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientThread implements Runnable{
    private User user;
    Socket socket;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    private ServerDataHandler dataHandler;
    Object newMessage;
    boolean messagePending;
    private Thread senderThread;
    private Thread receiverThread;
    private MessageSender sender;



    ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        dataHandler = ServerDataHandler.getInstance();
        new Thread(this).start();
        System.out.println("End of ClientThread constructor, waiting for authentication...");
        messagePending = false;
        newMessage = null;
    }

    public void run(){
        System.out.println("New client connected: " + socket.getInetAddress() + ":" + socket.getPort());
        try{
            // authentication logic
            AuthenticationData data = (AuthenticationData)ois.readObject();
            System.out.println("User: " + data.username + " is trying to login");
            if(data.newAccount){
                user = dataHandler.createUser(data.username, data.password, data.name);
                System.out.println("New user created: " + user.getUsername());
            }
            else{
                user = dataHandler.seachUser(data.username);
                if(user == null){
                    throw new IllegalArgumentException("No user found with username: " + data.username);
                }

                user.verifyPassword(data.password);
            }

        }catch(IllegalArgumentException  e){
            try {
                oos.writeObject(e.getMessage());
            } catch (IOException ex) {
                System.err.println("Error sending authentication error message to client: " + ex.getMessage());
            }
            // Handle authentication failure
            // send error message to client
            // close Thread
            System.err.println("Authentication failed: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            return;
        }
        catch (IOException | ClassNotFoundException e) {
            System.err.println("Error during authentication: " + e.getMessage()+ "\n" + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException();
        }

        System.out.println("User: " + user.getUsername() + " has successfully logged in");

        ServerDataHandler.getInstance().addActiveUser(user.getUsername(), this);

        try {
            oos.writeObject((String)"successful");
            oos.writeObject(user);
            Map<String, ChatThread> chatThreads = new ConcurrentHashMap<String, ChatThread>();
            HashSet<String> chatThreadIds = user.getChatThreads();
            for(String id : chatThreadIds){
                chatThreads.put(id, dataHandler.getChatThread(id));
            }

            oos.writeObject(chatThreads);


        } catch (IOException e) {
            System.err.println("Error sending success message to client: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException();
        }


        senderThread = new Thread(sender = new MessageSender());
        receiverThread = new Thread(new MessageReceiver());


        senderThread.start();
        receiverThread.start();

        try {
            senderThread.join();
            receiverThread.join();

        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }

        // Close resources
        try {
            ois.close();
            oos.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }

        System.err.println("User : "+user.getUsername()+" has disconnected");
        ServerDataHandler.getInstance().removeActiveUser(user.getUsername());


    }

    void sendMessage(Message message) {
        synchronized (ClientThread.this){
            newMessage = message;
            messagePending = true;
            ClientThread.this.notifyAll();
        }
    }

    public void sendNewChatThread(ChatThread chatThread) {
        synchronized (ClientThread.this){
            newMessage = chatThread;
            messagePending = true;
            ClientThread.this.notifyAll();
            System.out.println("Notifying sender thread to send new chat thread: " + chatThread.getId());
        }
    }


    class MessageSender implements Runnable {
        @Override
        public void run() {
            // Logic for sending messages to the client
            while(true) {
                try {
                    synchronized (ClientThread.this) {
                        while (!messagePending) {
                            ClientThread.this.wait();
                        }
                        System.out.println("Sender thread notified to send message");
                        if (newMessage != null) {
                            oos.writeObject(newMessage);
                            oos.flush();
                            messagePending = false;
                            newMessage = null;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error sending message to client: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                    break;
                } catch (InterruptedException e) {
                    System.err.println("Sender thread interrupted: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                    break;
                }
            }
        }
    }
    class MessageReceiver implements Runnable {
        @Override
        public void run() {
            // Logic for receiving messages from the client
            try {
                while (true) {
                    Object obj = ois.readObject();
                    if (obj instanceof Message) {
                        Message message = (Message) obj;
                        System.out.println("Message "+message.getContent()+"recieved from user: " + message.getSender()+" to "+message.getReciepent()+" at "+message.getTimestamp());
                        ServerDataHandler.getInstance().addNewMessage(message,user.getUsername());
                    } else if( obj instanceof String){  // Assuming the object is a String for searching users
                        String searchUsername = (String) obj;
                        System.out.println("Searching for user: " + searchUsername);
                        if(ServerDataHandler.getInstance().seachUser(searchUsername) != null) {
                            oos.writeObject(searchUsername);
                        } else {
                            oos.writeObject("###"); // Indicating that the user was not found
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(user.getUsername()+" disconnected. Message reciver exiting");
            }
        }
    }
}
