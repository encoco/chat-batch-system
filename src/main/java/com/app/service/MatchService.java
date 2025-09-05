package com.app.service;

import com.app.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    private final SimpMessagingTemplate messagingTemplate;

    private final Queue<Integer> waitingCustomers = new ConcurrentLinkedQueue<>();
    private final Queue<Integer> availableAgents = new ConcurrentLinkedQueue<>();
    private final Map<Integer, ChatSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> userToSession = new ConcurrentHashMap<>();

    public MatchResponse requestMatch(int userId, UserRole role) {
        log.info("=== 매칭 요청 시작 ===");
        log.info("요청자 ID: {}, 역할: {}", userId, role);
        printQueueStatus();

        MatchResponse response;
        if (role == UserRole.CUSTOMER) {
            response = matchCustomer(userId);
        } else {
            response = matchAgent(userId);
        }

        log.info("매칭 결과: {}", response.getStatus());
        printQueueStatus();
        log.info("=== 매칭 요청 완료 ===");

        return response;
    }

    private MatchResponse matchCustomer(int customerId) {
        Integer availableAgent = availableAgents.poll();

        if (availableAgent != null) {
            int sessionId = createSession(customerId, availableAgent);
            notifyMatchSuccess(customerId, availableAgent, sessionId);
            return new MatchResponse(sessionId, MatchStatus.MATCHED);
        } else {
            waitingCustomers.offer(customerId);
            return new MatchResponse(0, MatchStatus.WAITING);
        }
    }

    private MatchResponse matchAgent(int agentId) {
        Integer waitingCustomer = waitingCustomers.poll();

        if (waitingCustomer != null) {
            int sessionId = createSession(waitingCustomer, agentId);
            notifyMatchSuccess(waitingCustomer, agentId, sessionId);
            return new MatchResponse(sessionId, MatchStatus.MATCHED, waitingCustomer);
        } else {
            availableAgents.offer(agentId);
            return new MatchResponse(0, MatchStatus.WAITING);
        }
    }

    private int createSession(int customerId, int agentId) {
        int sessionId = (int)(System.currentTimeMillis() % 1000000);
        ChatSession session = new ChatSession(sessionId, customerId, agentId, LocalDateTime.now());

        activeSessions.put(sessionId, session);
        userToSession.put(customerId, sessionId);
        userToSession.put(agentId, sessionId);

        log.info("새 세션 생성: {} (고객: {}, 상담원: {})", sessionId, customerId, agentId);
        return sessionId;
    }

    private void notifyMatchSuccess(int customerId, int agentId, int sessionId) {
        log.info("매칭 성공 알림 전송 시작 - 세션: {}, 고객: {}, 상담원: {}", sessionId, customerId, agentId);

        try {
            MatchNotification customerNotification = new MatchNotification(sessionId, "CUSTOMER", agentId);
            MatchNotification agentNotification = new MatchNotification(sessionId, "AGENT", customerId);

            messagingTemplate.convertAndSendToUser(String.valueOf(customerId), "/queue/match-result", customerNotification);
            messagingTemplate.convertAndSendToUser(String.valueOf(agentId), "/queue/match-result", agentNotification);

            log.info("알림 전송 완료");
        } catch (Exception e) {
            log.error("알림 전송 실패", e);
        }
    }

    public void addMessageToSession(int sessionId, ChatMessageDTO message) {
        ChatSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.getMessages().add(message);
            log.info("메시지 메모리 저장 - 세션: {}, 총 메시지: {}", sessionId, session.getMessages().size());
        }
    }

    public void endSession(int sessionId, int userId) {
        ChatSession session = activeSessions.remove(sessionId);
        if (session != null) {
            userToSession.remove(session.getCustomerId());
            userToSession.remove(session.getAgentId());

            if (session.getAgentId() == userId) {
                autoRematchAgent(userId);
            }
        }
    }

    private void autoRematchAgent(int agentId) {
        Integer nextCustomer = waitingCustomers.poll();
        if (nextCustomer != null) {
            int newSessionId = createSession(nextCustomer, agentId);
            notifyMatchSuccess(nextCustomer, agentId, newSessionId);
        } else {
            availableAgents.offer(agentId);
        }
    }

    public void printQueueStatus() {
        log.info("현재 큐 상태:");
        log.info("  - 대기 고객: {} 명 {}", waitingCustomers.size(), waitingCustomers.toString());
        log.info("  - 대기 상담원: {} 명 {}", availableAgents.size(), availableAgents.toString());
    }
}