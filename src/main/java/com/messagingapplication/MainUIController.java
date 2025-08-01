package com.messagingapplication;

import com.SharedClasses.CallRequest;
import com.SharedClasses.ChatListCell;
import com.SharedClasses.ChatThread;
import com.SharedClasses.Message;
import com.messagingapplication.VideoCall.VideoCall;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Map;

public class MainUIController {

    ClientDataHandler clientDataHandler;
    ObjectOutputStream oos;
    String currentChatThreadId;
    public Map<String, ChatThread> chatThreads;
    public boolean isVideoCallActive = false;
    private byte[] selectedImage = null;
    private final int maxImageSize = 1024 * 1024; // 1 MB
    @FXML
    public void initialize() {
        clientDataHandler = ClientDataHandler.getInstance();
        File file = new File("src/main/resources/com/messagingapplication/DefaultProfilePicture/"+ClientDataHandler.getImageID(ClientDataHandler.getInstance().getCurrentUsername())+".png");
        UserProfilePicture.setImage(new Image(file.toURI().toString()));
        userName.setText(clientDataHandler.getCurrentUsername());
    }



    @FXML private TextField messageInput;
    @FXML private ScrollPane scrollPane;
    @FXML protected ListView<ChatThread> contactList;
    @FXML private Label chatHeader;
    @FXML private TextField searchText;
    @FXML private ImageView UserProfilePicture;
    @FXML private ImageView reciepentProfilePicture;
    @FXML private Label userName;
    @FXML private Label statusLabel;


    @FXML
    public void search(ActionEvent e){
        if(searchText.getText().isEmpty())return;
        String searchUsername = searchText.getText();
        if(searchUsername.equals(ClientDataHandler.getInstance().getCurrentUsername())){
            return;
        }
        boolean userExists = false;
        for(int i = 0; i<clientDataHandler.observableArrayList.size(); i++){
            if(clientDataHandler.observableArrayList.get(i).getRemoteUserName(ClientDataHandler.getInstance().getCurrentUsername()).equals(searchUsername)){
                contactList.getSelectionModel().select(i);
                userExists = true;
                break;
            }
        }
        if(!userExists)new searchUserTask(searchText.getText());
    }

    @FXML
    public void send(ActionEvent event){

        String messageText = messageInput.getText();
        if (messageText.isEmpty() && selectedImage == null) {
            return; // Do not send empty messages
        }
        messageInput.clear();
        String reciepent = currentChatThreadId==null?ClientDataHandler.getInstance().searchResult:ClientDataHandler.getInstance().chatThread.get(currentChatThreadId).getRemoteUserName(clientDataHandler.getCurrentUsername());
        if(reciepent == null || reciepent.isEmpty()) {
            System.err.println("No recipient selected for the message.");
            return;
        }
        Runnable sendMessageTask = () -> {
            // Create a new message object
            Message message = new Message(clientDataHandler.currentUser.getUsername(),reciepent,currentChatThreadId ,messageText,LocalDateTime.now(), selectedImage);
            selectedImage = null;
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

    @FXML
    public void videoCall() {
        String currentUser = ClientDataHandler.getInstance().getCurrentUsername();
        if (isVideoCallActive) {
            return;
        }
        if (currentChatThreadId == null) {
            System.err.println("No chat thread selected for video call.");
            return;
        }
        String remoteUser = clientDataHandler.chatThread.get(currentChatThreadId).getRemoteUserName(currentUser);
        if (remoteUser == null || remoteUser.isEmpty()) {
            System.err.println("Remote user not found for video call.");
            return;
        }

        Runnable CallRequenstTask = () -> {
            CallRequest callRequest = new CallRequest(currentUser, remoteUser);
            VideoCall videoCall = new VideoCall(callRequest,oos);
            Instances.videoCall = videoCall;
            videoCall.start();
        };
        new Thread(CallRequenstTask).start();
        System.out.println("Sending Call request: " +  "to" + remoteUser);
    }


    @FXML
    public void sendImage(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if(selectedFile != null) {
            if(selectedFile.length()> maxImageSize) {
                System.err.println("Selected image is too large. Maximum size is " + (maxImageSize / (1024 * 1024)) + " MB.");
                return;
            }
            try {
                selectedImage = Files.readAllBytes(selectedFile.toPath());
                send(null);
            } catch (IOException e) {
                System.err.println("Error sending image: " + e.getMessage());
            }
        } else {
            System.out.println("Image selection cancelled.");
        }
    }



    private void openChatInbox(VBox chatThreadView, String name) {
        if (chatThreadView != null) {
            scrollPane.setContent(chatThreadView);
            scrollPane.setVvalue(1.0);
            chatHeader.setText(name);
            statusLabel.getStyleClass().removeAll("online","offline");
            if(clientDataHandler.isUserActive(name)){
                statusLabel.setText("Active now");
                statusLabel.getStyleClass().addAll( "online");
            }
            else{
                statusLabel.setText("Offline");
                statusLabel.getStyleClass().addAll("offline");
            }
            File file = new File("src/main/resources/com/messagingapplication/DefaultProfilePicture/"+ClientDataHandler.getImageID(name)+".png");
            reciepentProfilePicture.setImage(new Image(file.toURI().toString()));

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
        contactList.getSelectionModel().select(0);
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
//                    System.out.println();
                    JOptionPane.showMessageDialog(null,"User not found: " + username);
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



    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public void  setActive(){
        statusLabel.setText("Active now");
        statusLabel.getStyleClass().removeAll("online","offline");
        statusLabel.getStyleClass().addAll( "online");
    }

    public void setInactive(){
        statusLabel.setText("Offline");
        statusLabel.getStyleClass().removeAll("online","offline");
        statusLabel.getStyleClass().addAll( "offline");
    }


}
