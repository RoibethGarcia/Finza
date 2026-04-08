package com.gestorgastos.identity.domain.exception;

public class InvalidAccessTokenException extends IdentitySecurityException {

	public InvalidAccessTokenException(String message, Throwable cause) { super(message, cause); }
	public InvalidAccessTokenException(String message) { super(message); }
}
