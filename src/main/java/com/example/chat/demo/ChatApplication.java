package com.example.chat.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader;
        fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon/chaticon.jpg")));
        stage.getIcons().add(icon);

        stage.setTitle("Mercury");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            // Примусово закриваємо всі сокети перед виходом
            NetworkManager.getInstance().disconnect();
            Platform.exit();
        });
    }
}