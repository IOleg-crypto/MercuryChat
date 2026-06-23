package com.example.chat.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;

import com.dosse.upnp.UPnP;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class NetworkManager {
    private static NetworkManager instance;

    // Shared
    private boolean isConnected = false;
    private boolean isServer = false;

    // Client specific
    private Socket clientSocket;
    private PrintWriter clientOut;
    private BufferedReader clientIn;
    // To show who send a message
    private String username;

    // Server specific
    private ServerSocket serverSocket;
    private List<ClientHandler> connectedClients = new CopyOnWriteArrayList<>();
    private int activePort = -1;
    private String serverExternalIP = "Unknown";
    private String serverLocalIP = "Unknown";

    private NetworkManager() {}

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public boolean startServer(int port, String username) {
        this.username = username;
        if (isConnected || serverSocket != null) {
            System.out.println("Server already started or connected.");
            return false;
        }

        try {
            // Configure UPnP Port Mapping
            System.out.println("Starting WaifUPnP mapping for port " + port);
            new Thread(() -> {
                boolean mapped = UPnP.openPortTCP(port);
                String externalIP = UPnP.getExternalIP();
                String localIP = UPnP.getLocalIP();

                if (externalIP != null) this.serverExternalIP = externalIP;
                if (localIP != null) this.serverLocalIP = localIP;

                if (mapped) {
                    String msg = "UPnP Port mapping successful! Port " + port + " is open.";
                    if (externalIP != null) {
                        msg += "\n>>> Your Public IP: " + externalIP;
                    }
                    System.out.println(">>> System: " + msg);
                    if (HelloController.getInstance() != null) {
                        final String finalMsg = msg;
                        HelloController.getInstance().appendMessage(">>> System: " + finalMsg);
                    }
                } else {
                    System.out.println("[UPnP] Port mapping failed. Port may not be exposed outwards.");
                    if (HelloController.getInstance() != null) {
                        HelloController.getInstance().appendMessage(">>> System: UPnP Port mapping failed. You might need to forward port " + port + " manually.");
                    }
                }
            }).start();

            serverSocket = new ServerSocket(port);
            isConnected = true;
            isServer = true;
            activePort = port;

            // Listen for multiple client connections
            Thread serverThread = new Thread(() -> {
                while (isConnected && !serverSocket.isClosed()) {
                    try {
                        Socket incomingSocket = serverSocket.accept();
                        System.out.println("Client connected: " + incomingSocket.getInetAddress());
                        ClientHandler handler = new ClientHandler(incomingSocket);
                        connectedClients.add(handler);
                        new Thread(handler).start();

                        if (HelloController.getInstance() != null) {
                            HelloController.getInstance().appendMessage(">>> System: A new user connected!");
                        }
                    } catch (IOException e) {
                        if (isConnected) {
                            System.out.println("Server accept error: " + e.getMessage());
                        }
                    }
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

            return true;
        } catch (Exception e) {
            System.out.println("Failed to start server: " + e.getMessage());

            // Error dialog for the Host if the port is already taken
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Server Error");
                alert.setHeaderText("Failed to host server on port " + port);
                alert.setContentText("This port is already in use by another application. Please try a different port.");
                alert.showAndWait();
            });
            return false;
        }
    }

    public boolean connect(String ip, int port, String username) {
        if (isConnected) {
            System.out.println("You are already connected to a server.");
            return true;
        }
        this.username = username;

        try {
            clientSocket = new Socket(ip, port);
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            isConnected = true;
            isServer = false;
            clientOut.println(username + " has joined the server!"); // Notify server

            startClientListening();

            return true;
        } catch (IOException e) {
            System.out.println("Connect error: " + e.getMessage());
            disconnect();

            // Error dialog for the Client if the server cannot be reached
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Connection Error");
                alert.setHeaderText("Failed to connect to the server");
                alert.setContentText("Please verify that the IP address (" + ip + ") and port (" + port + ") are correct, and make sure your friend has started the server.");
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(getClass().getResourceAsStream("icon/chaticon.jpg")));
                alert.showAndWait();
            });
            return false;
        }
    }

    public void sendMessage(String message) {
        String formattedMessage = username + ": " + message;

        if (isServer) {
            // If it's a server, broadcast to all clients AND show locally
            broadcastMessage(formattedMessage);
            if (HelloController.getInstance() != null) {
                HelloController.getInstance().appendMessage(formattedMessage);
            }
        } else {
            // If it's a client, send to server (server will broadcast it)
            if (isConnected && clientOut != null) {
                clientOut.println(formattedMessage);
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Connection Status");
                    alert.setContentText("Cannot send message. No active connection found! Please create or join a server first.");
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(new Image(getClass().getResourceAsStream("icon/chaticon.jpg")));
                    alert.showAndWait();
                });
                System.out.println("Cannot send message. No connection! Please create or enter server");
            }
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : connectedClients) {
            client.sendMessage(message);
        }
    }

    private void startClientListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                String serverMessage;
                while (isConnected && (serverMessage = clientIn.readLine()) != null) {
                    System.out.println("Received from server/friend: " + serverMessage);

                    if (HelloController.getInstance() != null) {
                        HelloController.getInstance().appendMessage(serverMessage);
                    }
                }
            } catch (IOException e) {
                System.out.println("Lost connection with server: " + e.getMessage());

                // If the connection drops suddenly during the chat session
                if (HelloController.getInstance() != null) {
                    HelloController.getInstance().appendMessage(">>> System: Lost connection with the server.");
                }
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Connection Lost");
                    alert.setHeaderText("Disconnected from server");
                    alert.setContentText("The connection to the host server has been lost.");
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(new Image(getClass().getResourceAsStream("icon/chaticon.jpg")));
                    alert.showAndWait();
                });
            } finally {
                disconnect();
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void disconnect() {
        isConnected = false;
        isServer = false;
        try {
            if (clientIn != null) clientIn.close();
            if (clientOut != null) clientOut.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();

            // Disconnect all clients
            for(ClientHandler client : connectedClients) {
                client.close();
            }
            connectedClients.clear();

            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            if (activePort != -1) {
                final int portToClose = activePort;
                new Thread(() -> UPnP.closePortTCP(portToClose)).start();
                activePort = -1;
            }
        } catch (IOException e) {
            System.out.println("Error while closing sockets: " + e.getMessage());
        }
        System.out.println("Connection closed.");
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getServerStatus() {
        if (!isConnected) {
            return "Not connected/hosting.";
        }
        if (isServer) {
            return "Hosting on Port: " + activePort + "\nLocal IP: " + serverLocalIP + "\nPublic IP: " + serverExternalIP;
        } else {
            return "Connected to server as client.";
        }
    }

    // Inner class for handling multiple clients on the server side
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    // When server receives a message from a client, it broadcasts it to everyone
                    // and shows it in its own chat
                    broadcastMessage(clientMessage);
                    if (HelloController.getInstance() != null) {
                        HelloController.getInstance().appendMessage(clientMessage);
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
                if (HelloController.getInstance() != null) {
                    HelloController.getInstance().appendMessage(">>> System: A user has disconnected.");
                }
            } finally {
                close();
                connectedClients.remove(this);
            }
        }

        public void sendMessage(String msg) {
            if (out != null) {
                out.println(msg);
            }
        }

        public void close() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}