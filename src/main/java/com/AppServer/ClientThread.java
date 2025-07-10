package com.AppServer;

import com.CommonClasses.AuthenticationData;
import com.CommonClasses.User;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ClientThread implements Runnable{
    private User user;
    Socket socket;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    private ServerDataHandler dataHandler;

    ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        dataHandler = ServerDataHandler.getInstance();
        new Thread(this).start();
        System.out.println("End of ClientThread constructor, waiting for authentication...");
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


        try {
            oos.writeObject((String)"successful");
        } catch (IOException e) {
            System.err.println("Error sending success message to client: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException();
        }

        Thread senderThread = new Thread(new MessageSender());
        Thread receiverThread = new Thread(new MessageReceiver());
        Thread updatePusherThread = new Thread(new UpdatePusher());


        senderThread.start();
        receiverThread.start();
        updatePusherThread.start();

        try {
            senderThread.join();
            receiverThread.join();
            updatePusherThread.join();
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


    }


    class MessageSender implements Runnable {
        @Override
        public void run() {
            // Logic for sending messages to the client
        }
    }
    class MessageReceiver implements Runnable {
        @Override
        public void run() {
            // Logic for receiving messages from the client
        }
    }
    class UpdatePusher implements Runnable {
        @Override
        public void run() {
            // Logic for pushing updates to the client
        }
    }
}
