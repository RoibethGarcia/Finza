package com.gestorgastos.ledger.application.port;

import com.gestorgastos.ledger.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryStore {

	Category save(Category category);

	Optional<Category> findByIdAndUserId(UUID categoryId, UUID userId);

	List<Category> findAllByUserId(UUID userId);
}
