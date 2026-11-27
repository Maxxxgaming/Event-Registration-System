package com.eventregistration.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.eventregistration.dto.ApiResponse;
import com.eventregistration.dto.EventRequest;
import com.eventregistration.dto.EventResponse;
import com.eventregistration.dto.EventStatusUpdateRequest;
import com.eventregistration.dto.PageResponse;
import com.eventregistration.security.CustomUserDetails;
import com.eventregistration.service.EventService;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Create, browse, and manage events")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Browse published events")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getPublishedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Page<EventResponse> result = eventService.getPublishedEvents(page, size, search);
        PageResponse<EventResponse> pageResponse = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Events retrieved successfully."));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an event by ID")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getEventById(id), "Event retrieved successfully."));
    }

    @PostMapping
    @Operation(summary = "Create an event")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        eventService.createEvent(request, principal.id()), "Event created successfully."));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an event")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(eventService.updateEvent(id, request), "Event updated successfully."));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update an event status")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<EventResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EventStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                eventService.updateStatus(id, request.status()), "Event status updated successfully."));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel an event")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<EventResponse>> cancelEvent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.cancelEvent(id), "Event cancelled successfully."));
    }
}
