package com.example.chat.demo;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.net.URI;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MainController implements NetworkListener {

    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private Button loginButton;
    @FXML private Button createServerButton;

    private static MainController currentInstance;

    @FXML
    public void initialize() {
        if (chatArea != null) {
            currentInstance = this;
            NetworkManager.getInstance().addListener(this);
        }
    }

    public static MainController getInstance() {
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
            String timeStamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            chatArea.appendText("[" + timeStamp + "] " + message + "\n");
        });
    }

    // --- РЕАЛІЗАЦІЯ ІНТЕРФЕЙСУ NETWORKLISTENER (OBSERVER) ---

    @Override
    public void onMessageReceived(String message) {

        appendMessage(message);
    }

    @Override
    public void onSystemMessage(String message) {
        appendMessage(">>> System: " + message);
        Platform.runLater(() -> {
            boolean connected = NetworkManager.getInstance().isConnected();

            if (loginButton != null) {
                loginButton.setDisable(connected);
            }

            if (createServerButton != null) {
                createServerButton.setDisable(connected);
            }

            if(!connected)
            {
                chatArea.setText(""); // If your disconnect , we clear chat
            }
        });

    }

    @Override
    public void onError(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);

            try {
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                InputStream iconStream = getClass().getResourceAsStream("icon/chaticon.jpg");
                if (iconStream != null) {
                    stage.getIcons().add(new Image(iconStream));
                }
            } catch (Exception e) {
                System.err.println("Error setting alert icon: " + e.getMessage());
            }

            alert.show();
        });
    }
    @FXML
    public void disconnectServer(ActionEvent event) {
        NetworkManager.getInstance().disconnect();
    }

    @FXML
    public void openInfoWindow(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("about-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage aboutStage = new Stage();
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon/chaticon.jpg")));
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
    public void openMyServers(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("servers-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 300, 250);
            Stage stage = new Stage();
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon/chaticon.jpg")));
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
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon/chaticon.jpg")));
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
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon/chaticon.jpg")));
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