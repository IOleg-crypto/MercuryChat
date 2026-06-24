package com.example.chat.demo;

public interface NetworkListener {
    void onMessageReceived(String message);
    void onSystemMessage(String message);
    void onError(String title, String header, String content);
}
