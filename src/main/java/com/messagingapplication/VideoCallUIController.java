package com.messagingapplication;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;

public class VideoCallUIController {
    @FXML
    private Label participantName;
    @FXML
    private Label CallStatus;
    @FXML
    private ImageView localView;
    @FXML
    private ImageView remoteView;

    @FXML
    public void rejectCall() {
        Instances.videoCall.endCall();
    }

    public void setParticipantName(String name) {
        participantName.setText(name);
    }

    public void setWaitingStatus(boolean calling) {
        if(calling)CallStatus.setText("Calling...");
        else CallStatus.setText("");
    }

    public void setFailedStatus(String reason) {
        CallStatus.setText(reason);
    }

    public void displayLocalVideo(WritableImage image) {
        Platform.runLater(() ->localView.setImage(image));
    }

    public void displayRemoteVideo(WritableImage image) {
        System.out.println("setting image to remote view");
        Platform.runLater(() ->remoteView.setImage(image));
        System.out.println("program is still running");
    }


}
