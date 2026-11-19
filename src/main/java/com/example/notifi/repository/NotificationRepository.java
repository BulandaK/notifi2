package com.example.notifi.repository;

import com.example.notifi.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query(value = """
        SELECT * FROM notifications
        WHERE status = 'PENDING' AND scheduled_at <= :now
        ORDER BY CASE WHEN priority = 'HIGH' THEN 0 ELSE 1 END, scheduled_at
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<Notification> lockDueNotifications(@Param("now") Instant now, @Param("limit") int limit);
}
