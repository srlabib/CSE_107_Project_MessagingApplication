package com.messagingapplication;

import com.SharedClasses.ChatListCell;
import com.SharedClasses.ChatThread;
import com.SharedClasses.Message;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDateTime;

import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Map;

public class MainUIController {

    ClientDataHandler clientDataHandler;
    ObjectOutputStream oos;
    String currentChatThreadId;
    public Map<String, ChatThread> chatThreads;

    @FXML
    public void initialize() {
        clientDataHandler = ClientDataHandler.getInstance();
    }

    @FXML private TextField messageInput;
    @FXML private ScrollPane scrollPane;
    @FXML protected ListView<ChatThread> contactList;
    @FXML private Label chatHeader;
    @FXML private TextField searchText;



    private void openChatInbox(VBox chatThreadView, String name) {
        if (chatThreadView != null) {
            scrollPane.setContent(chatThreadView);
            scrollPane.setVvalue(1.0);
            chatHeader.setText(name);
        } else {
            System.err.println("Chat thread view not found for ID: " + currentChatThreadId);
        }

    }


    public void loadUIData(){

        contactList.setItems(ClientDataHandler.getInstance().observableArrayList);
        contactList.setCellFactory(new Callback<ListView<ChatThread>, ListCell<ChatThread>>() {
            @Override
            public ListCell<ChatThread> call(ListView<ChatThread> chatThreadListView) {
                return new ChatListCell();
            }
        });
        contactList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                ChatThread seletedThread = (ChatThread) newSelection;
                currentChatThreadId = seletedThread.getId();
                // Clear the message input field
                messageInput.clear();
                // Load the messages for the selected chat thread
                VBox chatThreadView = clientDataHandler.getChatThreadView(currentChatThreadId);
                openChatInbox(chatThreadView,seletedThread.getRemoteUserName(ClientDataHandler.getInstance().getCurrentUsername()));
            }
        });
        contactList.refresh();
    }

    @FXML
    public void search(ActionEvent e){
        if(searchText.getText().isEmpty())return;
        new searchUserTask(searchText.getText());
    }

    class searchUserTask implements Runnable {
        private String username;

        public searchUserTask(String username) {
            this.username = username;
            new Thread(this).start();
        }

        @Override
        public void run() {
            synchronized (ClientDataHandler.getInstance().searchLock){
                if(username == null || username.isEmpty()) {
                    System.err.println("Username cannot be null or empty.");
                    return;
                }
                if(chatThreads.get(ChatThread.generateID(searchText.getText(),username))!=null) {
                    System.out.println("Chat thread already exists for user: " + username);
                    return; // Chat thread already exists, no need to search
                }
                ClientDataHandler.getInstance().searchResult = null;
                ClientDataHandler.getInstance().resultRecieved = false;
                try {
                    synchronized (oos) {
                        oos.writeObject(username);
                        oos.flush();
                    }
                } catch (IOException e) {
                    System.err.println("Error searching for user: " + e.getMessage());
                }

                // Wait for the response from the server

                while(!ClientDataHandler.getInstance().resultRecieved) {
                    try {
                        ClientDataHandler.getInstance().searchLock.wait();
                    } catch (InterruptedException e) {
                        System.err.println("Search user task interrupted: " + e.getMessage());
                    }
                }
                ClientDataHandler.getInstance().resultRecieved = false;

                // After receiving the result, update the UI
                if(ClientDataHandler.getInstance().searchResult.equals("###")){
                    System.out.println("User not found: " + username);
                    ClientDataHandler.getInstance().searchResult = null;
                } else {
                    System.out.println("User found: " + ClientDataHandler.getInstance().searchResult);
                    VBox chatThreadView = new VBox();
                    chatThreadView.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

                    currentChatThreadId = null;
                    Platform.runLater(() -> {
                        openChatInbox(chatThreadView, ClientDataHandler.getInstance().searchResult);
                    });

                }
            }
        }
    }

    @FXML
    public void send(ActionEvent event){

        String messageText = messageInput.getText();
        if (messageText.isEmpty()) {
            return; // Do not send empty messages
        }
        String reciepent = currentChatThreadId==null?ClientDataHandler.getInstance().searchResult:ClientDataHandler.getInstance().chatThread.get(currentChatThreadId).getRemoteUserName(clientDataHandler.getCurrentUsername());
        if(reciepent == null || reciepent.isEmpty()) {
            System.err.println("No recipient selected for the message.");
            return;
        }
        Runnable sendMessageTask = () -> {
            // Create a new message object
            Message message = new Message(clientDataHandler.currentUser.getUsername(),reciepent,currentChatThreadId ,messageText,LocalDateTime.now());
            System.out.println();
            try {
                synchronized (oos) {
                    System.out.println("Sending message: " + messageText + " to " + reciepent);
                    oos.writeObject(message);
                }
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }

        };
        new Thread(sendMessageTask).start();
    }




    public ScrollPane getScrollPane() {
        return scrollPane;
    }
}
