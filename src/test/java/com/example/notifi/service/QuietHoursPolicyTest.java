package com.example.notifi.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class QuietHoursPolicyTest {

    private static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");
    private final QuietHoursPolicy policy = new QuietHoursPolicy(LocalTime.of(22, 0), LocalTime.of(8, 0));

    @Test
    void adjust_DaytimeInRecipientZone_Unchanged() {
        Instant noon = Instant.parse("2026-11-17T11:00:00Z");
        assertThat(policy.adjust(noon, WARSAW)).isEqualTo(noon);
    }

    @Test
    void adjust_LateEvening_MovedToNextMorning() {
        Instant evening = Instant.parse("2026-11-17T22:30:00Z");
        assertThat(policy.adjust(evening, WARSAW)).isEqualTo(Instant.parse("2026-11-18T07:00:00Z"));
    }

    @Test
    void adjust_EarlyMorning_MovedToSameMorning() {
        Instant night = Instant.parse("2026-11-17T02:00:00Z");
        assertThat(policy.adjust(night, WARSAW)).isEqualTo(Instant.parse("2026-11-17T07:00:00Z"));
    }

    @Test
    void adjust_SameInstantDifferentZone_DependsOnRecipientZone() {
        Instant instant = Instant.parse("2026-11-17T23:00:00Z");
        assertThat(policy.adjust(instant, ZoneId.of("America/New_York"))).isEqualTo(instant);
        assertThat(policy.adjust(instant, WARSAW)).isEqualTo(Instant.parse("2026-11-18T07:00:00Z"));
    }

    @Test
    void isQuiet_Boundaries() {
        assertThat(policy.isQuiet(Instant.parse("2026-11-17T21:00:00Z"), WARSAW)).isTrue();
        assertThat(policy.isQuiet(Instant.parse("2026-11-17T07:00:00Z"), WARSAW)).isFalse();
    }
}
