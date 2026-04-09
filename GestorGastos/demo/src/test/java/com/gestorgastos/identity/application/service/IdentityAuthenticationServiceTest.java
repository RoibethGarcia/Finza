package com.gestorgastos.identity.application.service;

import com.gestorgastos.identity.application.port.UserStore;
import com.gestorgastos.identity.application.security.CurrentUserProvider;
import com.gestorgastos.identity.application.security.PasswordHasher;
import com.gestorgastos.identity.application.security.RefreshTokenService;
import com.gestorgastos.identity.application.security.TokenService;
import com.gestorgastos.identity.domain.exception.RefreshTokenNotFoundException;
import com.gestorgastos.identity.domain.model.AccessToken;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.identity.domain.model.IssuedRefreshToken;
import com.gestorgastos.identity.domain.model.RefreshToken;
import com.gestorgastos.identity.domain.model.User;
import com.gestorgastos.identity.domain.model.UserStatus;
import com.gestorgastos.shared.infrastructure.error.ApiErrorCode;
import com.gestorgastos.shared.infrastructure.error.ApiException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdentityAuthenticationServiceTest {

	private final UserStore userStore = mock(UserStore.class);
	private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
	private final TokenService tokenService = mock(TokenService.class);
	private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
	private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);

	private final IdentityAuthenticationService service = new IdentityAuthenticationService(
		userStore,
		passwordHasher,
		tokenService,
		refreshTokenService,
		currentUserProvider
	);

	@Test
	void shouldRegisterNewUserAndIssueSession() {
		LocalDate birthDate = LocalDate.of(1815, 12, 10);
		UUID userId = UUID.randomUUID();
		Instant accessTokenExpiresAt = Instant.parse("2026-04-09T12:15:00Z");
		Instant refreshTokenExpiresAt = Instant.parse("2026-04-16T12:00:00Z");

		when(userStore.findByEmail("ada@example.com")).thenReturn(Optional.empty());
		when(passwordHasher.hash("super-secret")).thenReturn("hashed-password");
		when(userStore.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			return new User(
				userId,
				user.fullName(),
				user.email(),
				user.birthDate(),
				user.passwordHash(),
				user.status(),
				Instant.parse("2026-04-09T12:00:00Z"),
				Instant.parse("2026-04-09T12:00:00Z")
			);
		});
		when(refreshTokenService.issue(userId)).thenReturn(new IssuedRefreshToken(
			UUID.randomUUID(),
			UUID.randomUUID(),
			userId,
			"refresh-token",
			refreshTokenExpiresAt
		));
		when(tokenService.issueAccessToken(any(AuthenticatedUser.class))).thenReturn(
			new AccessToken("access-token", Instant.parse("2026-04-09T12:00:00Z"), accessTokenExpiresAt)
		);

		AuthenticatedSession session = service.register(new RegisterIdentityCommand(
			"  Ada Lovelace  ",
			"ADA@EXAMPLE.COM",
			birthDate,
			"super-secret"
		));

		ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
		verify(userStore).save(savedUserCaptor.capture());
		User savedUser = savedUserCaptor.getValue();

		assertThat(savedUser.fullName()).isEqualTo("Ada Lovelace");
		assertThat(savedUser.email()).isEqualTo("ada@example.com");
		assertThat(savedUser.passwordHash()).isEqualTo("hashed-password");
		assertThat(savedUser.status()).isEqualTo(UserStatus.ACTIVE);

		assertThat(session.user().id()).isEqualTo(userId);
		assertThat(session.user().email()).isEqualTo("ada@example.com");
		assertThat(session.accessToken()).isEqualTo("access-token");
		assertThat(session.refreshToken()).isEqualTo("refresh-token");
		assertThat(session.accessTokenExpiresAt()).isEqualTo(accessTokenExpiresAt);
		assertThat(session.refreshTokenExpiresAt()).isEqualTo(refreshTokenExpiresAt);
	}

	@Test
	void shouldRejectRegisterWhenEmailAlreadyExists() {
		when(userStore.findByEmail("ada@example.com")).thenReturn(Optional.of(user(
			UUID.randomUUID(),
			"Ada Lovelace",
			"ada@example.com",
			UserStatus.ACTIVE
		)));

		assertThatThrownBy(() -> service.register(new RegisterIdentityCommand(
			"Ada Lovelace",
			"ADA@example.com",
			null,
			"super-secret"
		)))
			.isInstanceOf(ApiException.class)
			.extracting(exception -> ((ApiException) exception).errorCode())
			.isEqualTo(ApiErrorCode.CONFLICT);
	}

	@Test
	void shouldRejectLoginWhenPasswordDoesNotMatch() {
		User existingUser = user(UUID.randomUUID(), "Ada Lovelace", "ada@example.com", UserStatus.ACTIVE);
		when(userStore.findByEmail("ada@example.com")).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches("wrong-password", existingUser.passwordHash())).thenReturn(false);

		assertThatThrownBy(() -> service.login(new LoginIdentityCommand("ada@example.com", "wrong-password")))
			.isInstanceOf(ApiException.class)
			.extracting(exception -> ((ApiException) exception).errorCode())
			.isEqualTo(ApiErrorCode.INVALID_CREDENTIALS);
	}

	@Test
	void shouldRotateRefreshTokenForActiveUser() {
		UUID userId = UUID.randomUUID();
		RefreshToken currentToken = new RefreshToken(
			UUID.randomUUID(),
			userId,
			UUID.randomUUID(),
			"hashed-token",
			Instant.parse("2026-04-09T12:00:00Z"),
			Instant.parse("2026-04-16T12:00:00Z"),
			null,
			Instant.parse("2026-04-09T12:00:00Z"),
			Instant.parse("2026-04-09T12:00:00Z")
		);
		User existingUser = user(userId, "Ada Lovelace", "ada@example.com", UserStatus.ACTIVE);
		when(refreshTokenService.validate("refresh-token")).thenReturn(currentToken);
		when(userStore.findById(userId)).thenReturn(Optional.of(existingUser));
		when(refreshTokenService.rotate("refresh-token")).thenReturn(new IssuedRefreshToken(
			UUID.randomUUID(),
			currentToken.familyId(),
			userId,
			"rotated-token",
			Instant.parse("2026-04-16T12:00:00Z")
		));
		when(tokenService.issueAccessToken(any(AuthenticatedUser.class))).thenReturn(
			new AccessToken("new-access-token", Instant.parse("2026-04-09T12:05:00Z"), Instant.parse("2026-04-09T12:20:00Z"))
		);

		AuthenticatedSession session = service.refresh("refresh-token");

		assertThat(session.user().id()).isEqualTo(userId);
		assertThat(session.accessToken()).isEqualTo("new-access-token");
		assertThat(session.refreshToken()).isEqualTo("rotated-token");

		var inOrder = inOrder(refreshTokenService, userStore);
		inOrder.verify(refreshTokenService).validate("refresh-token");
		inOrder.verify(userStore).findById(userId);
		inOrder.verify(refreshTokenService).rotate("refresh-token");
	}

	@Test
	void shouldReturnCurrentAuthenticatedUserProfile() {
		UUID userId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(new AuthenticatedUser(
			userId,
			"ada@example.com",
			Set.of("USER")
		));
		when(userStore.findById(userId)).thenReturn(Optional.of(user(userId, "Ada Lovelace", "ada@example.com", UserStatus.ACTIVE)));

		UserProfile profile = service.me();

		assertThat(profile.id()).isEqualTo(userId);
		assertThat(profile.fullName()).isEqualTo("Ada Lovelace");
		assertThat(profile.email()).isEqualTo("ada@example.com");
		assertThat(profile.status()).isEqualTo(UserStatus.ACTIVE);
	}

	@Test
	void shouldTreatLogoutAsIdempotentWhenRefreshTokenDoesNotExist() {
		doThrow(new RefreshTokenNotFoundException("missing")).when(refreshTokenService).revoke("missing-token");

		assertThatCode(() -> service.logout("missing-token")).doesNotThrowAnyException();
	}

	private User user(UUID id, String fullName, String email, UserStatus status) {
		return new User(
			id,
			fullName,
			email,
			LocalDate.of(1815, 12, 10),
			"hashed-password",
			status,
			Instant.parse("2026-04-09T12:00:00Z"),
			Instant.parse("2026-04-09T12:00:00Z")
		);
	}
}
