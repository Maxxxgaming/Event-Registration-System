package com.eventregistration.dto;

import com.eventregistration.entity.Role;

public record UserSummaryResponse(Long id, String fullName, String email, Role role) {
}
