package com.example.notifi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.ZonedDateTime;

@Entity
@Table(name = "notifications")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String recipient;
    @Enumerated(EnumType.STRING)
    private Channel channel;
    @Enumerated(EnumType.STRING)
    private Priority priority;
    @Enumerated
    private NotificationStatus status;
    private String content;

    private String recipientZone;
    private Instant scheduledAt;
    private Instant createdAt;
    private Instant sentAt;
    private int attempts;

}
