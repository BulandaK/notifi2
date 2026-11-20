package com.example.notifi.repository;

import com.example.notifi.model.Notification;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query(value = """
        SELECT * FROM notifications
        WHERE status = 'PENDING' AND scheduled_at <= :now
        ORDER BY CASE WHEN priority = 'HIGH' THEN 0 ELSE 1 END, scheduled_at
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<Notification> lockDueNotifications(@Param("now") Instant now, @Param("limit") int limit);

    /** SELECT ... FOR UPDATE: blokuje wiersz do końca transakcji (worker, cancel, send-now). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from Notification n where n.id = :id")
    Optional<Notification> findByIdForUpdate(@Param("id") Long id);
}
