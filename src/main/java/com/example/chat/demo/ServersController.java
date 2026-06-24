package com.example.chat.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Window;

public class ServersController implements NetworkListener {
    @FXML private TextArea statusArea;

    @FXML
    public void initialize() {
        NetworkManager manager = NetworkManager.getInstance();

        statusArea.setText(manager.getServerStatus());
        manager.addListener(this);
        Platform.runLater(() -> {
            if (statusArea.getScene() != null && statusArea.getScene().getWindow() != null) {
                Window window = statusArea.getScene().getWindow();
                window.setOnCloseRequest(event -> {
                    NetworkManager.getInstance().removeListener(this);
                    System.out.println("[DEBUG] ServersController unsubscribe NetworkManager.");
                });
            }
        });
    }

    @Override
    public void onSystemMessage(String message) {
       // Update chatArea
        Platform.runLater(() -> statusArea.setText(NetworkManager.getInstance().getServerStatus()));
    }

    @Override
    public void onMessageReceived(String message) {} // We don`t need this

    @Override
    public void onError(String title, String header, String content) {} // We don`t need this
}