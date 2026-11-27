package com.eventregistration.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import com.eventregistration.entity.RegistrationStatus;

public record RegistrationResponse(
        Long id,
        Long eventId,
        String eventTitle,
        LocalDateTime eventStartDateTime,
        String eventLocation,
        Long userId,
        String userFullName,
        RegistrationStatus status,
        Instant registeredAt
) {
}
