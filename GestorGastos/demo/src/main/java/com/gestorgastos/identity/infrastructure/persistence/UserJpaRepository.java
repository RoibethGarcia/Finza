package com.gestorgastos.identity.infrastructure.persistence;

import com.gestorgastos.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

	Optional<UserEntity> findByEmailIgnoreCase(String email);
}
