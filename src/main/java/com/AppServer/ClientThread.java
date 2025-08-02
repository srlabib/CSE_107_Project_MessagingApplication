package com.AppServer;

import com.SharedClasses.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientThread implements Runnable{
    private User user;
    Socket socket;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    private final ServerDataHandler dataHandler;
    Object newMessage;
    boolean messagePending;



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
                try {
                    System.out.println("New user created: " + user.getUsername());
                }
                catch (IllegalArgumentException e) {
                    oos.writeObject(e.getMessage());
                    System.err.println("Error creating new user: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                    return; // Exit if user creation fails
                }
                oos.writeObject("successful");
                return;
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
            oos.writeObject("successful");
            oos.writeObject(user);
            Map<String, ChatThread> chatThreads = new ConcurrentHashMap<>();
            HashSet<String> chatThreadIds = user.getChatThreads();
            for(String id : chatThreadIds){
                chatThreads.put(id, dataHandler.getChatThread(id));
            }

            oos.writeObject(chatThreads);


        } catch (IOException e) {
            System.err.println("Error sending success message to client: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException();
        }


        Thread senderThread = new Thread(new MessageSender());
        Thread receiverThread = new Thread(new MessageReceiver());
        Thread activeUserListThread = new Thread(new SendActiveUserList());


        senderThread.start();
        receiverThread.start();
        activeUserListThread.start();

        try {
            senderThread.join();
            receiverThread.join();
            activeUserListThread.join();

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

    public void sendCallRequest(CallRequest callRequest) {
        synchronized (ClientThread.this){
            newMessage = callRequest;
            messagePending = true;
            ClientThread.this.notifyAll();
            System.out.println("Notifying sender thread to send call request: " + callRequest.getSender() + " to " + callRequest.getRecipient());
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
                    switch (obj) {
                        case Message message -> {
                            System.out.println("Message " + message.getContent() + "recieved from user: " + message.getSender() + " to " + message.getReciepent() + " at " + message.getTimestamp());
                            ServerDataHandler.getInstance().addNewMessage(message, user.getUsername());
                        }
                        case String searchUsername -> {
                            System.out.println("Searching for user: " + searchUsername);
                            if (ServerDataHandler.getInstance().seachUser(searchUsername) != null) {
                                oos.writeObject(searchUsername);
                            } else {
                                oos.writeObject("###"); // Indicating that the user was not found
                            }
                        }
                        case CallRequest callRequest -> {
                            System.out.println("Call request received from " + callRequest.getSender() + " to " + callRequest.getRecipient());
                            // Handle the call request, e.g., notify the receiver or log it
                            ServerDataHandler.getInstance().handleCallRequest(callRequest);
                        }
                        case null, default -> {
                            assert obj != null;
                            System.err.println("Received unknown object: " + obj.getClass().getName());
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(user.getUsername()+" disconnected. Message reciver exiting");
                ServerDataHandler.getInstance().removeActiveUser(user.getUsername());
            }
        }
    }

    class SendActiveUserList implements Runnable {
        @Override
        public void run() {
            // Logic for sending the list of active users to the client
            try {
                while (true) {
                    if(oos == null || socket.isClosed()) {
                        break;
                    }
                    Thread.sleep(3000);
                    ArrayList<String> activeUsernames = ServerDataHandler.getInstance().getActiveUsers();

                    oos.writeObject(activeUsernames);
                    oos.flush();
                }
            } catch (IOException e) {
                System.err.println("Error sending active user list: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            } catch (InterruptedException e) {
                System.err.println("SendActiveUserList thread interrupted: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            }
        }
    }
}
