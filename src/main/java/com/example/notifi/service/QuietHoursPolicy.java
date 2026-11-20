package com.example.notifi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class QuietHoursPolicy {

    private final LocalTime start;
    private final LocalTime end;

    @Autowired
    public QuietHoursPolicy(@Value("${notifi.quiet-hours.start:22:00}") String start,
                            @Value("${notifi.quiet-hours.end:08:00}") String end) {
        this(LocalTime.parse(start), LocalTime.parse(end));
    }

    public QuietHoursPolicy(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public boolean isQuiet(Instant instant, ZoneId zone) {
        LocalTime time = instant.atZone(zone).toLocalTime();
        if (start.isAfter(end)) {
            return !time.isBefore(start) || time.isBefore(end);
        }
        return !time.isBefore(start) && time.isBefore(end);
    }

    public Instant adjust(Instant instant, ZoneId zone) {
        if (!isQuiet(instant, zone)) {
            return instant;
        }
        ZonedDateTime local = instant.atZone(zone);
        ZonedDateTime candidate = local.toLocalDate().atTime(end).atZone(zone);
        if (!candidate.isAfter(local)) {
            candidate = local.toLocalDate().plusDays(1).atTime(end).atZone(zone);
        }
        return candidate.toInstant();
    }
}
