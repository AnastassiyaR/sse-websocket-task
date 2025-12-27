package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatService {

    private final List<ChatMessage> messageHistory = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messageHistory.add(message);
        log.info("New chat message from " + message.getSender() + ": " + message.getContent());
    }
}
