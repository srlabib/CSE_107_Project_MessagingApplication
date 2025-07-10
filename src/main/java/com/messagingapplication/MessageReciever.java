package com.messagingapplication;

import com.CommonClasses.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
            e.printStackTrace();
        }
    }
}
