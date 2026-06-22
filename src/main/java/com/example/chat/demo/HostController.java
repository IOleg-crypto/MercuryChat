package com.example.chat.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

public class HostController{
    @FXML private TextField hostUsername;
    @FXML private TextField hostPort;

    @FXML
    public void startServer(ActionEvent event) {
    String username = hostUsername.getText().trim();
    String portStr = hostPort.getText().trim();

    if (username.isEmpty()) {
        hostUsername.setText("HostAdmin");
        username = "HostAdmin";
    }
    if (portStr.isEmpty()) {
        System.out.println("Error: Port field is empty!");
        return;
    }

    try {
        int port = Integer.parseInt(portStr);

        NetworkManager networkManager = NetworkManager.getInstance();
        boolean isStarted = networkManager.startServer(port, username);

        if (isStarted) {
            if (HelloController.getInstance() != null) {
                HelloController.getInstance().appendMessage(">>> System: Server successfully hosted on port " + port);
                HelloController.getInstance().appendMessage(">>> System: Waiting for connection...");
            }
            // Close the host window
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } else {
            System.out.println("[SERVER] Launch failed. Port might be bound.");
        }

    } catch (NumberFormatException e) {
        System.out.println("Error: Port must be a valid number!");
        hostPort.setText("");
    }
}
}


