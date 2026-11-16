package com.example.notifi.service;


import com.example.notifi.dto.CreateNotificationRequest;
import com.example.notifi.exception.InvalidScheduledTimeException;
import com.example.notifi.exception.InvalidZoneException;
import com.example.notifi.model.Channel;
import com.example.notifi.model.Notification;
import com.example.notifi.model.NotificationStatus;
import com.example.notifi.model.Priority;
import com.example.notifi.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotificationServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-16T12:00:00Z");

    Clock clock;
    NotificationService notificationService;
    NotificationRepository notificationRepository;

    @BeforeEach
    void setup() {
        this.clock = Clock.fixed(NOW, ZoneOffset.UTC);
        this.notificationRepository = Mockito.mock(NotificationRepository.class);
        this.notificationService = new NotificationService(clock, notificationRepository);
    }

    @Test
    void create_DataCorrect_ReturnNotification() {
        //given
        CreateNotificationRequest request = new CreateNotificationRequest(
                "jan",
                "some random content",
                Channel.EMAIL,
                Priority.HIGH,
                "Europe/Warsaw",
                LocalDateTime.of(2026, 11, 17, 10, 0)
        );

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //when
        Notification notification = notificationService.create(request);

        //then
        assertThat(notification.getRecipient()).isEqualTo("jan");
        assertThat(notification.getContent()).isEqualTo("some random content");
        assertThat(notification.getChannel()).isEqualTo(Channel.EMAIL);
        assertThat(notification.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getRecipientZone()).isEqualTo("Europe/Warsaw");
        assertThat(notification.getScheduledAt()).isEqualTo(Instant.parse("2026-11-17T09:00:00Z"));
        assertThat(notification.getCreatedAt()).isEqualTo(NOW);
        assertThat(notification.getAttempts()).isZero();

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void create_InvalidZone_ThrowInvalidZoneException() {
        //given
        CreateNotificationRequest request = new CreateNotificationRequest(
                "jan",
                "some random content",
                Channel.EMAIL,
                Priority.HIGH,
                "Europe/Atlantis",
                LocalDateTime.of(2026, 11, 17, 10, 0)
        );

        //when + then
        assertThatThrownBy(() -> notificationService.create(request))
                .isInstanceOf(InvalidZoneException.class)
                .hasMessage("Zone not found. Please input valid zone");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void create_ScheduledTimeInPast_ThrowInvalidScheduledTimeException() {
        //given
        CreateNotificationRequest request = new CreateNotificationRequest(
                "jan",
                "some random content",
                Channel.EMAIL,
                Priority.HIGH,
                "Europe/Warsaw",
                LocalDateTime.of(2026, 11, 15, 10, 0)
        );

        //when + then
        assertThatThrownBy(() -> notificationService.create(request))
                .isInstanceOf(InvalidScheduledTimeException.class)
                .hasMessage("Scheduled time must be in the future");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

}
