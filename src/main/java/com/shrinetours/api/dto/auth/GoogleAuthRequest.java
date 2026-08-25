package com.shrinetours.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
		@NotBlank
		String name,
		@Email
		@NotBlank 
		String email) {
}
