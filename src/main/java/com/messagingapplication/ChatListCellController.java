package com.messagingapplication;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ChatListCellController {
    @FXML
    Label nameLabel;
    @FXML
    Label lastMessage;

    public void setName(String name) {
        nameLabel.setText(name);
    }
    public void setLastMessage(String message) {
        lastMessage.setText(message);
    }
}
