package com.eaglebank.bankapi.error;

import com.eaglebank.generated.model.BadRequestErrorResponse;
import com.eaglebank.generated.model.BadRequestErrorResponseDetailsInner;
import com.eaglebank.generated.model.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<BadRequestErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<BadRequestErrorResponseDetailsInner> details = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(ApiExceptionHandler::toDetail)
				.collect(Collectors.toList());
		if (details.isEmpty()) {
			details = List.of(new BadRequestErrorResponseDetailsInner()
					.field("request")
					.message("Validation failed")
					.type("validation"));
		}
		return ResponseEntity.badRequest().body(new BadRequestErrorResponse()
				.message("Invalid request")
				.details(details));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<BadRequestErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
		List<BadRequestErrorResponseDetailsInner> details = ex.getConstraintViolations()
				.stream()
				.map(violation -> new BadRequestErrorResponseDetailsInner()
						.field(violation.getPropertyPath().toString())
						.message(violation.getMessage())
						.type("validation"))
				.collect(Collectors.toList());
		return ResponseEntity.badRequest().body(new BadRequestErrorResponse()
				.message("Invalid request")
				.details(details));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<BadRequestErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
		BadRequestErrorResponseDetailsInner detail = new BadRequestErrorResponseDetailsInner()
				.field("body")
				.message("Malformed JSON request")
				.type("invalid");
		return ResponseEntity.badRequest().body(new BadRequestErrorResponse()
				.message("Invalid request")
				.details(List.of(detail)));
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse().message(ex.getMessage()));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ErrorResponse().message(ex.getMessage()));
	}

	@ExceptionHandler(UnprocessableEntityException.class)
	public ResponseEntity<ErrorResponse> handleUnprocessable(UnprocessableEntityException ex) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(new ErrorResponse().message(ex.getMessage()));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
		String message = ex.getMessage();
		if (message == null || message.isBlank()) {
			message = "The user is not allowed to access the transaction";
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ErrorResponse().message(message));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException ex) {
		String message = ex.getMessage();
		if (message == null || message.isBlank()) {
			message = "Access token is missing or invalid";
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponse().message(message));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse().message("An unexpected error occurred"));
	}

	private static BadRequestErrorResponseDetailsInner toDetail(FieldError error) {
		return new BadRequestErrorResponseDetailsInner()
				.field(error.getField())
				.message(error.getDefaultMessage())
				.type("validation");
	}
}
