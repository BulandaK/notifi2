package com.example.notifi.dto;

import com.example.notifi.model.Priority;

public record NotificationMessageDto(
        Long id,
        String content,
        Priority priority
) {
}
