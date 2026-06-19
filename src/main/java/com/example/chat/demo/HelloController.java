package com.example.chat.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality; // 1. ДОДАНО ІМПОРТ ДЛЯ MODALITY
import javafx.stage.Stage;
import java.io.IOException;

public class HelloController {

    @FXML
    public void onHelloButtonClick(ActionEvent event) {
        System.out.println("I am clicked button");
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
            System.out.println("Error!Cannot open info window.");
        }
    }
}