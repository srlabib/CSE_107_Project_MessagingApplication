package com.messagingapplication;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import com.messagingapplication.LoadPage;

import java.io.IOException;

public class RegisterController {
    public void moveToLogin(ActionEvent e) throws IOException {
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(LoadPage.loadFXML("loginPage.fxml"));
        stage.show();
    }
}
