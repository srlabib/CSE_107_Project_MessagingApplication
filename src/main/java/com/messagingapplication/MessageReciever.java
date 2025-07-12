package com.messagingapplication;

import com.SharedClasses.Message;

import java.io.ObjectInputStream;

public class MessageReciever extends Thread{
    ObjectInputStream ois;
    public MessageReciever(ObjectInputStream ois){
        this.ois = ois;
        this.start();
    }
    @Override
    public void run() {
        try {
            while (true) {
                Object obj = ois.readObject();
                if (obj instanceof Message) {
                    Message message = (Message) obj;
                    ClientDataHandler.getInstance().addNewMessage(message);

                } else {
                    System.err.println("Received unknown object: " + obj.getClass().getName());
                }
            }
        } catch (Exception e) {
            System.out.println("Server disconnected. Message reciver exiting");
        }
    }
}
