package com.eventregistration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Full name is required.")
        @Size(max = 100, message = "Full name must be 100 characters or fewer.")
        String fullName,

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 150, message = "Email must be 150 characters or fewer.")
        String email,

        @NotBlank(message = "Password is required.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "Password must be at least 8 characters and include at least one letter and one number."
        )
        String password
) {
}
