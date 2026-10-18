package com.ojt_22.mmspg.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StaffUserRegisterRequest {

	@Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
	private String username;

	@NotBlank(message = "Password is required")
	@Size(min = 6, message = "Password must be at least 6 characters")
	private String password;

	@NotBlank(message = "Full name is required")
	@Size(max = 150, message = "Full name cannot exceed 150 characters")
	private String fullName;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	@Size(max = 150, message = "Email cannot exceed 150 characters")
	private String email;

	@NotBlank(message = "Role is required")
	@Pattern(regexp = "^(ADMIN|AUDITOR|SUPPORT|STAFF)$", message = "Role must be ADMIN, AUDITOR, SUPPORT, or STAFF")
	private String role;
}
