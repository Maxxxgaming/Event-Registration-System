package com.eventregistration.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.eventregistration.dto.ApiResponse;
import com.eventregistration.dto.EventResponse;
import com.eventregistration.dto.PageResponse;
import com.eventregistration.entity.EventStatus;
import com.eventregistration.service.EventService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative event management")
@SecurityRequirement(name = "bearerAuth")
public class AdminEventController {

    private final EventService eventService;

    @GetMapping("/api/admin/events")
    @Operation(summary = "Browse all events, optionally filtered by status")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) EventStatus status
    ) {
        Page<EventResponse> result = eventService.getAllEvents(page, size, status);
        PageResponse<EventResponse> pageResponse = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "All events were retrieved successfully."));
    }
}
