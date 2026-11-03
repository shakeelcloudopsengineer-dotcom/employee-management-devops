package com.shakeel.employeemanagement.exception;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.shakeel.employeemanagement.dto.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EmployeeNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(EmployeeNotFoundException exception, WebRequest request) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(DuplicateEmployeeEmailException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
			DuplicateEmployeeEmailException exception, WebRequest request) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException exception, WebRequest request) {
		Map<String, String> validationErrors = new TreeMap<>();
		exception.getBindingResult().getFieldErrors().forEach(fieldError ->
				validationErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
		return error(HttpStatus.BAD_REQUEST, "Request validation failed", request, validationErrors);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
			DataIntegrityViolationException exception, WebRequest request) {
		return error(HttpStatus.CONFLICT, "The request conflicts with an existing employee record",
				request, Map.of());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
			HttpMessageNotReadableException exception, WebRequest request) {
		return error(HttpStatus.BAD_REQUEST, "Request body is malformed or contains an invalid value",
				request, Map.of());
	}

	private ResponseEntity<ApiErrorResponse> error(
			HttpStatus status, String message, WebRequest request, Map<String, String> validationErrors) {
		String path = ((ServletWebRequest) request).getRequest().getRequestURI();
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(), status.value(), status.getReasonPhrase(), message, path, validationErrors);
		return ResponseEntity.status(status).body(body);
	}
}
