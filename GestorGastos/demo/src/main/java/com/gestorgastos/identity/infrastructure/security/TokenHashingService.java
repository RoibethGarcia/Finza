package com.gestorgastos.identity.infrastructure.security;

public interface TokenHashingService {

	String hash(String rawToken);
}
