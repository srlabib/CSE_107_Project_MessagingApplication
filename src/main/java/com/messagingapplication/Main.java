package com.messagingapplication;

import javafx.application.Application;

import javafx.stage.Stage;
import java.io.IOException;



public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Messenger Application");
        stage.setScene(LoadPage.loadFXML("loginPage.fxml"));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}