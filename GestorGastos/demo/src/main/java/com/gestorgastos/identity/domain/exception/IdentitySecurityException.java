package com.gestorgastos.identity.domain.exception;

public class IdentitySecurityException extends RuntimeException {

	public IdentitySecurityException(String message) { super(message); }
	public IdentitySecurityException(String message, Throwable cause) { super(message, cause); }
}
