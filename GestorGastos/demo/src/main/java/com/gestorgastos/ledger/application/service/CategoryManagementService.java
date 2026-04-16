package com.gestorgastos.ledger.application.service;

import com.gestorgastos.identity.application.security.CurrentUserProvider;
import com.gestorgastos.ledger.application.port.CategoryStore;
import com.gestorgastos.ledger.domain.model.Category;
import com.gestorgastos.shared.infrastructure.error.ApiErrorCode;
import com.gestorgastos.shared.infrastructure.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryManagementService {

	private static final String CATEGORY_NOT_FOUND_DETAIL = "Category was not found.";

	private final CategoryStore categoryStore;
	private final CurrentUserProvider currentUserProvider;

	public CategoryManagementService(CategoryStore categoryStore, CurrentUserProvider currentUserProvider) {
		this.categoryStore = categoryStore;
		this.currentUserProvider = currentUserProvider;
	}

	@Transactional
	public CategoryView create(CreateCategoryCommand command) {
		Category category = new Category(
			null,
			currentUserProvider.requireCurrentUser().userId(),
			normalizeName(command.name()),
			command.type(),
			false,
			null,
			null
		);
		return toView(categoryStore.save(category));
	}

	@Transactional(readOnly = true)
	public List<CategoryView> findAll(boolean includeArchived) {
		UUID currentUserId = currentUserProvider.requireCurrentUser().userId();
		return categoryStore.findAllByUserId(currentUserId).stream()
			.filter(category -> includeArchived || !category.archived())
			.map(this::toView)
			.toList();
	}

	@Transactional(readOnly = true)
	public CategoryView getById(UUID categoryId) {
		return toView(findOwnedCategory(categoryId));
	}

	@Transactional
	public CategoryView update(UUID categoryId, UpdateCategoryCommand command) {
		Category existingCategory = findOwnedCategory(categoryId);
		Category updatedCategory = new Category(
			existingCategory.id(),
			existingCategory.userId(),
			normalizeName(command.name()),
			command.type(),
			existingCategory.archived(),
			existingCategory.createdAt(),
			existingCategory.updatedAt()
		);
		return toView(categoryStore.save(updatedCategory));
	}

	@Transactional
	public void archive(UUID categoryId) {
		Category existingCategory = findOwnedCategory(categoryId);
		if (existingCategory.archived()) {
			return;
		}

		categoryStore.save(new Category(
			existingCategory.id(),
			existingCategory.userId(),
			existingCategory.name(),
			existingCategory.type(),
			true,
			existingCategory.createdAt(),
			existingCategory.updatedAt()
		));
	}

	private Category findOwnedCategory(UUID categoryId) {
		UUID currentUserId = currentUserProvider.requireCurrentUser().userId();
		return categoryStore.findByIdAndUserId(categoryId, currentUserId)
			.orElseThrow(() -> new ApiException(
				HttpStatus.NOT_FOUND,
				ApiErrorCode.RESOURCE_NOT_FOUND,
				CATEGORY_NOT_FOUND_DETAIL
			));
	}

	private CategoryView toView(Category category) {
		return new CategoryView(
			category.id(),
			category.name(),
			category.type(),
			category.archived(),
			category.createdAt(),
			category.updatedAt()
		);
	}

	private String normalizeName(String rawName) {
		return rawName == null ? null : rawName.trim().replaceAll("\\s+", " ");
	}
}
