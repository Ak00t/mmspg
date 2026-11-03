package com.ojt_22.mmspg.exception;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ojt_22.mmspg.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(InvalidTransactionStatusException.class)
	public ResponseEntity<Map<String, String>> handleInvalidTransactionStatus(InvalidTransactionStatusException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(SettlementAlreadyExistsException.class)
	public ResponseEntity<Map<String, String>> handleSettlementAlreadyExists(SettlementAlreadyExistsException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(InvalidAmountException.class)
	public ResponseEntity<Map<String, String>> handleInvalidAmount(InvalidAmountException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(InvalidFeeConfigurationException.class)
	public ResponseEntity<Map<String, String>> handleInvalidFeeConfiguration(InvalidFeeConfigurationException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(MerchantNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleMerchantNotFound(MerchantNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		String errorMessage = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining(", "));
		log.warn("Validation error: {}", errorMessage);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errorMessage));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
		log.warn("Illegal argument: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
		log.warn("Illegal state: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		String detail = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
		log.error("Data integrity violation: {}", detail, ex);
		String message = "Database conflict or duplicate record";
		if (detail != null && detail.contains("Duplicate entry")) {
			message = "Duplicate entry detected: a record with these details already exists.";
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", message));
	}

	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<Map<String, Object>> handleDisabledException(DisabledException ex) {
		log.warn("Account suspended / inactive login attempt: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
				"error", "ACCOUNT_SUSPENDED",
				"suspended", true,
				"message", "Your account has been suspended or is inactive. Please contact administrator support."
		));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
		log.warn("Invalid credentials: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
				"error", "INVALID_CREDENTIALS",
				"suspended", false,
				"message", ex.getMessage() != null ? ex.getMessage() : "Invalid email or password"
		));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		log.error("Unhandled exception caught by GlobalExceptionHandler: ", ex);
		String detailMessage = ex.getMessage() != null && !ex.getMessage().isBlank() 
				? ex.getMessage() 
				: "An unexpected internal server error occurred.";
		ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), detailMessage);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(body);
	}
}
