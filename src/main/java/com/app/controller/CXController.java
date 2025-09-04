package com.app.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, String> activeUsers = new ConcurrentHashMap<>();
    private final Map<String, String> chatRooms = new ConcurrentHashMap<>();

    // ✅ 풀 API 방식: /api/pub/chat/join → @MessageMapping("/chat/join")
    // ✅ 하이브리드 방식: /pub/chat/join → @MessageMapping("/chat/join")
    // (Spring이 ApplicationDestinationPrefixes를 자동으로 처리)

    @MessageMapping("/chat/join")
    public void join(@RequestBody Map<String, Object> message) {
        String userId = (String) message.get("senderId");
        String userRole = (String) message.get("userRole");

        log.info("사용자 입장: userId={}, role={}", userId, userRole);

        activeUsers.put(userId, userRole);

        Map<String, Object> joinResponse = Map.of(
                "type", "JOIN_SUCCESS",
                "content", userRole + " 모드로 입장했습니다.",
                "timestamp", System.currentTimeMillis(),
                "senderId", "system"
        );

        // ✅ 풀 API 방식 전송 경로
        if (isFullApiMode()) {
            messagingTemplate.convertAndSend("/api/sub/chat/" + userId, joinResponse);
        } else {
            // 하이브리드 방식 전송 경로
            messagingTemplate.convertAndSend("/sub/chat/" + userId, joinResponse);
        }

        if ("customer".equals(userRole)) {
            handleCustomerJoin(userId);
        } else if ("agent".equals(userRole)) {
            handleAgentJoin(userId);
        }
    }

    @MessageMapping("/chat/message")
    public void handleMessage(@RequestBody Map<String, Object> message) {
        String senderId = (String) message.get("senderId");
        String content = (String) message.get("content");
        String chatRoomId = (String) message.get("chatRoomId");

        log.info("채팅 메시지: senderId={}, content={}", senderId, content);

        message.put("timestamp", System.currentTimeMillis());
        message.put("type", "CHAT");

        if (chatRoomId != null) {
            String destination = isFullApiMode() ?
                    "/api/sub/chat/room/" + chatRoomId :
                    "/sub/chat/room/" + chatRoomId;

            messagingTemplate.convertAndSend(destination, message);
        }
    }

    @MessageMapping("/chat/leave")
    public void leave(@RequestBody Map<String, Object> message) {
        String userId = (String) message.get("senderId");
        String chatRoomId = (String) message.get("chatRoomId");

        log.info("사용자 퇴장: userId={}, chatRoomId={}", userId, chatRoomId);

        activeUsers.remove(userId);

        if (chatRoomId != null) {
            Map<String, Object> leaveMessage = Map.of(
                    "type", "USER_LEFT",
                    "content", "상대방이 채팅을 종료했습니다.",
                    "timestamp", System.currentTimeMillis(),
                    "senderId", "system"
            );

            String destination = isFullApiMode() ?
                    "/api/sub/chat/room/" + chatRoomId :
                    "/sub/chat/room/" + chatRoomId;

            messagingTemplate.convertAndSend(destination, leaveMessage);
        }
    }

    private void handleCustomerJoin(String customerId) {
        String agentId = findAvailableAgent();

        if (agentId != null) {
            String chatRoomId = "room_" + System.currentTimeMillis();
            chatRooms.put(chatRoomId, customerId + ":" + agentId);

            Map<String, Object> matchMessage = Map.of(
                    "type", "AGENT_MATCHED",
                    "chatRoomId", chatRoomId,
                    "content", "상담원이 연결되었습니다.",
                    "timestamp", System.currentTimeMillis(),
                    "senderId", "system"
            );

            String customerDest = isFullApiMode() ?
                    "/api/sub/chat/" + customerId :
                    "/sub/chat/" + customerId;

            messagingTemplate.convertAndSend(customerDest, matchMessage);

            Map<String, Object> agentNotification = Map.of(
                    "type", "CUSTOMER_MATCHED",
                    "chatRoomId", chatRoomId,
                    "content", "새로운 고객이 연결되었습니다.",
                    "timestamp", System.currentTimeMillis(),
                    "senderId", "system"
            );

            String agentDest = isFullApiMode() ?
                    "/api/sub/chat/" + agentId :
                    "/sub/chat/" + agentId;

            messagingTemplate.convertAndSend(agentDest, agentNotification);

        } else {
            Map<String, Object> waitMessage = Map.of(
                    "type", "WAITING",
                    "content", "상담원 연결을 기다리고 있습니다. 잠시만 기다려주세요.",
                    "timestamp", System.currentTimeMillis(),
                    "senderId", "system"
            );

            String destination = isFullApiMode() ?
                    "/api/sub/chat/" + customerId :
                    "/sub/chat/" + customerId;

            messagingTemplate.convertAndSend(destination, waitMessage);
        }
    }

    private void handleAgentJoin(String agentId) {
        Map<String, Object> readyMessage = Map.of(
                "type", "AGENT_READY",
                "content", "고객 문의를 기다리고 있습니다.",
                "timestamp", System.currentTimeMillis(),
                "senderId", "system"
        );

        String destination = isFullApiMode() ?
                "/api/sub/chat/" + agentId :
                "/sub/chat/" + agentId;

        messagingTemplate.convertAndSend(destination, readyMessage);
    }

    private String findAvailableAgent() {
        return activeUsers.entrySet().stream()
                .filter(entry -> "agent".equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    // 설정에 따라 API 모드 결정 (실제로는 @Value로 properties에서 읽어오면 됨)
    private boolean isFullApiMode() {
        // 풀 API 방식을 사용하려면 true 반환
        return true; // 또는 false (하이브리드 방식)
    }
}