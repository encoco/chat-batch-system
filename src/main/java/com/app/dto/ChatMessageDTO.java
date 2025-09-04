package com.app.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    int chatId;
    int userId;
    String message;
    String date;
}
