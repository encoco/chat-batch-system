package com.app.service;


import com.app.dto.ChatMessageDTO;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    public ChatMessageDTO saveChat(String inputMessage) {
        System.out.println("ChatService.saveChat " + inputMessage);
        return null;
    }
}
