package com.app.controller;


import com.app.dto.ChatMessageDTO;
import com.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CXController {
    private final ChatService chatService;

    @MessageMapping("/send")
    @SendTo("/sub/messages")
    public ChatMessageDTO sendMessage(String inputMessage) {
        log.info("메시지 들어옴 : {}", inputMessage);
        return chatService.saveChat(inputMessage);
    }
}

