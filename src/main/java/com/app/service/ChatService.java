package com.app.service;


import com.app.dto.ChatMessageDTO;
import com.app.mapper.ChatMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ChatService {
    private final ChatMapper chatMapper;
    public Boolean saveChat(ChatMessageDTO inputMessage) {
        System.out.println("ChatService.saveChat " + inputMessage);
        return chatMapper.insertChat(inputMessage);
    }
}
