package com.messagingapplication;

import com.SharedClasses.AuthenticationData;
import com.SharedClasses.ChatThread;
import com.SharedClasses.Message;
import com.SharedClasses.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.application.Platform;

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
    public void login(ActionEvent e) throws IOException, ClassNotFoundException {

        String name = usernameField.getText();
        String password = passwordField.getText();
        ObjectOutputStream oos;
        ObjectInputStream ois;

        if(name.isEmpty() || password.isEmpty()){
            System.out.println("Please fill all fields");
            return;
        }

        Socket socket;
        try {
            socket = new Socket("localhost", 2222);
            AuthenticationData data = new AuthenticationData(name, password);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

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

        // first I need to get all the required data from the server
        // and load the clientDataHandler with that data
        // after that I can load the main UI
        // the main UI will load its Vbox with the data from the clientDataHandler

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



        new MessageReciever(ois);

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

/*
    public void loadDummyDataAndShowMainUI(ActionEvent e) throws IOException {
        // Dummy user
        User currentUser = new User("testuser","pass" ,"Test User");

        // Dummy chat threads
        Map<String, ChatThread> chatThreads = new java.util.concurrent.ConcurrentHashMap<>();

        // ChatThread 1
        ChatThread thread1 = new ChatThread("thread1", new String[]{"testuser", "alice"});
//        thread1.pushMessage(new Message("alice", "thread1", "Hello!", java.time.LocalDateTime.now().minusMinutes(10)));
//        thread1.pushMessage(new Message("testuser", "thread1", "Hi Alice!", java.time.LocalDateTime.now().minusMinutes(9)));
        chatThreads.put(thread1.getId(), thread1);

        // ChatThread 2
        ChatThread thread2 = new ChatThread("thread2", new String[]{"testuser", "bob"});
//        thread2.pushMessage(new Message("bob", "thread2", "Hey, are you there?", java.time.LocalDateTime.now().minusMinutes(5)));
        chatThreads.put(thread2.getId(), thread2);

        // Load Main UI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainUI.fxml"));
        Parent root = loader.load();
        MainUIController mainUIController = loader.getController();
        mainUIController.chatThreads = chatThreads;

        // Update ClientDataHandler
        ClientDataHandler.getInstance().setCurrentUser(currentUser);
        ClientDataHandler.getInstance().scrollPane = mainUIController.getScrollPane();
        ClientDataHandler.getInstance().loadData(chatThreads);
        mainUIController.loadUIData(); // Initialize UI with chat threads
        mainUIController.clientDataHandler = ClientDataHandler.getInstance();

        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
*/



}
