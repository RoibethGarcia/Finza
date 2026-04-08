package com.gestorgastos.identity.domain.exception;

public class RefreshTokenExpiredException extends IdentitySecurityException {

	public RefreshTokenExpiredException(String message) { super(message); }
}
