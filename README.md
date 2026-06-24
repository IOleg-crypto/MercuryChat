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

* Real-time messaging
* Simple and clean UI (JavaFX)
* Client-server architecture
* Ability to connect to a server
* Ability to create your own server
* Basic login system

---

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

* Built with JavaFX
* Handles UI and user input
* Sends and receives messages

### Server

* Handles connections
* Broadcasts messages to clients
* Manages basic communication logic

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
* Sockets (TCP)
* Upnp
---
## 📌 Notes
This is a simple educational project.
It may not include advanced features like encryption, chat history, or scalability.
---
## 📡 Future ideas
* Chat rooms / channels
* Message history
* Better authentication
---

