package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();  // active users

    public SseEmitter createEmitter() {
        // Timeout - MAX VALUE
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        log.info("New SSE emitter added. Total: {}", emitters.size());

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("Emitter completed. Total: {}", emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.info("Emitter timeout. Total: {}", emitters.size());
        });

        // Spring/Tomcat calls this automatically when the connection is broken
        emitter.onError(error -> {
            emitters.remove(emitter);
            log.error("Emitter error: {}", error.getMessage());
        });

        return emitter;
    }

    public void sendEvent(String eventName, Object data) {
        log.info("Sending '{}' event to {} clients", eventName, emitters.size());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name(eventName)
                                .data(data)
                );
            } catch (IOException e) {
                log.warn("Emitter is dead. Removing it...");
                emitters.remove(emitter);
            }
        }
    }

    public void sendTimeUpdate() {
        String time = LocalTime.now().toString();
        sendEvent("time-update", "Server time: " + time);
    }

    public int getActiveEmittersCount() {
        return emitters.size();
    }
}
