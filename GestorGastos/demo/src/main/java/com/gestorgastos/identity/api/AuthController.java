package com.gestorgastos.identity.api;

import com.gestorgastos.identity.application.service.AuthenticatedSession;
import com.gestorgastos.identity.application.service.IdentityAuthenticationService;
import com.gestorgastos.identity.application.service.LoginIdentityCommand;
import com.gestorgastos.identity.application.service.RegisterIdentityCommand;
import com.gestorgastos.identity.application.service.UserProfile;
import com.gestorgastos.identity.domain.model.UserStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final IdentityAuthenticationService authenticationService;

	public AuthController(IdentityAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthSessionResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(authenticationService.register(
			new RegisterIdentityCommand(request.fullName(), request.email(), request.birthDate(), request.password())
		)));
	}

	@PostMapping("/login")
	public AuthSessionResponse login(@Valid @RequestBody LoginRequest request) {
		return toResponse(authenticationService.login(new LoginIdentityCommand(request.email(), request.password())));
	}

	@PostMapping("/refresh")
	public AuthSessionResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return toResponse(authenticationService.refresh(request.refreshToken()));
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@Valid @RequestBody LogoutRequest request) {
		authenticationService.logout(request.refreshToken());
	}

	@GetMapping("/me")
	public UserProfileResponse me() {
		return toResponse(authenticationService.me());
	}

	private AuthSessionResponse toResponse(AuthenticatedSession session) {
		return new AuthSessionResponse(
			"Bearer",
			session.accessToken(),
			session.accessTokenExpiresAt(),
			session.refreshToken(),
			session.refreshTokenExpiresAt(),
			toResponse(session.user())
		);
	}

	private UserProfileResponse toResponse(UserProfile profile) {
		return new UserProfileResponse(
			profile.id(),
			profile.fullName(),
			profile.email(),
			profile.birthDate(),
			profile.status()
		);
	}
}

record RegisterRequest(
	@NotBlank @Size(max = 150) String fullName,
	@NotBlank @Email @Size(max = 320) String email,
	@Past LocalDate birthDate,
	@NotBlank @Size(min = 8, max = 72) String password
) {
}

record LoginRequest(
	@NotBlank @Email @Size(max = 320) String email,
	@NotBlank String password
) {
}

record RefreshTokenRequest(@NotBlank String refreshToken) {
}

record LogoutRequest(@NotBlank String refreshToken) {
}

record AuthSessionResponse(
	String tokenType,
	String accessToken,
	Instant accessTokenExpiresAt,
	String refreshToken,
	Instant refreshTokenExpiresAt,
	UserProfileResponse user
) {
}

record UserProfileResponse(
	UUID id,
	String fullName,
	String email,
	LocalDate birthDate,
	UserStatus status
) {
}
