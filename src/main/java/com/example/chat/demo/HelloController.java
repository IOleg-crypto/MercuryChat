package com.example.chat.demo;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.net.URI;
import java.io.IOException;

public class HelloController {


    @FXML private TextArea chatArea;
    @FXML private TextField messageField;


    private static HelloController currentInstance;

    @FXML
    public void initialize() {
        currentInstance = this;
    }

    public static HelloController getInstance() {
        return currentInstance;
    }


    @FXML
    public void onHelloButtonClick(ActionEvent event) {
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            NetworkManager.getInstance().sendMessage(text);
            messageField.clear();
        }
    }


    public void appendMessage(String message) {
        Platform.runLater(() -> {
            chatArea.appendText(message + "\n");
        });
    }

    @FXML
    public void openInfoWindow(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("about-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage aboutStage = new Stage();
            Image icon = new Image(getClass().getResourceAsStream("icon/chaticon.jpg"));
            aboutStage.getIcons().add(icon);
            aboutStage.setTitle("Developer info");
            aboutStage.setScene(scene);
            aboutStage.initModality(Modality.APPLICATION_MODAL);
            aboutStage.setResizable(false);
            aboutStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error! Cannot open info window.");
        }
    }
    @FXML
    public void openJoinWindow(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage joinStage = new Stage();
            Image icon = new Image(getClass().getResourceAsStream("icon/chaticon.jpg"));
            joinStage.getIcons().add(icon);
            joinStage.setTitle("Connect to server");
            joinStage.setScene(scene);
            joinStage.initModality(Modality.APPLICATION_MODAL);
            joinStage.setResizable(false);
            joinStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error! Cannot open join window.");
        }
    }

    @FXML
    public void openHostWindow(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("host-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage hostStage = new Stage();
            Image icon = new Image(getClass().getResourceAsStream("icon/chaticon.jpg"));
            hostStage.getIcons().add(icon);
            hostStage.setTitle("Create Server");
            hostStage.setScene(scene);
            hostStage.initModality(Modality.APPLICATION_MODAL);
            hostStage.setResizable(false);
            hostStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error! Cannot open host window.");
        }
    }



    @FXML
    public void openGithub(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/IOleg-crypto"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Не вдалося відкрити браузер.");
        }
    }
}