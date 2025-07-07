package com.messagingapplication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class MainUIController {

    @FXML private VBox messageContainer;
    @FXML private TextField messageInput;
    @FXML private ScrollPane scrollPane;

    private double calculateMessageHeight(Label label) {
        Text text = new Text(label.getText());
        text.setFont(label.getFont());
        text.setWrappingWidth(label.getMaxWidth() - 20); // Account for padding
        return text.getLayoutBounds().getHeight() + 20; // Add label padding
    }

    public void send(ActionEvent event){
        System.out.println("called");
        String message = messageInput.getText();
        addMessage(message,true);

    }


    private void addMessage(String message, boolean isSentByUser) {
        if(message.charAt(0) == '0')isSentByUser = false;
        // Create message label
        Label messageLabel = new Label(message);
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
        messageContainer.getChildren().add(messagePane);

        // Auto-scroll to bottom
        messageContainer.heightProperty().addListener(obs ->
                scrollPane.setVvalue(1.0));

        messageInput.clear();
    }

}
