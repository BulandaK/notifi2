package com.example.notifi.service;

import com.example.notifi.dto.CreateNotificationRequest;
import com.example.notifi.exception.InvalidScheduledTimeException;
import com.example.notifi.exception.InvalidStatusException;
import com.example.notifi.exception.InvalidZoneException;
import com.example.notifi.exception.NotificationNotFoundException;
import com.example.notifi.model.Notification;
import com.example.notifi.model.NotificationStatus;
import com.example.notifi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final NotificationPublisher notificationPublisher;
    private final QuietHoursPolicy quietHoursPolicy;
    private static final Set<String> ZONES = ZoneId.getAvailableZoneIds();

    public Notification create(CreateNotificationRequest request) {

        if (!ZONES.contains(request.timeZone())) {
            throw new InvalidZoneException("Zone not found. Please input valid zone");
        }

        ZoneId zone = ZoneId.of(request.timeZone());
        Instant requestedTime = request.scheduledAt().atZone(zone).toInstant();

        if (!requestedTime.isAfter(Instant.now(clock))) {
            throw new InvalidScheduledTimeException("Scheduled time must be in the future");
        }

        Instant scheduledTime = quietHoursPolicy.adjust(requestedTime, zone);

        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRecipient(request.recipient());
        notification.setContent(request.content());
        notification.setChannel(request.channel());
        notification.setPriority(request.priority());
        notification.setScheduledAt(scheduledTime);
        if (!scheduledTime.equals(requestedTime)) {
            notification.setOriginalScheduledAt(requestedTime);
        }
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

    @Transactional
    public Notification sendNow(Long id) {
        Notification notification = lock(id);
        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new InvalidStatusException(
                    "Only PENDING notification can be sent now, current status: " + notification.getStatus());
        }
        notification.setScheduledAt(Instant.now(clock));
        notification.setStatus(NotificationStatus.QUEUED);
        notificationPublisher.publish(notification);
        return notification;
    }


    @Transactional
    public Notification cancel(Long id) {
        Notification notification = lock(id);
        NotificationStatus status = notification.getStatus();
        if (status != NotificationStatus.PENDING && status != NotificationStatus.QUEUED) {
            throw new InvalidStatusException("Notification cannot be cancelled, current status: " + status);
        }
        notification.setStatus(NotificationStatus.CANCELLED);
        return notification;
    }

    private Notification lock(Long id) {
        return notificationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
    }
}
