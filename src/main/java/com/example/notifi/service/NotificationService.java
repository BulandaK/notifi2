package com.example.notifi.service;

import com.example.notifi.dto.CreateNotificationRequest;
import com.example.notifi.dto.NotificationResponse;
import com.example.notifi.exception.InvalidScheduledTimeException;
import com.example.notifi.exception.InvalidZoneException;
import com.example.notifi.exception.NotificationNotFoundException;
import com.example.notifi.model.Notification;
import com.example.notifi.model.NotificationStatus;
import com.example.notifi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final Clock clock;
    private final NotificationRepository notificationRepository;
    private static final Set<String> ZONES = ZoneId.getAvailableZoneIds();

    public Notification create(CreateNotificationRequest request) {

        if (!ZONES.contains(request.timeZone())) {
            throw new InvalidZoneException("Zone not found. Please input valid zone");
        }

        ZoneId zone = ZoneId.of(request.timeZone());
        Instant scheduledTime = request.scheduledAt().atZone(zone).toInstant();

        if (!scheduledTime.isAfter(Instant.now(clock))) {
            throw new InvalidScheduledTimeException("Scheduled time must be in the future");
        }

        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRecipient(request.recipient());
        notification.setContent(request.content());
        notification.setChannel(request.channel());
        notification.setPriority(request.priority());
        notification.setScheduledAt(scheduledTime);
        notification.setRecipientZone(request.timeZone());
        notification.setAttempts(0);
        notification.setCreatedAt(Instant.now(clock));

        return notificationRepository.save(notification);
    }

    public Notification get(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
    }

    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }
}
