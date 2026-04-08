package com.gestorgastos.identity.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gestorgastos.identity.application.security.TokenService;
import com.gestorgastos.identity.domain.exception.InvalidAccessTokenException;
import com.gestorgastos.identity.domain.model.AccessToken;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.shared.infrastructure.config.AppSecurityProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtTokenService implements TokenService {

	private static final long CLOCK_SKEW_SECONDS = 30;

	private final AppSecurityProperties.Jwt properties;
	private final Clock clock;
	private final Algorithm algorithm;

	public JwtTokenService(AppSecurityProperties securityProperties, Clock clock) {
		this.properties = securityProperties.getJwt();
		this.clock = clock;
		validateSecret(properties.getSecret());
		this.algorithm = Algorithm.HMAC256(properties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public AccessToken issueAccessToken(AuthenticatedUser authenticatedUser) {
		Instant issuedAt = Instant.now(clock);
		Instant expiresAt = issuedAt.plus(properties.getAccessTokenTtl());

		String token = JWT.create()
			.withIssuer(properties.getIssuer())
			.withAudience(properties.getAudience())
			.withSubject(authenticatedUser.userId().toString())
			.withClaim("email", authenticatedUser.email())
			.withArrayClaim("roles", authenticatedUser.roles().toArray(String[]::new))
			.withIssuedAt(Date.from(issuedAt))
			.withNotBefore(Date.from(issuedAt))
			.withExpiresAt(Date.from(expiresAt))
			.withJWTId(UUID.randomUUID().toString())
			.sign(algorithm);

		return new AccessToken(token, issuedAt, expiresAt);
	}

	@Override
	public AuthenticatedUser parseAccessToken(String token) {
		try {
			DecodedJWT jwt = JWT.decode(token);
			if (!algorithm.getName().equals(jwt.getAlgorithm())) {
				throw new InvalidAccessTokenException("Access token algorithm is not allowed.");
			}
			algorithm.verify(jwt);
			validateClaims(jwt);

			String subject = jwt.getSubject();
			String email = jwt.getClaim("email").asString();
			List<String> rolesClaim = jwt.getClaim("roles").asList(String.class);

			if (subject == null || subject.isBlank()) {
				throw new InvalidAccessTokenException("Access token subject is missing.");
			}

			return new AuthenticatedUser(
				UUID.fromString(subject),
				email,
				rolesClaim == null ? Set.of() : Set.copyOf(rolesClaim)
			);
		} catch (JWTDecodeException | SignatureVerificationException | IllegalArgumentException exception) {
			throw new InvalidAccessTokenException("Access token verification failed.", exception);
		}
	}

	private void validateClaims(DecodedJWT jwt) {
		Instant now = Instant.now(clock);
		Instant issuedAt = jwt.getIssuedAtAsInstant();
		Instant notBefore = jwt.getNotBeforeAsInstant();
		Instant expiresAt = jwt.getExpiresAtAsInstant();

		if (!properties.getIssuer().equals(jwt.getIssuer())) {
			throw new InvalidAccessTokenException("Access token issuer is invalid.");
		}
		if (jwt.getAudience() == null || !jwt.getAudience().contains(properties.getAudience())) {
			throw new InvalidAccessTokenException("Access token audience is invalid.");
		}
		if (issuedAt == null) {
			throw new InvalidAccessTokenException("Access token issued-at claim is missing.");
		}
		if (notBefore != null && notBefore.minusSeconds(CLOCK_SKEW_SECONDS).isAfter(now)) {
			throw new InvalidAccessTokenException("Access token cannot be used yet.");
		}
		if (expiresAt == null || expiresAt.plusSeconds(CLOCK_SKEW_SECONDS).isBefore(now)) {
			throw new InvalidAccessTokenException("Access token is expired.");
		}
	}

	private void validateSecret(String secret) {
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException("JWT secret must be at least 32 bytes long.");
		}
	}
}
