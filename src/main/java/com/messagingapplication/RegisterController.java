package com.messagingapplication;

import com.SharedClasses.AuthenticationData;
import com.SharedClasses.ChatThread;
import com.SharedClasses.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    Label errorMessage2;

    public void moveToLogin(ActionEvent e) throws IOException {
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(LoadPage.loadFXML("loginPage.fxml"));
        stage.show();
    }
    public void CreateAccount(ActionEvent e) throws IOException, ClassNotFoundException {

        String name = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        ObjectOutputStream oos;
        ObjectInputStream ois;

        if(name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            errorMessage2.setText("Please fill all fields");
            return;
        }
        if(!password.equals(confirmPassword)){
            errorMessage2.setText("Passwords do not match");
            return;
        }

        Socket socket;
        try {
            socket = new Socket(Instances.ip, 2222);
            AuthenticationData data = new AuthenticationData(name,"Labib",password);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            Instances.oos = oos;
            Instances.ois = ois;

            oos.writeObject(data);
            oos.flush();
            String response = (String) ois.readObject();
            if(response.equals("successful")){
                System.out.println("New Account created successfully");
                errorMessage2.setText("Account created successfully!");
            } else {
                System.out.println("Account creation failed: " + response);
                return;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }

    }
}
