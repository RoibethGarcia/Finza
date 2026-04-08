package com.gestorgastos.shared.infrastructure.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private final ApiProblemFactory apiProblemFactory;

	public GlobalExceptionHandler(ApiProblemFactory apiProblemFactory) {
		this.apiProblemFactory = apiProblemFactory;
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<?> handleApiException(ApiException exception, HttpServletRequest request) {
		return ResponseEntity.status(exception.status())
			.body(apiProblemFactory.create(exception.status(), exception.errorCode(), exception.getMessage(), request));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpServletRequest request) {
		String detail = exception.getBindingResult().getFieldErrors().stream()
			.map(this::formatFieldError)
			.collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(apiProblemFactory.create(
			ApiErrorCode.VALIDATION_ERROR.httpStatus(),
			ApiErrorCode.VALIDATION_ERROR,
			detail.isBlank() ? ApiErrorCode.VALIDATION_ERROR.defaultDetail() : detail,
			request
		));
	}

	@ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class})
	public ResponseEntity<?> handleBadRequest(Exception exception, HttpServletRequest request) {
		return ResponseEntity.badRequest().body(apiProblemFactory.create(
			ApiErrorCode.VALIDATION_ERROR.httpStatus(),
			ApiErrorCode.VALIDATION_ERROR,
			exception.getMessage(),
			request
		));
	}

	@ExceptionHandler({NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
	public ResponseEntity<?> handleNotFound(Exception exception, HttpServletRequest request) {
		return ResponseEntity.status(ApiErrorCode.RESOURCE_NOT_FOUND.httpStatus()).body(apiProblemFactory.create(
			ApiErrorCode.RESOURCE_NOT_FOUND.httpStatus(),
			ApiErrorCode.RESOURCE_NOT_FOUND,
			exception.getMessage(),
			request
		));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<?> handleConflict(DataIntegrityViolationException exception, HttpServletRequest request) {
		return ResponseEntity.status(ApiErrorCode.CONFLICT.httpStatus()).body(apiProblemFactory.create(
			ApiErrorCode.CONFLICT.httpStatus(),
			ApiErrorCode.CONFLICT,
			ApiErrorCode.CONFLICT.defaultDetail(),
			request
		));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleUnexpected(Exception exception, HttpServletRequest request) {
		log.error("Unexpected error while processing request [{} {}]", request.getMethod(), request.getRequestURI(), exception);
		return ResponseEntity.status(ApiErrorCode.INTERNAL_ERROR.httpStatus()).body(apiProblemFactory.create(
			ApiErrorCode.INTERNAL_ERROR.httpStatus(),
			ApiErrorCode.INTERNAL_ERROR,
			ApiErrorCode.INTERNAL_ERROR.defaultDetail(),
			request
		));
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}
}
