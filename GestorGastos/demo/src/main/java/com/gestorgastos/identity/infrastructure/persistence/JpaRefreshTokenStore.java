package com.gestorgastos.identity.infrastructure.persistence;

import com.gestorgastos.identity.application.port.RefreshTokenStore;
import com.gestorgastos.identity.domain.model.RefreshToken;
import com.gestorgastos.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaRefreshTokenStore implements RefreshTokenStore {

	private final RefreshTokenJpaRepository repository;

	public JpaRefreshTokenStore(RefreshTokenJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public RefreshToken save(RefreshToken refreshToken) {
		RefreshTokenEntity entity = refreshToken.id() == null
			? new RefreshTokenEntity()
			: repository.findById(refreshToken.id()).orElseGet(RefreshTokenEntity::new);
		entity = IdentityPersistenceMapper.toEntity(refreshToken, entity);
		return IdentityPersistenceMapper.toDomain(repository.save(entity));
	}

	@Override
	public Optional<RefreshToken> findByTokenHash(String tokenHash) {
		return repository.findByTokenHash(tokenHash).map(IdentityPersistenceMapper::toDomain);
	}

	@Override
	public Optional<RefreshToken> findByIdAndUserId(UUID tokenId, UUID userId) {
		return repository.findByIdAndUserId(tokenId, userId).map(IdentityPersistenceMapper::toDomain);
	}

	@Override
	public List<RefreshToken> findAllByUserId(UUID userId) {
		return repository.findAllByUserId(userId).stream().map(IdentityPersistenceMapper::toDomain).toList();
	}

	@Override
	@Transactional
	public void revokeActiveByUserId(UUID userId, Instant revokedAt) {
		repository.revokeActiveByUserId(userId, revokedAt);
	}
}
