package com.eventregistration.dto;

import jakarta.validation.constraints.NotNull;

import com.eventregistration.entity.EventStatus;

public record EventStatusUpdateRequest(
        @NotNull(message = "Event status is required.")
        EventStatus status
) {
}
