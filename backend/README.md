# ITI0302-2025 Demo: WebSockets & SSE Chat

## Overview

This project is a **Spring Boot demo** showing real-time communication:

* **SSE (Server-Sent Events)** – backend sends time updates to the frontend every 5 seconds.
* **WebSocket (STOMP)** – simple chat where users send and receive messages instantly.

It is **unauthenticated**, focusing on **demonstrating SSE and WebSocket concepts**.

---

## Structure

### 1. Configuration (`config`)

#### a) **SseScheduler.java** – scheduled SSE updates

* Sends the current server time to all connected clients every 5 seconds.
* Uses `@Scheduled(fixedRate = 5000)` to trigger the update.
* Logs the number of active clients.

```java
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class SseScheduler {

    private final SseService sseService;

    @Scheduled(fixedRate = 5000)
    public void sendPeriodicUpdates() {
        log.info("Sending periodic time update to {} clients",
                sseService.getActiveEmittersCount());
        sseService.sendTimeUpdate();
    }
}
```

**Why it’s important:** Shows automatic backend-to-frontend push without client requests.

#### b) **WebSocketConfig.java** – WebSocket setup

* Configures STOMP endpoints and message broker.
* `/ws` is the endpoint clients connect to (with SockJS fallback).
* `/app/...` for incoming messages, `/topic/...` for broadcasting.

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

**Why it’s important:** Establishes the backbone for real-time chat communication.

### 2. Controllers (`controller`)

#### a) **SseController.java** – SSE endpoint

* Provides `/api/sse` endpoint for clients to connect.
* Returns a new `SseEmitter` for each client.

```java
@RestController
@AllArgsConstructor
public class SseController {

    private final SseService sseService;

    @GetMapping(path = "/api/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return sseService.createEmitter();
    }
}
```

**Explanation:** Each client gets a connection that the backend can push updates to.

#### b) **ChatController.java** – WebSocket chat

* Receives messages at `/app/chat.send`.
* Converts `ChatMessageDTO` to `ChatMessage` entity.
* Stores the message and broadcasts to `/topic/messages`.

```java
@Controller
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatMessageMapper mapper;

    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public ChatMessageDTO sendMessage(ChatMessageDTO chatMessageDTO) {
        chatMessageDTO.setTimestamp(LocalDateTime.now());

        ChatMessage message = mapper.toEntity(chatMessageDTO);
        chatService.addMessage(message);

        return mapper.toDTO(message);
    }
}
```

**Explanation:** Handles real-time chat messaging and separates API from internal logic.

### 3. DTOs (`dto`)

#### ChatMessageDTO.java

* Represents chat messages sent between frontend and backend.
* Fields: `sender`, `content`, `timestamp`.

```java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private String sender;
    private String content;
    private LocalDateTime timestamp;
}
```

**Why:** Protects internal model and simplifies API communication.

### 4. Mapper (`mapper`)

#### ChatMessageMapper.java

* Converts between `ChatMessageDTO` and `ChatMessage`.
* Uses MapStruct for automatic mapping.

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMessageMapper {
    ChatMessageDTO toDTO(ChatMessage entity);
    ChatMessage toEntity(ChatMessageDTO dto);
}
```

**Explanation:** Keeps conversion logic clean and separate from controllers.

### 5. Models (`model`)

#### ChatMessage.java

* Internal representation of a chat message.
* Fields: `sender`, `content`, `timestamp`.

```java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String sender;
    private String content;
    private LocalDateTime timestamp;
}
```

**Why:** Serves as the main domain object for storing and processing chat messages.

### 6. Services (`service`)

#### a) **SseService.java** – manages SSE

* Holds a list of `SseEmitter` instances.
* Sends events to all connected clients.
* Handles timeouts, errors, and completed connections.

```java
@Slf4j
@Service
public class SseService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));

        return emitter;
    }

    public void sendTimeUpdate() {
        String time = LocalTime.now().toString();
        sendEvent("time-update", time);
    }

    private void sendEvent(String eventName, Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }

    public int getActiveEmittersCount() {
        return emitters.size();
    }
}
```

**Explanation:** Centralizes SSE logic for easier management and reliability.

#### b) **ChatService.java** – chat storage

* Stores messages in memory.
* Logs new messages.

```java
@Slf4j
@Service
public class ChatService {

    private final List<ChatMessage> messageHistory = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messageHistory.add(message);
        log.info("New chat message from {}: {}", message.getSender(), message.getContent());
    }
}
```

**Explanation:** Decouples message storage from controllers, keeping code clean.

---

## Summary

* **SSE**: pushes server time every 5 seconds.
* **WebSocket chat**: real-time messaging between clients.
* **Layered architecture**: Config → Controller → Service → Model/DTO/Mapper.
* **DTOs & Mapper**: separate API from domain model.
* **Logging**: provides visibility into message flow and active clients.
