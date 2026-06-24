package com.example.chat.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MercuryChat extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MercuryChat.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Image icon = new Image(getClass().getResourceAsStream("icon/chaticon.jpg"));
        stage.getIcons().add(icon);

        stage.setTitle("Mercury");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event -> {
            NetworkManager.getInstance().disconnect();
        });
    }
}