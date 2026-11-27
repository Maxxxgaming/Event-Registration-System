package com.eventregistration.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventRequest(
        @NotBlank(message = "Event title is required.")
        @Size(max = 200, message = "Event title must be 200 characters or fewer.")
        String title,

        @Size(max = 5000, message = "Description must be 5000 characters or fewer.")
        String description,

        @NotBlank(message = "Event location is required.")
        @Size(max = 255, message = "Location must be 255 characters or fewer.")
        String location,

        @NotNull(message = "Event start date and time are required.")
        LocalDateTime startDateTime,

        @NotNull(message = "Event end date and time are required.")
        LocalDateTime endDateTime,

        @NotNull(message = "Registration deadline is required.")
        LocalDateTime registrationDeadline,

        @NotNull(message = "Event capacity is required.")
        @Min(value = 1, message = "Event capacity must be at least 1.")
        Integer capacity
) {
}
