package com.shrinetours.api.dto.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public record UpdateProfileRequest(
        String name,
        @Email(message = "Email must be valid")
        String email,
        String phone,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dob
) {
}
