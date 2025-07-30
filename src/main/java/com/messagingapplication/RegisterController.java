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


        User currentUser = (User) ois.readObject();
        ClientDataHandler.getInstance().setCurrentUser(currentUser);
        Map<String, ChatThread> chatThreads = (ConcurrentHashMap<String, ChatThread>) ois.readObject();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainUI.fxml"));
        Parent root = loader.load();

        MainUIController mainUIController = loader.getController();
        mainUIController.oos = oos;
        mainUIController.chatThreads = chatThreads;

        // Updating required data in ClientDataHandler from the server
        ClientDataHandler.getInstance().scrollPane = mainUIController.getScrollPane();
        ClientDataHandler.getInstance().uiController = mainUIController;
        ClientDataHandler.getInstance().loadData(chatThreads);
        mainUIController.loadUIData(); // Initialize UI with chat threads
        mainUIController.clientDataHandler = ClientDataHandler.getInstance();

        Instances.clientDataHandler = ClientDataHandler.getInstance();
        Instances.mainUIController = mainUIController;



        new MessageReciever(ois,oos);

        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setTitle("Messaging Application - " + currentUser.getUsername());
        stage.setScene(new Scene(root));
        stage.setResizable(true);
        stage.show();


    }
}
