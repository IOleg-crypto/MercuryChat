package com.example.chat.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class HelloController {


    @FXML
    public void onHelloButtonClick(ActionEvent event) {
        System.out.println("I am clicked button");
    }
}
