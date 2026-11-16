package com.example.notifi.mapper;

import com.example.notifi.dto.NotificationResponse;
import com.example.notifi.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {

    @Mapping(target = "scheduledAtUtc", source = "scheduledAt")
    @Mapping(target = "scheduledAtLocal", source = "notification", qualifiedByName = "toLocalTime")
    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);

    @Named("toLocalTime")
    default ZonedDateTime toLocalTime(Notification notification) {
        if (notification.getScheduledAt() == null || notification.getRecipientZone() == null) {
            return null;
        }
        return notification.getScheduledAt().atZone(ZoneId.of(notification.getRecipientZone()));
    }
}
