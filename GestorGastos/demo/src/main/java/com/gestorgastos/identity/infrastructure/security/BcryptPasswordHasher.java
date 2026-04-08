package com.gestorgastos.identity.infrastructure.security;

import com.gestorgastos.identity.application.security.PasswordHasher;
import com.gestorgastos.shared.infrastructure.config.AppSecurityProperties;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {

	private final BCryptPasswordEncoder encoder;

	public BcryptPasswordHasher(AppSecurityProperties securityProperties) {
		this.encoder = new BCryptPasswordEncoder(securityProperties.getPassword().getBcryptStrength());
	}

	@Override
	public String hash(String rawPassword) {
		return encoder.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String hashedPassword) {
		return encoder.matches(rawPassword, hashedPassword);
	}
}
