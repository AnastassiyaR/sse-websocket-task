# ITI0302-2025 Demo: WebSockets & SSE Chat

## Overview

This project is a **Spring Boot demo** showing real-time communication:

* **SSE (Server-Sent Events)** – backend sends time updates to the frontend every 5 seconds.
* **WebSocket (STOMP)** – simple chat where users send and receive messages instantly.

It is **unauthenticated**, focusing on **demonstrating SSE and WebSocket concepts**.

---

## Project Structure

```
com/example/demo/
├── config/
│   ├── WebSocketConfig.java       # WebSocket + STOMP setup
│   └── SseScheduler.java          # Sends time every 5 seconds
├── controller/
│   ├── ChatController.java        # Handles chat messages
│   └── SseController.java         # SSE endpoint
├── dto/
│   └── ChatMessageDTO.java        # Chat message format
├── mapper/
│   └── ChatMessageMapper.java     # Converts entity ↔ DTO
├── model/
│   └── ChatMessage.java           # Chat message entity
└── service/
    ├── ChatService.java           # Stores messages in memory
    └── SseService.java            # Manages SSE connections
```

---

## Structure

### 1. Configuration (`config`)

#### a) **SseScheduler.java** – scheduled SSE updates

* Sends the current server time to all connected clients every 5 seconds.
* Uses `@Scheduled(fixedRate = 5000)` to trigger the update.
* Logs the number of active clients.

Shows automatic backend-to-frontend push without client requests.

#### b) **WebSocketConfig.java** – WebSocket setup

* Configures STOMP endpoints and message broker.
* `/ws` is the endpoint clients connect to (with SockJS fallback).
* `/app/...` for incoming messages, `/topic/...` for broadcasting.

**Explanation:** Establishes the backbone for real-time chat communication.

---

### 2. Controllers (`controller`)

#### a) **SseController.java** – SSE endpoint

* Provides `/api/sse` endpoint for clients to connect.
* Returns a new `SseEmitter` for each client.

**Explanation:** Each client gets a connection that the backend can push updates to.

#### b) **ChatController.java** – WebSocket chat

* Receives messages at `/app/chat.send`.
* Converts `ChatMessageDTO` to `ChatMessage` entity.
* Stores the message and broadcasts to `/topic/messages`.

**Explanation:** Handles real-time chat messaging and separates API from internal logic.

---

### 3. DTOs (`dto`)

#### ChatMessageDTO.java

* Represents chat messages sent between frontend and backend.
* Fields: `sender`, `content`, `timestamp`.

**Explanation:** Protects internal model and simplifies API communication.

---

### 4. Mapper (`mapper`)

#### ChatMessageMapper.java

* Converts between `ChatMessageDTO` and `ChatMessage`.
* Uses MapStruct for automatic mapping.

**Explanation:** Keeps conversion logic clean and separate from controllers.

---

### 5. Models (`model`)

#### ChatMessage.java

* Internal representation of a chat message.
* Fields: `sender`, `content`, `timestamp`.

**Explanation:** Serves as the main domain object for storing and processing chat messages.

---

### 6. Services (`service`)

#### a) **SseService.java** – manages SSE

* Holds a list of `SseEmitter` instances.
* Sends events to all connected clients.
* Handles timeouts, errors, and completed connections.

**Explanation:** Centralizes SSE logic for easier management and reliability.


#### b) **ChatService.java** – chat storage

* Stores messages in memory using thread-safe list.
* Logs new messages.

**Explanation:** Decouples message storage from controllers, keeping code clean. Uses `CopyOnWriteArrayList` for thread safety.

---

## How It Works

### WebSocket Chat

**Flow:**
```
Client 1 sends "Hello" → Server → Broadcasts to ALL clients
```

**Endpoints:**
- Connect: `ws://localhost:8080/ws`
- Send message: `/app/chat.send`
- Receive messages: `/topic/messages`

---

### SSE Time Updates

**Flow:**
```
Client connects → Server sends time every 5 seconds → Client displays
```

**Endpoint:**
- Stream: `GET http://localhost:8080/api/sse`

---

## What is STOMP?

**STOMP** (Simple Text Oriented Messaging Protocol) adds structure to WebSocket:
- **Topics** for routing (`/topic/messages`)
- **Subscriptions** to channels
- **Standard format** for messages

Without STOMP, you'd handle raw WebSocket frames. STOMP makes it easier to organize messages.

---

## Key Differences: WebSocket vs SSE

| Feature | WebSocket | SSE |
|---------|-----------|-----|
| **Communication** | Bidirectional (client ↔ server) | Unidirectional (server → client) |
| **Protocol** | `ws://` | HTTP |
| **Reconnection** | Manual | Automatic |
| **Use Case** | Chat, games | Live updates, feeds |

---

## When to Use What?

### Use WebSocket for:
- Chat applications (like this demo)
- Multiplayer games
- Collaborative editing
- Any two-way real-time communication

### Use SSE for:
- Live dashboards (like this demo - time updates)
- News feeds
- Notifications
- Progress indicators
- Any one-way server → client updates

---

## Summary

* **SSE**: pushes server time every 5 seconds.
* **WebSocket chat**: real-time messaging between clients.
* **Layered architecture**: Config → Controller → Service → Model/DTO/Mapper.
* **DTOs & Mapper**: separate API from domain model.
* **Thread safety**: Uses `CopyOnWriteArrayList` for concurrent access.