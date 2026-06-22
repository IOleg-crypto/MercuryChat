package com.example.chat.demo;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ServersController {
    @FXML private TextArea statusArea;

    @FXML
    public void initialize() {
        NetworkManager manager = NetworkManager.getInstance();
        if (manager != null) {
            String status = manager.getServerStatus();
            statusArea.setText(status);
        } else {
            statusArea.setText("NetworkManager is not running.");
        }
    }
}
