package com.gestorgastos.identity.application.service;

import com.gestorgastos.identity.application.port.UserStore;
import com.gestorgastos.identity.application.security.CurrentUserProvider;
import com.gestorgastos.identity.application.security.PasswordHasher;
import com.gestorgastos.identity.application.security.RefreshTokenService;
import com.gestorgastos.identity.application.security.TokenService;
import com.gestorgastos.identity.domain.exception.RefreshTokenExpiredException;
import com.gestorgastos.identity.domain.exception.RefreshTokenNotFoundException;
import com.gestorgastos.identity.domain.exception.RefreshTokenRevokedException;
import com.gestorgastos.identity.domain.model.AccessToken;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.identity.domain.model.IssuedRefreshToken;
import com.gestorgastos.identity.domain.model.User;
import com.gestorgastos.identity.domain.model.UserStatus;
import com.gestorgastos.shared.infrastructure.error.ApiErrorCode;
import com.gestorgastos.shared.infrastructure.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class IdentityAuthenticationService {

	private static final Set<String> DEFAULT_ROLES = Set.of("USER");

	private final UserStore userStore;
	private final PasswordHasher passwordHasher;
	private final TokenService tokenService;
	private final RefreshTokenService refreshTokenService;
	private final CurrentUserProvider currentUserProvider;

	public IdentityAuthenticationService(
			UserStore userStore,
			PasswordHasher passwordHasher,
			TokenService tokenService,
			RefreshTokenService refreshTokenService,
			CurrentUserProvider currentUserProvider
	) {
		this.userStore = userStore;
		this.passwordHasher = passwordHasher;
		this.tokenService = tokenService;
		this.refreshTokenService = refreshTokenService;
		this.currentUserProvider = currentUserProvider;
	}

	@Transactional
	public AuthenticatedSession register(RegisterIdentityCommand command) {
		String normalizedEmail = normalizeEmail(command.email());
		if (userStore.findByEmail(normalizedEmail).isPresent()) {
			throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, "Email is already registered.");
		}

		User user = userStore.save(new User(
			null,
			normalizeFullName(command.fullName()),
			normalizedEmail,
			command.birthDate(),
			passwordHasher.hash(command.password()),
			UserStatus.ACTIVE,
			null,
			null
		));

		return issueSession(user, refreshTokenService.issue(user.id()));
	}

	@Transactional(readOnly = true)
	public AuthenticatedSession login(LoginIdentityCommand command) {
		String normalizedEmail = normalizeEmail(command.email());
		User user = userStore.findByEmail(normalizedEmail)
			.orElseThrow(() -> invalidCredentials());

		if (!passwordHasher.matches(command.password(), user.passwordHash())) {
			throw invalidCredentials();
		}

		ensureUserIsActive(user);
		return issueSession(user, refreshTokenService.issue(user.id()));
	}

	@Transactional
	public AuthenticatedSession refresh(String rawRefreshToken) {
		try {
			User user = requireExistingUser(refreshTokenService.validate(rawRefreshToken).userId());
			ensureUserIsActive(user);
			return issueSession(user, refreshTokenService.rotate(rawRefreshToken));
		} catch (RefreshTokenNotFoundException | RefreshTokenExpiredException | RefreshTokenRevokedException exception) {
			throw invalidRefreshToken(exception);
		}
	}

	@Transactional
	public void logout(String rawRefreshToken) {
		try {
			refreshTokenService.revoke(rawRefreshToken);
		} catch (RefreshTokenNotFoundException | RefreshTokenExpiredException | RefreshTokenRevokedException ignored) {
			// logout is intentionally idempotent for clients
		}
	}

	@Transactional(readOnly = true)
	public UserProfile me() {
		AuthenticatedUser authenticatedUser = currentUserProvider.requireCurrentUser();
		User user = requireExistingUser(authenticatedUser.userId());
		ensureUserIsActive(user);
		return toUserProfile(user);
	}

	private AuthenticatedSession issueSession(User user, IssuedRefreshToken issuedRefreshToken) {
		AccessToken accessToken = tokenService.issueAccessToken(toAuthenticatedUser(user));
		return new AuthenticatedSession(
			toUserProfile(user),
			accessToken.value(),
			accessToken.expiresAt(),
			issuedRefreshToken.rawToken(),
			issuedRefreshToken.expiresAt()
		);
	}

	private UserProfile toUserProfile(User user) {
		return new UserProfile(user.id(), user.fullName(), user.email(), user.birthDate(), user.status());
	}

	private AuthenticatedUser toAuthenticatedUser(User user) {
		return new AuthenticatedUser(user.id(), user.email(), DEFAULT_ROLES);
	}

	private User requireExistingUser(java.util.UUID userId) {
		return userStore.findById(userId)
			.orElseThrow(() -> new ApiException(
				HttpStatus.UNAUTHORIZED,
				ApiErrorCode.INVALID_ACCESS_TOKEN,
				"The authenticated user no longer exists."
			));
	}

	private void ensureUserIsActive(User user) {
		if (user.status() != UserStatus.ACTIVE) {
			throw new ApiException(HttpStatus.FORBIDDEN, ApiErrorCode.INACTIVE_USER, "The user account is not active.");
		}
	}

	private ApiException invalidCredentials() {
		return new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_CREDENTIALS, "Email or password is invalid.");
	}

	private ApiException invalidRefreshToken(Exception cause) {
		return new ApiException(
			HttpStatus.UNAUTHORIZED,
			ApiErrorCode.INVALID_REFRESH_TOKEN,
			"Refresh token is invalid, expired, or revoked."
		);
	}

	private String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

	private String normalizeFullName(String fullName) {
		return fullName == null ? null : fullName.trim();
	}
}
