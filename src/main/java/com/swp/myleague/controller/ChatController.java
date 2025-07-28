package com.swp.myleague.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.swp.myleague.model.entities.ChatMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ChatController {

    @MessageMapping("/chat.sendToAdmin")
    @SendTo("/topic/admin")
    public ChatMessage sendToAdmin(ChatMessage message) {
        message.setTimestamp(now());
        return message;
    }

    @MessageMapping("/chat.sendToUser")
    public void sendToUser(ChatMessage message,
            org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        message.setTimestamp(now());
        // Server gửi đến user cụ thể
        messagingTemplate.convertAndSend("/topic/user/" + message.getUserId(), message);
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
