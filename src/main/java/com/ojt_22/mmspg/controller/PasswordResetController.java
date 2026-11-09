package com.ojt_22.mmspg.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.dto.ForgotPasswordRequest;
import com.ojt_22.mmspg.dto.ResetPasswordRequest;
import com.ojt_22.mmspg.service.PasswordResetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"http://localhost:8003", "http://127.0.0.1:8003", "http://localhost:5173", "http://127.0.0.1:5173"})
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for login and password reset flows")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot Password", description = "Generates a reset token for the given email if it exists.")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "A password reset code has been sent to your email."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage(), "message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    @Auditable(menuName = "Authentication", action = "RESET_PASSWORD", description = "Password reset with token")
    @Operation(summary = "Reset Password", description = "Resets the password using a valid token.")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password has been successfully updated."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
