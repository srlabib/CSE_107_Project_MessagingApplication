package com.messagingapplication;

import com.CommonClasses.ChatThread;
import com.CommonClasses.Message;
import com.CommonClasses.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientDataHandler {
    public Map<String, ChatThread> charThreads = new ConcurrentHashMap<String, ChatThread>();
    // This VBox to hold all chats to be displayed in the main UI
    private Map<String, VBox> chatThreadViews = new ConcurrentHashMap<String, VBox>();
    public ScrollPane scrollPane;

    private static ClientDataHandler INSTANCE;
    User currentUser;


    public static ClientDataHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClientDataHandler();
        }
        return INSTANCE;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void loadData(Map<String, ChatThread> pastChatThreads) {
        // Loading previous chat threads from the server
        this.charThreads = pastChatThreads;

        for(var entry : charThreads.entrySet()) {
            VBox chatThreadView = new VBox();
            chatThreadView.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            var chatMessages = entry.getValue().getMessageList();

            for(Message message:chatMessages){
                addMessageToVbox(message,chatThreadView);
            }

            // Adding a listener to auto-scroll to the bottom when new messages are added
            // A listener is an object that waits for a specific event to occur, such as a change in a property.
            // this listener will be triggered whenever the height of the chatThreadView changes,
            chatThreadView.heightProperty().addListener(obs ->{
                    Platform.runLater(()->scrollPane.setVvalue(1.0)); // Scroll to the bottom
            });

            chatThreadViews.put(entry.getKey(), chatThreadView);
        }

    }

    public void clearData() {
        // Clear the chat threads data
        charThreads.clear();
        currentUser = null;
    }

    public void addNewMessage(Message message){
        // Generate the chat thread ID based on the current user and the message sender
        String chatThreadId = ChatThread.generateID(currentUser.getUsername(), message.getSender());
        // Search for the chat thread in the existing threads
        ChatThread chatThread = charThreads.get(chatThreadId);

        if (chatThread == null) {
            // If the chat thread does not exist, create a new one
            chatThread = new ChatThread(chatThreadId, new String[]{currentUser.getUsername(),message.getSender()});
            charThreads.put(chatThreadId, chatThread);

            VBox chatThreadView = new VBox();
            chatThreadView.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            chatThreadViews.put(chatThreadId, chatThreadView);

        }
        // Push the message to the chat thread
        chatThread.pushMessage(message);
        addMessageToVbox(message,chatThreadViews.get(chatThreadId));
    }

    private double calculateMessageHeight(Label label) {
        Text text = new Text(label.getText());
        text.setFont(label.getFont());
        text.setWrappingWidth(label.getMaxWidth() - 20); // Account for padding
        return text.getLayoutBounds().getHeight() + 20; // Add label padding
    }

    private void addMessageToVbox(Message message, VBox messageContainer) {
        boolean isSentByUser = message.getSender().equals(currentUser.getName());
        // Create message label
        Label messageLabel = new Label(message.getContent());
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(200);
        messageLabel.setFont(new Font("System", 13));
        messageLabel.setPadding(new Insets(10));

        // Style based on sender
        String backgroundColor = isSentByUser ? "#0084ff" : "#e5e5ea";
        String textColor = isSentByUser ? "white" : "black";
        String borderRadius = isSentByUser ? "15 0 15 15" : "0 15 15 15";

        messageLabel.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; -fx-background-radius: %s;",
                backgroundColor, textColor, borderRadius
        ));

        // Calculate dimensions
        double width = 250;
        double height = calculateMessageHeight(messageLabel);

        // Create container
        AnchorPane messagePane = new AnchorPane();


        messagePane.setPrefHeight(height+10);
        messagePane.setMinHeight(Region.USE_COMPUTED_SIZE);

        // Position based on sender
        if (isSentByUser) {
            AnchorPane.setRightAnchor(messageLabel, 0.0);
        } else {
            AnchorPane.setLeftAnchor(messageLabel, 0.0);
        }

        messagePane.getChildren().add(messageLabel);
        // javaFx's Platform.runLater is used to ensure that the UI updates are done on the JavaFX Application Thread
        Platform.runLater(()-> {
            messageContainer.getChildren().add(messagePane);
        });

    }






}
