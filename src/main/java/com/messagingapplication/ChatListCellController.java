// File: src/main/java/com/messagingapplication/ChatListCellController.java
package com.messagingapplication;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ChatListCellController {
    @FXML
    Label nameLabel;
    @FXML
    Label lastMessage;
    @FXML
    ImageView profileImage;

    public void setName(String name) {
        nameLabel.setText(name);
    }

    public void setLastMessage(String message) {
        lastMessage.setText(message);
    }

    public void setProfileImage(Image image) {
        profileImage.setImage(image);
    }
}