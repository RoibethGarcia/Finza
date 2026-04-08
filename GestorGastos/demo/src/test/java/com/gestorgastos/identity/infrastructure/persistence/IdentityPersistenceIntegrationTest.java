package com.gestorgastos.identity.infrastructure.persistence;

import com.gestorgastos.identity.domain.model.UserStatus;
import com.gestorgastos.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import com.gestorgastos.identity.infrastructure.persistence.entity.UserEntity;
import com.gestorgastos.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IdentityPersistenceIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Autowired
	private RefreshTokenJpaRepository refreshTokenJpaRepository;

	@Test
	void shouldPersistUserAndLookupByEmailIgnoringCase() {
		UserEntity user = new UserEntity();
		user.setId(UUID.randomUUID());
		user.setFullName("Ada Lovelace");
		user.setEmail("ada@example.com");
		user.setBirthDate(LocalDate.of(1815, 12, 10));
		user.setPasswordHash("hashed-password");
		user.setStatus(UserStatus.ACTIVE);
		userJpaRepository.saveAndFlush(user);

		assertThat(userJpaRepository.findByEmailIgnoreCase("ADA@EXAMPLE.COM"))
			.isPresent()
			.get()
			.extracting(UserEntity::getFullName)
			.isEqualTo("Ada Lovelace");
	}

	@Test
	void shouldPersistRefreshTokenAndLookupByOwnership() {
		UserEntity user = new UserEntity();
		UUID userId = UUID.randomUUID();
		user.setId(userId);
		user.setFullName("Grace Hopper");
		user.setEmail("grace@example.com");
		user.setPasswordHash("hashed-password");
		user.setStatus(UserStatus.ACTIVE);
		userJpaRepository.saveAndFlush(user);

		RefreshTokenEntity refreshToken = new RefreshTokenEntity();
		UUID tokenId = UUID.randomUUID();
		refreshToken.setId(tokenId);
		refreshToken.setUserId(userId);
		refreshToken.setFamilyId(UUID.randomUUID());
		refreshToken.setTokenHash("abc123hash");
		refreshToken.setIssuedAt(Instant.parse("2026-04-07T00:00:00Z"));
		refreshToken.setExpiresAt(Instant.parse("2026-04-21T00:00:00Z"));
		refreshTokenJpaRepository.saveAndFlush(refreshToken);

		assertThat(refreshTokenJpaRepository.findByIdAndUserId(tokenId, userId)).isPresent();
		assertThat(refreshTokenJpaRepository.findAllByUserId(userId)).hasSize(1);
	}
}
