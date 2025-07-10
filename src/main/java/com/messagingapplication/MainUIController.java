package com.messagingapplication;

import com.CommonClasses.ChatThread;
import com.CommonClasses.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.LocalDateTime;

import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MainUIController {

    ClientDataHandler clientDataHandler;
    ObjectOutputStream oos;
    String currentChatThreadId;

    @FXML private TextField messageInput;
    @FXML private ScrollPane scrollPane;



    public void send(ActionEvent event){
        String messageText = messageInput.getText();
        if (messageText.isEmpty()) {
            return; // Do not send empty messages
        }
        Runnable sendMessageTask = () -> {
            // Create a new message object
            Message message = new Message(clientDataHandler.currentUser.getUsername(),currentChatThreadId ,messageText,LocalDateTime.now());
            // Get the current chat thread
            try {
                oos.writeObject(message);
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }

        };
     // Clear the input field after sending the message
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }
}
