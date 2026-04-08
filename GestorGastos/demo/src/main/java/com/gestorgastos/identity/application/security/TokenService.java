package com.gestorgastos.identity.application.security;

import com.gestorgastos.identity.domain.model.AccessToken;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;

public interface TokenService {

	AccessToken issueAccessToken(AuthenticatedUser authenticatedUser);
	AuthenticatedUser parseAccessToken(String token);
}
