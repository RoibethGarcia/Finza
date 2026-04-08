package com.gestorgastos.identity.application.security;

import com.gestorgastos.identity.domain.model.IssuedRefreshToken;
import com.gestorgastos.identity.domain.model.RefreshToken;

import java.util.UUID;

public interface RefreshTokenService {

	IssuedRefreshToken issue(UUID userId);
	RefreshToken validate(String rawToken);
	IssuedRefreshToken rotate(String rawToken);
	void revoke(String rawToken);
}
