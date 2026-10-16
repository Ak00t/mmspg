package com.ojt_22.mmspg.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ojt_22.mmspg.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ex.getMessage());
	}

	@ExceptionHandler(InvalidTransactionStatusException.class)
	public ResponseEntity<String> handleInvalidTransactionStatus(InvalidTransactionStatusException ex) {

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ex.getMessage());
	}

	@ExceptionHandler(SettlementAlreadyExistsException.class)
	public ResponseEntity<String> handleSettlementAlreadyExists(SettlementAlreadyExistsException ex) {

		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ex.getMessage());
	}

	@ExceptionHandler(InvalidAmountException.class)
	public ResponseEntity<String> handleInvalidAmount(InvalidAmountException ex) {

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ex.getMessage());
	}

	@ExceptionHandler(InvalidFeeConfigurationException.class)
	public ResponseEntity<String> handleInvalidFeeConfiguration(InvalidFeeConfigurationException ex) {

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ex.getMessage());
	}

	@ExceptionHandler(MerchantNotFoundException.class)
	public ResponseEntity<String> handelMerchantNotFound(MerchantNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

		ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "An unexpected internal server error occurred.");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(body);
	}
}