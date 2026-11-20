package com.example.notifi.dto;

import com.example.notifi.model.Channel;
import com.example.notifi.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateNotificationRequest(
        @NotBlank String recipient,
        @NotBlank String content,
        @NotNull Channel channel,
        Priority priority,
        @NotBlank String timeZone,
        @NotNull LocalDateTime scheduledAt
) {
    public CreateNotificationRequest {
        if (priority == null) {
            priority = Priority.LOW;
        }
    }
}
