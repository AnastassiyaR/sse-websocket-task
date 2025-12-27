package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDTO;
import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

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
