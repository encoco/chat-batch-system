package com.app.mapper;

import com.app.dto.ChatMessageDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMapper {
    Boolean insertChat(ChatMessageDTO message);
}
