package com.gestorgastos.identity.application.security;

import com.gestorgastos.identity.domain.model.AuthenticatedUser;

import java.util.Optional;

public interface CurrentUserProvider {

	Optional<AuthenticatedUser> currentUser();

	default AuthenticatedUser requireCurrentUser() {
		return currentUser().orElseThrow(() -> new IllegalStateException("Current user is not available in the security context."));
	}
}
