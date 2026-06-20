package com.example.chat.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;


public class LoginController {
    @FXML private TextField usernameField;
    @FXML private TextField ipField;
    @FXML private TextField portField;

    @FXML public void JoinServer(ActionEvent event)
    {
        String username = usernameField.getText();
        String ip = ipField.getText();
        String portStr = portField.getText();

        if(username.isEmpty())
        {
            usernameField.setText("Anonymous");
            username = "Anonymous";
        }
        if (ip.isEmpty() || portStr.isEmpty()) {
            System.out.println("Error: IP address and port can`t be empty!");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            NetworkManager networkManager = NetworkManager.getInstance();
            boolean isConnected = networkManager.connect(ip, port, username);

            if (isConnected) {
                System.out.println("[CLIENT] Connected successfully to " + ip + ":" + port);
            } else {
                System.out.println("[CLIENT] Connection failed. Check if server is running.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Port must be a valid number (e.g., 8080)!");
            portField.setText("");
        }
    }
}
