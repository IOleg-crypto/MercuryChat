# 💬 Mercury Chat

Mercury is a simple client-server chat application built with **Java** and **JavaFX**.

The goal of this project was to create a clean and minimal real-time chat with a custom server and UI.
inspiration was taken from the Walnut project(https://github.com/StudioCherno/Walnut.git)

# Screenshot
---
[![Znimok-ekrana-2026-06-24-184837.png](https://i.postimg.cc/q76GG9qW/Znimok-ekrana-2026-06-24-184837.png)](https://postimg.cc/06xmkXpC)

[![Znimok-ekrana-2026-06-24-185442.png](https://i.postimg.cc/XNryBwGb/Znimok-ekrana-2026-06-24-185442.png)](https://postimg.cc/vDdmpxtq)
---

## 🚀 Features

* **Real-time messaging** with a simple and clean UI (JavaFX)
* **Client-server architecture** (Join an existing server or host your own)
* **Zero-Config Hosting:** Built-in UPnP support automatically forwards ports on your router, allowing friends to connect over the internet instantly without manual network configuration.
* **Basic login system**

---

## 🌐 Seamless Multiplayer with UPnP

Hosting a local server usually requires logging into your router and manually configuring port forwarding. **Mercury Chat solves this.** By utilizing **UPnP (Universal Plug and Play)**, the application automatically negotiates with your home router to open the necessary TCP ports when you click "Create Server". This means your server becomes instantly accessible over the internet to your friends—zero network knowledge required.

## 🖥️ UI
The application consists of:

* Chat area (messages display)
* Input field (send messages)
* Server panel:

  * Log In — connect to server
  * Create Server — start your own server
  * Disconnect - to close and exit from another server
  * My Servers - watch your current server (local and public ip)
  * Info — basic app info

---

## 🏗️ Structure

### Client
* Built with JavaFX.
* Handles UI updates asynchronously and manages user input.
* Sends and receives serialized text messages.

### Server
* Handles concurrent client connections.
* Broadcasts incoming messages to all connected participants.
* Manages UPnP mapping and socket lifecycles.

---

---

## ▶️ How to run

1. Clone the repository

```bash
git clone https://github.com/IOleg-crypto/MercuryChat.git
cd MercuryChat
```
2. Open the project in your IDE (IntelliJ IDEA recommended)
3. Make sure you have:
* JDK 17+
* JavaFX configured
4. Run the application
---
## ⚙️ Tech stack
* Java
* JavaFX
* TCP Sockets
* UPnP API (for Port Forwarding)
---
## 📌 Notes
This is a simple educational project focused on learning Java network programming and UI design. It may not include advanced features like end-to-end encryption, persistent chat history (database), or massive scalability.
---
## 📡 Future ideas
* Chat rooms / channels
* Message history
* Better authentication
---

