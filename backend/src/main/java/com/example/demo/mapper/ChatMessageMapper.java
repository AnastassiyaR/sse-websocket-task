package com.example.demo.mapper;


import com.example.demo.dto.ChatMessageDTO;
import com.example.demo.model.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMessageMapper {

    ChatMessageDTO toDTO(ChatMessage entity);

    ChatMessage toEntity(ChatMessageDTO dto);
}
