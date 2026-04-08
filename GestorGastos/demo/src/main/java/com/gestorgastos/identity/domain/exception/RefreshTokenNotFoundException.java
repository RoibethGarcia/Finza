package com.gestorgastos.identity.domain.exception;

public class RefreshTokenNotFoundException extends IdentitySecurityException {

	public RefreshTokenNotFoundException(String message) { super(message); }
}
