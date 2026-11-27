package com.eventregistration.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.eventregistration.dto.ApiResponse;
import com.eventregistration.dto.RegistrationResponse;
import com.eventregistration.security.CustomUserDetails;
import com.eventregistration.service.RegistrationService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Registrations", description = "Register for events and manage registrations")
@SecurityRequirement(name = "bearerAuth")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/api/events/{eventId}/registrations")
    @Operation(summary = "Register a user for an event")
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerForEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                registrationService.registerForEvent(eventId, principal.id()), "Registered for event successfully."));
    }

    @DeleteMapping("/api/registrations/{id}")
    @Operation(summary = "Cancel a registration")
    public ResponseEntity<ApiResponse<RegistrationResponse>> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                registrationService.cancelRegistration(
                        id, principal.id(), "ADMIN".equals(principal.role())),
                "Registration cancelled successfully."));
    }

    @GetMapping("/api/registrations/me")
    @Operation(summary = "List the current user's registrations")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getMyRegistrations(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                registrationService.getRegistrationsByUser(principal.id()),
                "Your registrations were retrieved successfully."));
    }

    @GetMapping("/api/events/{eventId}/registrations")
    @Operation(summary = "List registrations for an event")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getRegistrationsByEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                registrationService.getRegistrationsByEvent(eventId), "Event registrations retrieved successfully."));
    }
}
