package com.ojt_22.mmspg.exception;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ojt_22.mmspg.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private ResponseEntity<ErrorResponse> buildJsonResponse(HttpStatus status, String message) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message
		);
		return ResponseEntity.status(status)
				.contentType(MediaType.APPLICATION_JSON)
				.body(body);
	}

	@ExceptionHandler(DuplicateTransactionException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateTransaction(DuplicateTransactionException ex) {
		log.warn("Duplicate transaction rejected: {}", ex.getMessage());
		return buildJsonResponse(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
		return buildJsonResponse(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(MerchantNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleMerchantNotFound(MerchantNotFoundException ex) {
		return buildJsonResponse(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(InvalidTransactionStatusException.class)
	public ResponseEntity<ErrorResponse> handleInvalidTransactionStatus(InvalidTransactionStatusException ex) {
		return buildJsonResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(InvalidAmountException.class)
	public ResponseEntity<ErrorResponse> handleInvalidAmount(InvalidAmountException ex) {
		return buildJsonResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(InvalidFeeConfigurationException.class)
	public ResponseEntity<ErrorResponse> handleInvalidFeeConfiguration(InvalidFeeConfigurationException ex) {
		return buildJsonResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(SettlementAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleSettlementAlreadyExists(SettlementAlreadyExistsException ex) {
		return buildJsonResponse(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
		log.warn("Bad request argument: {}", ex.getMessage());
		return buildJsonResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
		log.warn("Illegal state: {}", ex.getMessage());
		return buildJsonResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
		String errorMessage = ex.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining(", "));
		log.warn("Validation failure: {}", errorMessage);
		return buildJsonResponse(HttpStatus.BAD_REQUEST, errorMessage);
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
		log.error("Unhandled exception in API: ", ex);
		String message = (ex.getMessage() != null && !ex.getMessage().isBlank())
				? ex.getMessage()
				: "An unexpected internal server error occurred.";
		return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
	}
}
