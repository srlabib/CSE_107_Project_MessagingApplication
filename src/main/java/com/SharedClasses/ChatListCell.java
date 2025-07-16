// File: src/main/java/com/SharedClasses/ChatListCell.java
package com.SharedClasses;

import com.messagingapplication.ChatListCellController;
import com.messagingapplication.ClientDataHandler;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class ChatListCell extends ListCell<ChatThread> {
    public ChatListCell(){
        this.setOnMouseEntered(e-> applyScale(this,1.05));
        this.setOnMouseExited(e-> applyScale(this,1.0));
    }
    @Override
    protected void updateItem(ChatThread item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/messagingapplication/ChatListCell.fxml"));
                HBox root = loader.load();
                ChatListCellController controller = loader.getController();

                String remoteName = item.getRemoteUserName(ClientDataHandler.getInstance().getCurrentUsername());
                String lastMsg = item.getMessageList().isEmpty() ? "" :
                        item.getMessageList().get(item.getMessageList().size() - 1).getContent();

                controller.setName(remoteName);
                controller.setLastMessage(lastMsg);
                HBox.setMargin(root,new Insets(5,0,5,0)); // Set margin for the root HBox
                setGraphic(root);
                setText(null);
                // Force a layout pass on the root container to refresh the UI
//                root.requestLayout();
            } catch (Exception e) {
                e.printStackTrace();
                setText(item.getRemoteUserName(ClientDataHandler.getInstance().getCurrentUsername()));
                setGraphic(null);
            }
        }
    }
    private void applyScale(ListCell<?> cell, double scale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), cell);
        st.setToX(scale);
        st.setToY(scale);
        st.play();
    }
}