package com.messagingapplication;

import com.SharedClasses.CallRequest;
import com.SharedClasses.ChatThread;
import com.SharedClasses.Message;
import com.messagingapplication.VideoCall.VideoCall;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.layout.VBox;

import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Comparator;

public class MessageReciever extends Thread{
    ObjectInputStream ois;
    ObjectOutputStream oos;
    private String currentUser;
    public MessageReciever(ObjectInputStream ois,ObjectOutputStream oos){
        currentUser = ClientDataHandler.getInstance().getCurrentUsername();
        this.ois = ois;
        this.oos = oos;
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
                else if(obj instanceof CallRequest){
                    CallRequest callRequest = (CallRequest) obj;
                    // This is the response of the call request sent by the user
                    if(callRequest.getSender().equals(currentUser)){
                        // A video call thread should be open unless it is closed before receiving the response
                        System.out.println("Call request response received: " + callRequest.getResponse());
                        if(Instances.videoCall!=null){
                            Instances.videoCall.updateCallRequest(callRequest);
                        }

                    }
                    // This is the call request sent by the other user
                    else{
                        System.out.println("Call request received from: " + callRequest.getSender() + " to " + callRequest.getRecipient());
                        Instances.videoCall = new VideoCall(callRequest,oos);
                        Instances.videoCall.start();
                    }
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
