package com.eventregistration.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import com.eventregistration.entity.EventStatus;

public record EventResponse(
        Long id,
        String title,
        String description,
        String location,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        LocalDateTime registrationDeadline,
        int capacity,
        EventStatus status,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt,
        long registeredCount,
        long availableSeats
) {
}
