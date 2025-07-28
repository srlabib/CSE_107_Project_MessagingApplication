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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class LoginController {

    @FXML
    TextField usernameField;
    @FXML
    TextField passwordField;
    @FXML
    Label errorMessage;

    public void login(ActionEvent e) throws IOException, ClassNotFoundException {

        String name = usernameField.getText();
        String password = passwordField.getText();
        ObjectOutputStream oos;
        ObjectInputStream ois;

        if(name.isEmpty() || password.isEmpty()){
            errorMessage.setText("Username and password cannot be empty");
            return;
        }

        Socket socket;
        try {
            socket = new Socket(Instances.ip, 2222);
            AuthenticationData data = new AuthenticationData(name, password);
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
                errorMessage.setText(response);
                return;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        User currentUser = (User) ois.readObject();
        Map<String, ChatThread> chatThreads = (ConcurrentHashMap<String, ChatThread>) ois.readObject();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainUI.fxml"));
        Parent root = loader.load();

        MainUIController mainUIController = loader.getController();
        mainUIController.oos = oos;
        mainUIController.chatThreads = chatThreads;

        // Updating required data in ClientDataHandler from the server
        ClientDataHandler.getInstance().setCurrentUser(currentUser);
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
        stage.show();
    }


    public void moveToCreateAccount(ActionEvent e) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("CreateAccount.fxml"));
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

    }


}
