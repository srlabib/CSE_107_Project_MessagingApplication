package com.messagingapplication;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class IncommingCallUIController {
    @FXML
    private Label participantName;

    public void setCallerName(String name) {
        participantName.setText(name);
    }
    @FXML
    public void acceptCall() {
        // Logic to accept the call
        System.out.println("Call accepted");
        Instances.videoCall.acceptCall();
    }
    @FXML
    public void rejectCall() {
        // Logic to reject the call
        System.out.println("Call rejected");
        Instances.videoCall.rejectCall();
    }
}
