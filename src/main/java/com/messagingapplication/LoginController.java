package com.messagingapplication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;


public class LoginController {
    @FXML
    TextField usernameField;
    public void login(ActionEvent e) throws IOException {
//        String name = usernameField.getText();
//        System.out.println("Hello " + name);
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(LoadPage.loadFXML("MainUI.fxml"));
        stage.show();
    }


    public void moveToCreateAccount(ActionEvent e) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("CreateAccount.fxml"));
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

    }



}
