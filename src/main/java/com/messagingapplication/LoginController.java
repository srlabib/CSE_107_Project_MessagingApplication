package com.messagingapplication;

import com.CommonClasses.AuthenticationData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class LoginController {
    @FXML
    TextField usernameField;
    @FXML
    TextField passwordField;
    public void login(ActionEvent e) throws IOException, ClassNotFoundException {
        String name = usernameField.getText();
        String password = passwordField.getText();

        if(name.isEmpty() || password.isEmpty()){
            System.out.println("Please fill all fields");
            return;
        }

        Socket socket;
        try {
            socket = new Socket("localhost", 2222);
            AuthenticationData data = new AuthenticationData(name, password);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(data);
            oos.flush();
            String response = (String) ois.readObject();
            if(response.equals("successful")){
                System.out.println("Login successful");
            } else {
                System.out.println("Login failed: " + response);
                return;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }



//        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
//        stage.setScene(LoadPage.loadFXML("MainUI.fxml"));
//        stage.show();
    }


    public void moveToCreateAccount(ActionEvent e) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("CreateAccount.fxml"));
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

    }



}
