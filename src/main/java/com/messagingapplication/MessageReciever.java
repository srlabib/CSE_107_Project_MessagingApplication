package com.messagingapplication;

import com.SharedClasses.ChatThread;
import com.SharedClasses.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.layout.VBox;

import java.io.ObjectInputStream;
import java.util.Comparator;

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
                    System.out.println("New message received: " + ((Message) obj).getContent());
                    Message message = (Message) obj;
                    ClientDataHandler.getInstance().addNewMessage(message);
                    Platform.runLater(() -> {
                        FXCollections.sort(ClientDataHandler.getInstance().observableArrayList, Comparator.comparing(ChatThread::getLastUpdated).reversed());
                        ClientDataHandler.getInstance().uiController.contactList.refresh();
                    });
                }
                else if(obj instanceof String){
                    ClientDataHandler.getInstance().searchResult = (String) obj;
                    ClientDataHandler.getInstance().resultRecieved = true;
                    System.out.println("Search result received: " + ClientDataHandler.getInstance().searchResult);
                    // Notify the main thread that the search result is available
                    synchronized (ClientDataHandler.getInstance().searchLock){
                        ClientDataHandler.getInstance().searchLock.notifyAll();
                    }
                }
                else if(obj instanceof ChatThread){
                    ChatThread chatThread = (ChatThread) obj;
                    ClientDataHandler.getInstance().chatThread.put(chatThread.getId(),chatThread);
                    ClientDataHandler.getInstance().observableArrayList.add(chatThread);
                    ClientDataHandler.getInstance().addChatThreadView(chatThread);
                    Platform.runLater(() -> {
                        FXCollections.sort(ClientDataHandler.getInstance().observableArrayList, Comparator.comparing(ChatThread::getLastUpdated).reversed());
                        ClientDataHandler.getInstance().uiController.contactList.refresh();
                        ClientDataHandler.getInstance().uiController.contactList.getSelectionModel().select(0);
                    });
                    System.out.println("New chat thread received: " + ((ChatThread) obj).getId());
                }

                else {
                    System.err.println("Received unknown object: " + obj.getClass().getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Server disconnected. Message reciver exiting");
        }
    }
}
