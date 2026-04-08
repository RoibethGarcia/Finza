package com.gestorgastos.shared.infrastructure.error;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final ApiErrorCode errorCode;

	public ApiException(HttpStatus status, ApiErrorCode errorCode, String message) {
		super(message);
		this.status = status;
		this.errorCode = errorCode;
	}

	public HttpStatus status() { return status; }
	public ApiErrorCode errorCode() { return errorCode; }
}
