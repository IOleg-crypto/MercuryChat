package com.example.chat.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

    public class NetworkManager {
        private static NetworkManager instance;

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private boolean isConnected = false;
        private Thread listenerThread;

        private NetworkManager() {}
        // Maybe i change this?
        public static NetworkManager getInstance() {
            if (instance == null) {
                instance = new NetworkManager();
            }
            return instance;
        }


        public boolean connect(String ip, int port, String username) {
            if (isConnected) {
                System.out.println("Your are connected to this server.");
                return true;
            }

            try {
                socket = new Socket(ip, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                isConnected = true;
                out.println(username);

                startListening();

                return true;
            } catch (IOException e) {
                System.out.println("Connect error :" + e.getMessage());
                disconnect();
                return false;
            }
        }

        public void sendMessage(String message) {
            if (isConnected && out != null) {
                out.println(message);
            } else {
                System.out.println("Can`t send message!No connection.");
            }
        }


        private void startListening() {
            listenerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while (isConnected && (serverMessage = in.readLine()) != null) {
                        System.out.println("Take from server: " + serverMessage);

                        // TODO: Send text to chat area in HelloController,

                    }
                } catch (IOException e) {
                    System.out.println("Lost connection with server: " + e.getMessage());
                } finally {
                    disconnect();
                }
            });

            listenerThread.setDaemon(true);
            listenerThread.start();
        }

        public void disconnect() {
            isConnected = false;
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.out.println("Error with close sockets: " + e.getMessage());
            }
            System.out.println("Connection closed.");
        }
        public boolean isConnected() {
            return isConnected;
        }
    }
