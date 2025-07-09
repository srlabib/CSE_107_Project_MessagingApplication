package com.AppServer;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable{
    private User user;
    Socket socket;
    ObjectOutputStream oos;
    ObjectInputStream ois;

    ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        new Thread(this).start();
    }
    public void run(){

        try{
            // authentication logic
            AuthenticationData data = (AuthenticationData)ois.readObject();


        }catch(Exception e){
            // Handle authentication failure
            // send error message to client
            // close Thread

        }


        // If authentication is successful, start the message sender, receiver, and update pusher threads

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
