package com.messagingapplication;

import com.AppServer.ClientThread;
import com.SharedClasses.ChatListCell;
import com.SharedClasses.ChatThread;
import com.SharedClasses.Message;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;

import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Map;


public class MainUIController {

    ClientDataHandler clientDataHandler;
    ObjectOutputStream oos;
    String currentChatThreadId;
    private SortedList<ChatThread> sortedChatThreads;
    public Map<String, ChatThread> chatThreads;


    @FXML private TextField messageInput;
    @FXML private ScrollPane scrollPane;
    @FXML private ListView contactList;
    @FXML private Label chatHeader;



    public void loadUIData(){
        ObservableList<ChatThread> observableArrayList = javafx.collections.FXCollections.observableArrayList(chatThreads.values());
        sortedChatThreads = new SortedList<>(observableArrayList, Comparator.comparing(ChatThread::getLastUpdated));
        contactList.setItems(sortedChatThreads);
        contactList.setCellFactory(param -> new ChatListCell());
        contactList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                ChatThread seletedThread = (ChatThread) newSelection;
                currentChatThreadId = seletedThread.getId();
                // Clear the message input field
                messageInput.clear();
                // Load the messages for the selected chat thread
                VBox chatThreadView = clientDataHandler.getChatThreadView(currentChatThreadId);
                if (chatThreadView != null) {
                    scrollPane.setContent(chatThreadView);
                    scrollPane.setVvalue(1.0);
                    chatHeader.setText(seletedThread.getRemoteUserName(ClientDataHandler.getInstance().getCurrentUsername()));
                } else {
                    System.err.println("Chat thread view not found for ID: " + currentChatThreadId);
                }
            }
        });
    }


    public void send(ActionEvent event){

        String messageText = messageInput.getText();
        if (messageText.isEmpty()) {
            return; // Do not send empty messages
        }
        Runnable sendMessageTask = () -> {
            // Create a new message object
            Message message = new Message(clientDataHandler.currentUser.getUsername(),currentChatThreadId ,messageText,LocalDateTime.now());
            try {
                oos.writeObject(message);
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }

        };
    }

    public void search(ActionEvent e){
        String searchUsername = messageInput.getText();
        if (searchUsername.isEmpty()) {
            return; // Do not search with empty input
        }

//        try{
////            oos.writeObject();
//        }
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }
}
