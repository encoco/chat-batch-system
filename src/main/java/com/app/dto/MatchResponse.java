package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchResponse {
    private int sessionId;
    private MatchStatus status;
    private Integer matchedUserId;

    public MatchResponse(int sessionId, MatchStatus status) {
        this.sessionId = sessionId;
        this.status = status;
    }
}