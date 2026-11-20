package com.example.notifi.service;

import com.example.notifi.model.Notification;
import com.example.notifi.model.NotificationStatus;
import com.example.notifi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@Profile("api")
@RequiredArgsConstructor
public class NotificationDispatcher {

    private static final int BATCH_SIZE = 50;

    private final NotificationRepository repository;
    private final NotificationPublisher publisher;
    private final Clock clock;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void dispatch() {
        List<Notification> due = repository.lockDueNotifications(Instant.now(clock), BATCH_SIZE);
        for (Notification notification : due) {
            notification.setStatus(NotificationStatus.QUEUED);
            publisher.publish(notification);
            log.info("Zakolejkowano powiadomienie {} ({}, {})",
                    notification.getId(), notification.getChannel(), notification.getPriority());
        }
    }
}
