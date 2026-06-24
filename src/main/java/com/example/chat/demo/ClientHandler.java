package com.example.chat.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final NetworkManager serverManager;

    public ClientHandler(Socket socket, NetworkManager serverManager) {
        this.socket = socket;
        this.serverManager = serverManager;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                serverManager.handleIncomingClientMessage(clientMessage);
            }
        } catch (IOException e) {
            serverManager.handleIncomingClientMessage(">>>Server : " + serverManager.getUsername() + " has disconnected.");
        } finally {
            close();
            serverManager.removeClient(this);
        }
    }

    public void sendMessage(String msg) {
        if (out != null)
        {
            out.println(msg);
        }
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}