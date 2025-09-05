package com.app.controller;


import com.app.dto.*;
import com.app.service.ChatService;
import com.app.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CXController {
    private final ChatService chatService;
    private final MatchService matchService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/match-request")
    public void requestMatch(@Payload MatchRequest request) {
        MatchResponse response = matchService.requestMatch(request.getUserId(), request.getRole());

        if (response.getStatus() == MatchStatus.WAITING) {
            messagingTemplate.convertAndSendToUser(String.valueOf(request.getUserId()), "/queue/waiting",
                    new WaitingStatus("대기 중입니다"));
        }
    }

    @MessageMapping("/chat/{sessionId}")
    @SendTo("/api/sub/chat/{sessionId}")
    public ChatMessageDTO sendMessage(@DestinationVariable int sessionId, @Payload ChatMessageDTO message) {
        log.info("채팅 메시지 수신 - 세션: {}, 발신자: {}, 내용: {}", sessionId, message.getUserId(), message.getMessage());

        message.setChatId(sessionId);
        message.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        matchService.addMessageToSession(sessionId, message);
        System.out.println("써비쓰" + chatService.saveChat(message));
        return message;
    }

    @MessageMapping("/leave/{sessionId}")
    public void leaveChat(@DestinationVariable int sessionId, @Payload LeaveRequest request) {
        log.info("상담 종료 요청 - 세션: {}, 사용자: {}", sessionId, request.getUserId());

        matchService.endSession(sessionId, request.getUserId());

        ChatMessageDTO systemMessage = new ChatMessageDTO();
        systemMessage.setMessage(request.getUserId() + "님이 상담을 종료했습니다");
        systemMessage.setDate(LocalDateTime.now().toString());
        systemMessage.setUserId(0);

        messagingTemplate.convertAndSend("/api/sub/chat/" + sessionId, systemMessage);
    }
}