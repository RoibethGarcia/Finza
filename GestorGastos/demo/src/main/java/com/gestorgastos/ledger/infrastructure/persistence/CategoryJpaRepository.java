package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.ledger.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {

	Optional<CategoryEntity> findByIdAndUserId(UUID categoryId, UUID userId);

	List<CategoryEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
