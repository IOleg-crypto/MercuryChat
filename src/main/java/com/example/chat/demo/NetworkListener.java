package com.example.chat.demo;

// For Observer
public interface NetworkListener {
    void onMessageReceived(String message);
    void onSystemMessage(String message);
    void onError(String title, String header, String content);
}
