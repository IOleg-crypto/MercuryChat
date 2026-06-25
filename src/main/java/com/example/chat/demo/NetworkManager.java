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

/**
 * Singleton network manager handling socket connections, UPnP port mapping,
 * and decoupling UI logic from the network layer using the Observer pattern.
 */
public class NetworkManager {
    private static NetworkManager instance;

    // Connection state flags. Volatile ensures atomic visibility across threads.
    private volatile boolean isConnected = false;
    private boolean isServer = false;

    // Client-side socket components
    private Socket clientSocket;
    private PrintWriter clientOut;
    private BufferedReader clientIn;
    private String username;

    // Server-side socket components
    private ServerSocket serverSocket;
    // Thread-safe list to store and manage active remote client handlers
    private final List<ClientHandler> connectedClients = new CopyOnWriteArrayList<>();
    private int activePort = -1;
    private String serverExternalIP = "Unknown";
    private String serverLocalIP = "Unknown";

    public String getUsername() {
        return username;
    }

    // List of decoupled event listeners (UI or controllers subscribing to network events)
    private final List<NetworkListener> listeners = new CopyOnWriteArrayList<>();

    private NetworkManager() {}

    /**
     * Thread-safe Singleton instance retriever.
     */
    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    // --- Observer Pattern Subscription Management ---

    public void addListener(NetworkListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }

    // --- Internal Event Broadcasters to Observers ---

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

    /**
     * Instantiates a ServerSocket and spawns a background thread to accept connection requests.
     * Also triggers asynchronous UPnP port forwarding via WaifUPnP.
     */
    public boolean startServer(int port, String username) {
        this.username = username;
        if (isConnected || serverSocket != null) {
            System.out.println("Server already started or connected.");
            return false;
        }

        try {
            notifySystem("Starting UPnP mapping for port " + port);

            // Asynchronously request router port forwarding to keep UI responsive
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

            // Instantiates and runs the main listening loop thread
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

    /**
     * Assembles the main background thread responsible for block-waiting on incoming connections.
     */
    @NotNull
    private Thread getThread() {
        Thread serverThread = new Thread(() -> {
            while (isConnected && !serverSocket.isClosed()) {
                try {
                    // Blocking call that awaits an incoming remote connection
                    Socket incomingSocket = serverSocket.accept();
                    System.out.println("Client connected: " + incomingSocket.getInetAddress());

                    // Hand off socket processing to a dedicated ClientHandler instance
                    ClientHandler handler = new ClientHandler(incomingSocket, this);
                    connectedClients.add(handler);

                    // Daemon thread setup ensures the handler thread stops automatically when app quits
                    Thread t = new Thread(handler);
                    t.setDaemon(true);
                    t.start();

                    // Notify local console about the low-level connection
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

    /**
     * Connects to a remote server host as a client participant.
     */
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

            // Broadcast initial greeting to notify the host of arrival
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

    /**
     * Packages and sends outbound chat messages depending on host state (Server vs Client).
     */
    public void sendMessage(String message) {
        String formattedMessage = username + ": " + message;

        if (isServer) {
            // Server broadcasts directly to all clients and appends to own UI local view
            broadcastMessage(formattedMessage);
            notifyMessage(formattedMessage);
        } else {
            // Client forwards payload to the host server for central distribution
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
     * Invoked by ClientHandlers when they intercept client payloads. Handles global routing.
     */
    public void handleIncomingClientMessage(String message) {
        if (isServer) {
            broadcastMessage(message);
            notifyMessage(message);
        }
    }

    /**
     * Distributes a pre-formatted message payload across all tracked ClientHandler sockets.
     */
    public void broadcastMessage(String message) {
        for (ClientHandler client : connectedClients) {
            client.sendMessage(message);
        }
    }

    /**
     * Spawns a background listener loop to read downstream messages from the server socket stream.
     */
    private void startClientListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                String serverMessage;
                // Read operations block natively until a newline character is intercepted
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

    /**
     * Synchronized disconnection logic. Safely tears down open sockets,
     * stream resources, and unmaps dynamic ports via UPnP asynchronously.
     */
    public synchronized void disconnect() {
        if (!isConnected && !isServer && activePort == -1) return;

        isConnected = false;
        isServer = false;

        // Clean up UPnP mappings in a separate background routine to avoid UI locks
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
            // Closing sockets breaks active blocking read/accept executions immediately
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