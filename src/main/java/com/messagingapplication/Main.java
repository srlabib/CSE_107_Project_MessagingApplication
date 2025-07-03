package com.messagingapplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import com.messagingapplication.LoadPage;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Login");
        stage.setScene(LoadPage.loadFXML("loginPage.fxml"));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}