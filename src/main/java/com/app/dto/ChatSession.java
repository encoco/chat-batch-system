package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSession {
    private int sessionId;
    private int customerId;
    private int agentId;
    private LocalDateTime startTime;
    private List<ChatMessageDTO> messages = new ArrayList<>();

    public ChatSession(int sessionId, int customerId, int agentId, LocalDateTime startTime) {
        this.sessionId = sessionId;
        this.customerId = customerId;
        this.agentId = agentId;
        this.startTime = startTime;
        this.messages = new ArrayList<>();
    }
}