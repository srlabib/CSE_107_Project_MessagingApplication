package com.messagingapplication;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

public class LoadPage {
    public static Scene loadFXML(String file) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(file));
        Scene scene = new Scene(fxmlLoader.load());
        return scene;
    }
}
