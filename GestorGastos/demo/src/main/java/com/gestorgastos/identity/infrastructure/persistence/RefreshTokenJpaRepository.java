package com.gestorgastos.identity.infrastructure.persistence;

import com.gestorgastos.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

	Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
	Optional<RefreshTokenEntity> findByIdAndUserId(UUID id, UUID userId);
	List<RefreshTokenEntity> findAllByUserId(UUID userId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update RefreshTokenEntity token set token.revokedAt = :revokedAt where token.userId = :userId and token.revokedAt is null")
	void revokeActiveByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);
}
