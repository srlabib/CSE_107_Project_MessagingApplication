package com.messagingapplication;

import com.SharedClasses.ChatThread;
import com.SharedClasses.Message;
import com.SharedClasses.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientDataHandler {
    public Map<String, ChatThread> chatThread = new ConcurrentHashMap<String, ChatThread>();
    // This VBox to hold all chats to be displayed in the main UI
    private Map<String, VBox> chatThreadViews = new ConcurrentHashMap<String, VBox>();
    public ScrollPane scrollPane;

    private static ClientDataHandler INSTANCE;
    User currentUser;
    public String searchResult;
    public boolean resultRecieved = false;
    final public Object searchLock = new Object();
    public ObservableList<ChatThread> observableArrayList;
    public MainUIController uiController;



    public static ClientDataHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClientDataHandler();
        }
        return INSTANCE;
    }


    public VBox getChatThreadView(String chatThreadId) {
        // Returns the VBox for the specified chat thread ID
        return chatThreadViews.get(chatThreadId);
    }
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void loadData(Map<String, ChatThread> pastChatThreads) {
        // Loading previous chat threads from the server

        this.chatThread = pastChatThreads;

        for(var entry : chatThread.entrySet()) {
            VBox chatThreadView = new VBox();
            chatThreadView.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            var chatMessages = entry.getValue().getMessageList();

            LocalDateTime last = null;
            for(Message message:chatMessages){
                if(last == null || Duration.between(last, message.getTimestamp()).toMinutes() > 5) {
                    last = message.getTimestamp();
                    addMessageToVbox(message, chatThreadView, true);
                } else {
                    addMessageToVbox(message, chatThreadView, false);
                }
            }

            // Adding a listener to auto-scroll to the bottom when new messages are added
            // A listener is an object that waits for a specific event to occur, such as a change in a property.
            // this listener will be triggered whenever the height of the chatThreadView changes,
            chatThreadView.heightProperty().addListener(obs ->{
                    Platform.runLater(()->scrollPane.setVvalue(1.0)); // Scroll to the bottom
            });

            chatThreadViews.put(entry.getKey(), chatThreadView);
        }

        observableArrayList = javafx.collections.FXCollections.observableArrayList(chatThread.values());
        FXCollections.sort(observableArrayList, Comparator.comparing(ChatThread::getLastUpdated).reversed());

    }

    public String getCurrentUsername() {
        if (currentUser == null) {
            throw new IllegalStateException("Current user is not set.");
        }
        return currentUser.getUsername();
    }

    public void clearData() {
        // Clear the chat threads data
        chatThread.clear();
        currentUser = null;
    }

    public void addNewMessage(Message message){
        // Generate the chat thread ID based on the current user and the message sender
        String chatThreadId = message.getThreadID();
        // Search for the chat thread in the existing threads
        ChatThread chatThread = this.chatThread.get(chatThreadId);

        if (chatThread == null) {
            // If the chat thread does not exist, create a new one
            chatThread = new ChatThread(chatThreadId, new String[]{currentUser.getUsername(),message.getSender()});
            this.chatThread.put(chatThreadId, chatThread);

            VBox chatThreadView = new VBox();
            chatThreadView.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            chatThreadViews.put(chatThreadId, chatThreadView);

        }
        // Push the message to the chat thread
        chatThread.pushMessage(message);
        Duration gap = Duration.between(chatThread.getLastUpdated(), message.getTimestamp());
        addMessageToVbox(message,chatThreadViews.get(chatThreadId), gap.toMinutes() > 5);
    }

    public void addChatThreadView(ChatThread chatThread) {
        VBox chatThreadView = new VBox();
        chatThreadView.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        LocalDateTime last = null;
        for(Message message:chatThread.getMessageList()){
            if(last == null || Duration.between(last, message.getTimestamp()).toMinutes() > 5) {
                last = message.getTimestamp();
                addMessageToVbox(message, chatThreadView, true);
            } else {
                addMessageToVbox(message, chatThreadView, false);
            }
        }
        chatThreadViews.put(chatThread.getId(), chatThreadView);
    }

    private double calculateMessageHeight(Label label) {
        Text text = new Text(label.getText());
        text.setFont(label.getFont());
        text.setWrappingWidth(label.getMaxWidth() - 20); // Account for padding
        return text.getLayoutBounds().getHeight() + 20; // Add label padding
    }

    private void addMessageToVbox(Message message, VBox messageContainer,boolean addTime) {
        boolean isSentByUser = message.getSender().equals(currentUser.getUsername());
        Label messageLabel = new Label(message.getContent());
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(200);
        messageLabel.setFont(new Font("Arial", 14));
        messageLabel.setPadding(new Insets(10));

        String backgroundGradient = isSentByUser ? "linear-gradient(to top,#0084ff 76%, #4fbcff 100%)" : "linear-gradient(to top,#d6d6d6 72%, #f7f7f7 100%)";
        String textColor = isSentByUser ? "white" : "black";
        String borderRadius = "15 15 15 15";

        messageLabel.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; -fx-background-radius: %s; -fx-margin: 50,0,50,0;",
                backgroundGradient,textColor, borderRadius
        ));

        double width = 250;
        double height = calculateMessageHeight(messageLabel);

        // Create container
        AnchorPane messagePane = new AnchorPane();

        messagePane.setPrefHeight(height+100);
        messagePane.setMinHeight(Region.USE_COMPUTED_SIZE);

        // Position based on sender
        if (isSentByUser) {
            AnchorPane.setRightAnchor(messageLabel, 0.0);
        } else {
            AnchorPane.setLeftAnchor(messageLabel, 0.0);
        }


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mma");
        String formattedDateTime = message.getTimestamp().format(formatter).toLowerCase();

        VBox messageWithTime = new VBox();
        Label timeLabel = new Label(formattedDateTime);
        timeLabel.setFont(new Font("System", 10));
        timeLabel.setStyle("-fx-text-fill: #888888;");
        if(addTime)messageWithTime.getChildren().addAll(timeLabel, messageLabel);
        else messageWithTime.getChildren().add(messageLabel);


        // Position the VBox inside the AnchorPane
        if (isSentByUser) {
            AnchorPane.setRightAnchor(messageWithTime, 0.0);
        } else {
            AnchorPane.setLeftAnchor(messageWithTime, 0.0);
        }
        messagePane.getChildren().add(messageWithTime);


        VBox.setMargin(messagePane, new Insets(2, 5, 2, 5));
        Platform.runLater(() -> {
            messageContainer.getChildren().add(messagePane);
        });
    }





}
