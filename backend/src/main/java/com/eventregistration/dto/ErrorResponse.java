package com.eventregistration.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ErrorResponse(
        boolean success,
        String message,
        String errorCode,
        int status,
        String path,
        Instant timestamp,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<FieldError> fieldErrors
) {

    public record FieldError(String field, String message) {
    }
}
