package com.SharedClasses;

import com.messagingapplication.ChatListCellController;
import com.messagingapplication.ClientDataHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

public class ChatListCell extends ListCell<ChatThread> {
    private HBox root;
    private ChatListCellController controller;

    public ChatListCell(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/messagingapplication/ChatListCell.fxml"));
            System.out.println(loader.getLocation());
            root = loader.load();
            controller = loader.getController();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateItem(ChatThread item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            controller.setName(item.getRemoteUserName(ClientDataHandler.getInstance().getCurrentUsername())); // Assuming the first participant is the name to display
            controller.setLastMessage(item.getMessageList().isEmpty() ? "" : item.getMessageList().get(item.getMessageList().size() - 1).getContent());
            setGraphic(root);
        }
    }
}
