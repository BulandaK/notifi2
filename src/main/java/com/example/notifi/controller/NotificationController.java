package com.example.notifi.controller;

import com.example.notifi.dto.CreateNotificationRequest;
import com.example.notifi.dto.NotificationResponse;
import com.example.notifi.mapper.NotificationMapper;
import com.example.notifi.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody CreateNotificationRequest request) {
        return notificationMapper.toResponse(notificationService.create(request));
    }

    @PostMapping("/{id}/send-now")
    public NotificationResponse sendNow(@PathVariable Long id) {
        return notificationMapper.toResponse(notificationService.sendNow(id));
    }

    @PostMapping("/{id}/cancel")
    public NotificationResponse cancel(@PathVariable Long id) {
        return notificationMapper.toResponse(notificationService.cancel(id));
    }

    @GetMapping("/{id}")
    public NotificationResponse get(@PathVariable Long id) {
        return notificationMapper.toResponse(notificationService.get(id));
    }

    @GetMapping
    public List<NotificationResponse> getAll() {
        return notificationMapper.toResponseList(notificationService.getAll());
    }

}
