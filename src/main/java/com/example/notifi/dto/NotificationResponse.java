package com.example.notifi.dto;

import com.example.notifi.model.Channel;
import com.example.notifi.model.NotificationStatus;
import com.example.notifi.model.Priority;

import java.time.Instant;
import java.time.ZonedDateTime;

public record NotificationResponse(
        Long id,
        NotificationStatus status,
        Channel channel,
        Priority priority,
        Instant scheduledAtUtc,
        ZonedDateTime scheduledAtLocal
) {
}
