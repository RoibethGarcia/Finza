package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.ledger.application.port.CategoryStore;
import com.gestorgastos.ledger.domain.model.Category;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCategoryStore implements CategoryStore {

	private final CategoryJpaRepository categoryJpaRepository;
	private final CategoryPersistenceMapper mapper;

	public JpaCategoryStore(CategoryJpaRepository categoryJpaRepository, CategoryPersistenceMapper mapper) {
		this.categoryJpaRepository = categoryJpaRepository;
		this.mapper = mapper;
	}

	@Override
	public Category save(Category category) {
		return mapper.toDomain(categoryJpaRepository.save(mapper.toEntity(category)));
	}

	@Override
	public Optional<Category> findByIdAndUserId(UUID categoryId, UUID userId) {
		return categoryJpaRepository.findByIdAndUserId(categoryId, userId).map(mapper::toDomain);
	}

	@Override
	public List<Category> findAllByUserId(UUID userId) {
		return categoryJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
			.map(mapper::toDomain)
			.toList();
	}
}
