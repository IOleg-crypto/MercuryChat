package com.example.chat.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.dosse.upnp.UPnP;
import org.jetbrains.annotations.NotNull;

public class NetworkManager {
    private static NetworkManager instance;

    private volatile boolean isConnected = false;
    private boolean isServer = false;

    private Socket clientSocket;
    private PrintWriter clientOut;
    private BufferedReader clientIn;
    private String username;

    // Серверні змінні
    private ServerSocket serverSocket;
    private final List<ClientHandler> connectedClients = new CopyOnWriteArrayList<>();
    private int activePort = -1;
    private String serverExternalIP = "Unknown";
    private String serverLocalIP = "Unknown";

    public String getUsername()
    {
        return username;
    }

    // СПИСОК СЛУХАЧІВ ІНТЕРФЕЙСУ (Паттерн Observer)
    private final List<NetworkListener> listeners = new CopyOnWriteArrayList<>();

    private NetworkManager() {}

    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void addListener(NetworkListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }

    private void notifyMessage(String message) {
        for (NetworkListener listener : listeners) {
            listener.onMessageReceived(message);
        }
    }

    private void notifySystem(String message) {
        for (NetworkListener listener : listeners) {
            listener.onSystemMessage(message);
        }
    }

    private void notifyError(String title, String header, String content) {
        for (NetworkListener listener : listeners) {
            listener.onError(title, header, content);
        }
    }

    public boolean startServer(int port, String username) {
        this.username = username;
        if (isConnected || serverSocket != null) {
            System.out.println("Server already started or connected.");
            return false;
        }

        try {
            notifySystem("Starting UPnP mapping for port " + port);

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
                    notifySystem(msg);
                } else {
                    notifySystem("UPnP Port mapping failed. You might need to forward port " + port + " manually.");
                }
            }).start();

            serverSocket = new ServerSocket(port);
            isConnected = true;
            isServer = true;
            activePort = port;

            Thread serverThread = getThread();
            serverThread.start();

            return true;
        } catch (Exception e) {
            System.out.println("Failed to start server: " + e.getMessage());
            notifyError("Server Error",
                    "Failed to host server on port " + port,
                    "This port is already in use by another application. Please try a different port.");
            return false;
        }
    }

    @NotNull
    private Thread getThread() {
        Thread serverThread = new Thread(() -> {
            while (isConnected && !serverSocket.isClosed()) {
                try {
                    Socket incomingSocket = serverSocket.accept();
                    System.out.println("Client connected: " + incomingSocket.getInetAddress());

                    ClientHandler handler = new ClientHandler(incomingSocket, this);
                    connectedClients.add(handler);

                    Thread t = new Thread(handler);
                    t.setDaemon(true);
                    t.start();

                    // Оповіщаємо ТІЛЬКИ локальну консоль сервера про підключення сокету
                    System.out.println("A new user connected to socket.");
                } catch (IOException e) {
                    if (isConnected) {
                        System.out.println("Server accept error: " + e.getMessage());
                    }
                }
            }
        });
        serverThread.setDaemon(true);
        return serverThread;
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

            clientOut.println(username + " has joined the server!");
            startClientListening();

            return true;
        } catch (IOException e) {
            System.out.println("Connect error: " + e.getMessage());
            disconnect();

            notifyError("Connection Error",
                    "Failed to connect to the server",
                    "Please verify that the IP address (" + ip + ") and port (" + port + ") are correct.");
            return false;
        }
    }

    public void sendMessage(String message) {
        String formattedMessage = username + ": " + message;

        if (isServer) {
            // Сервер сам написав: розсилаємо всім і показуємо у себе
            broadcastMessage(formattedMessage);
            notifyMessage(formattedMessage);
        } else {
            // Клієнт сам написав: відправляємо на сервер
            if (isConnected && clientOut != null) {
                clientOut.println(formattedMessage);
            } else {
                notifyError("Error",
                        "Connection Status",
                        "Cannot send message. No active connection found! Please create or join a server first.");
            }
        }
    }

    /**
     * ФІКС: НОВИЙ МЕТОД ДЛЯ ОБРОБКИ ПОВІДОМЛЕНЬ ВІД ІНШИХ КЛІЄНТІВ
     * Сервер просто транслює готове підписане повідомлення без спотворення нікнеймів.
     */
    public void handleIncomingClientMessage(String message) {
        if (isServer) {
            // 1. Пересилаємо повідомлення (воно вже містить префікс "Нік: текст") усім клієнтам
            broadcastMessage(message);
            // 2. Виводимо це повідомлення на екран самого Сервера
            notifyMessage(message);
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
                    notifyMessage(serverMessage);
                }
            } catch (IOException e) {
                if (isConnected) {
                    notifySystem("Lost connection with the server.");
                    notifyError("Connection Lost", "Disconnected from server", "The connection to the host server has been lost.");
                } else {
                    System.out.println("Client disconnected intentionally.");
                }
            } finally {
                disconnect();
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public synchronized void disconnect() {
        if (!isConnected && !isServer && activePort == -1) return;

        isConnected = false;
        isServer = false;

        if (activePort != -1) {
            final int portToClose = activePort;
            activePort = -1;
            Thread upnpThread = new Thread(() -> {
                try {
                    System.out.println("[UPnP] Attempting to close port " + portToClose);
                    UPnP.closePortTCP(portToClose);
                    System.out.println("[UPnP] Port closed successfully.");
                } catch (Throwable t) {
                    System.out.println("[UPnP] Error closing port: " + t.getMessage());
                }
            });
            upnpThread.setDaemon(true);
            upnpThread.start();
        }

        try {
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            if (clientIn != null) clientIn.close();
            if (clientOut != null) clientOut.close();

            for (ClientHandler client : connectedClients) {
                client.close();
            }
            connectedClients.clear();

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error while closing sockets: " + e.getMessage());
        }

        notifySystem("Connection closed.");
    }

    public void removeClient(ClientHandler handler) {
        connectedClients.remove(handler);
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
}