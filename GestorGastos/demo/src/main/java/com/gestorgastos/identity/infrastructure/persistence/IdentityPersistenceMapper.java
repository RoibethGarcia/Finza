package com.gestorgastos.identity.infrastructure.persistence;

import com.gestorgastos.identity.domain.model.RefreshToken;
import com.gestorgastos.identity.domain.model.User;
import com.gestorgastos.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import com.gestorgastos.identity.infrastructure.persistence.entity.UserEntity;

public final class IdentityPersistenceMapper {

	private IdentityPersistenceMapper() {
	}

	public static User toDomain(UserEntity entity) {
		return new User(
			entity.getId(),
			entity.getFullName(),
			entity.getEmail(),
			entity.getBirthDate(),
			entity.getPasswordHash(),
			entity.getStatus(),
			entity.getCreatedAt(),
			entity.getUpdatedAt()
		);
	}

	public static UserEntity toEntity(User domain, UserEntity target) {
		target.setId(domain.id());
		target.setFullName(domain.fullName());
		target.setEmail(domain.email());
		target.setBirthDate(domain.birthDate());
		target.setPasswordHash(domain.passwordHash());
		target.setStatus(domain.status());
		target.setCreatedAt(domain.createdAt());
		target.setUpdatedAt(domain.updatedAt());
		return target;
	}

	public static RefreshToken toDomain(RefreshTokenEntity entity) {
		return new RefreshToken(
			entity.getId(),
			entity.getUserId(),
			entity.getFamilyId(),
			entity.getTokenHash(),
			entity.getIssuedAt(),
			entity.getExpiresAt(),
			entity.getRevokedAt(),
			entity.getCreatedAt(),
			entity.getUpdatedAt()
		);
	}

	public static RefreshTokenEntity toEntity(RefreshToken domain, RefreshTokenEntity target) {
		target.setId(domain.id());
		target.setUserId(domain.userId());
		target.setFamilyId(domain.familyId());
		target.setTokenHash(domain.tokenHash());
		target.setIssuedAt(domain.issuedAt());
		target.setExpiresAt(domain.expiresAt());
		target.setRevokedAt(domain.revokedAt());
		target.setCreatedAt(domain.createdAt());
		target.setUpdatedAt(domain.updatedAt());
		return target;
	}
}
