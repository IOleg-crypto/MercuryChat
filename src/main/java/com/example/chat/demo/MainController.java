package com.example.chat.demo;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    @FXML private Button loginButton;
    @FXML private Button createServerButton;


    private static HelloController currentInstance;

    @FXML
    public void initialize() {
        if (chatArea != null) {
            currentInstance = this;
        }
    }

    public static HelloController getInstance() {
        return currentInstance;
    }
    public Button getLoginButton()
    {
        return loginButton;
    }
    public Button getCreateServerButton()
    {
        return createServerButton;
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
            String timeStamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            chatArea.appendText("[" + timeStamp + "] " + message + "\n");
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
    public void disconnectServer(ActionEvent event) {
        NetworkManager.getInstance().disconnect();
        appendMessage(">>> System: Disconnected.");
    }

    @FXML
    public void openMyServers(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("servers-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 300, 250);
            Stage stage = new Stage();
            Image icon = new Image(getClass().getResourceAsStream("icon/chaticon.jpg"));
            stage.getIcons().add(icon);
            stage.setTitle("My Servers");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Load file is failed servers-view.fxml " + e);
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
            System.out.println("Can`t open browser.");
        }
    }
}