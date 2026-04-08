package com.gestorgastos.identity.infrastructure.persistence;

import com.gestorgastos.identity.application.port.UserStore;
import com.gestorgastos.identity.domain.model.User;
import com.gestorgastos.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaUserStore implements UserStore {

	private final UserJpaRepository repository;

	public JpaUserStore(UserJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<User> findById(UUID userId) {
		return repository.findById(userId).map(IdentityPersistenceMapper::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return repository.findByEmailIgnoreCase(email).map(IdentityPersistenceMapper::toDomain);
	}

	@Override
	public User save(User user) {
		UserEntity entity = user.id() == null
			? new UserEntity()
			: repository.findById(user.id()).orElseGet(UserEntity::new);
		entity = IdentityPersistenceMapper.toEntity(user, entity);
		entity.setEmail(user.email().toLowerCase());
		return IdentityPersistenceMapper.toDomain(repository.save(entity));
	}
}
