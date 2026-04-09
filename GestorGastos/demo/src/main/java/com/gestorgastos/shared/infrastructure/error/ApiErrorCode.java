package com.gestorgastos.shared.infrastructure.error;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {

	AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH-001", "Authentication is required to access this resource."),
	INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-002", "Access token is invalid or expired."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH-003", "You do not have permission to access this resource."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-004", "Email or password is invalid."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-005", "Refresh token is invalid, expired, or revoked."),
	INACTIVE_USER(HttpStatus.FORBIDDEN, "AUTH-006", "The user account is not active."),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "GEN-001", "The request contains invalid data."),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "GEN-002", "The requested resource was not found."),
	CONFLICT(HttpStatus.CONFLICT, "GEN-003", "The request conflicts with the current state."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GEN-999", "An unexpected internal error occurred.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultDetail;

	ApiErrorCode(HttpStatus httpStatus, String code, String defaultDetail) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.defaultDetail = defaultDetail;
	}

	public HttpStatus httpStatus() { return httpStatus; }
	public String code() { return code; }
	public String defaultDetail() { return defaultDetail; }
}
