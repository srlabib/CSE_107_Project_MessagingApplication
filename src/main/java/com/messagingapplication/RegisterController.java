package com.messagingapplication;

import com.SharedClasses.AuthenticationData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RegisterController {


    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    public void moveToLogin(ActionEvent e) throws IOException {
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(LoadPage.loadFXML("loginPage.fxml"));
        stage.show();
    }
    public void CreateAccount(ActionEvent e) throws IOException {

        String name = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if(name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            System.out.println("Please fill all fields");
            return;
        }
        if(!password.equals(confirmPassword)){
            System.out.println("Passwords do not match");
            return;
        }

        Socket socket;
        try {
            socket = new Socket("localhost", 2222);
            AuthenticationData data = new AuthenticationData(name,"Labib",password);
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


    }
}
