package com.gestorgastos.identity.infrastructure.security;

import com.gestorgastos.identity.application.security.CurrentUserProvider;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

	@Override
	public Optional<AuthenticatedUser> currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return Optional.empty();
		}
		if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
			return Optional.of(authenticatedUser);
		}
		return Optional.empty();
	}
}
