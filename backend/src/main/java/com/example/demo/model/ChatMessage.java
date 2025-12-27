package com.example.demo.model;

import lombok.*;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatMessage {
    private String sender;
    private String content;
    private LocalDateTime timestamp;
}
