package com.gestorgastos.ledger.application.service;

import com.gestorgastos.identity.application.security.CurrentUserProvider;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.ledger.application.port.CategoryStore;
import com.gestorgastos.ledger.domain.model.Category;
import com.gestorgastos.ledger.domain.model.CategoryType;
import com.gestorgastos.shared.infrastructure.error.ApiErrorCode;
import com.gestorgastos.shared.infrastructure.error.ApiException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryManagementServiceTest {

	private final CategoryStore categoryStore = mock(CategoryStore.class);
	private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);

	private final CategoryManagementService service = new CategoryManagementService(categoryStore, currentUserProvider);

	@Test
	void shouldCreateCategoryForCurrentUserNormalizingName() {
		UUID userId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(categoryStore.save(any(Category.class))).thenAnswer(invocation -> {
			Category category = invocation.getArgument(0);
			return new Category(
				categoryId,
				category.userId(),
				category.name(),
				category.type(),
				category.archived(),
				Instant.parse("2026-04-10T10:00:00Z"),
				Instant.parse("2026-04-10T10:00:00Z")
			);
		});

		CategoryView created = service.create(new CreateCategoryCommand("  Salario mensual  ", CategoryType.INCOME));

		ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
		verify(categoryStore).save(categoryCaptor.capture());
		assertThat(categoryCaptor.getValue().name()).isEqualTo("Salario mensual");
		assertThat(created.id()).isEqualTo(categoryId);
		assertThat(created.type()).isEqualTo(CategoryType.INCOME);
	}

	@Test
	void shouldListOnlyActiveCategoriesByDefault() {
		UUID userId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(categoryStore.findAllByUserId(userId)).thenReturn(List.of(
			category(UUID.randomUUID(), userId, "Salario", CategoryType.INCOME, false),
			category(UUID.randomUUID(), userId, "Comida", CategoryType.EXPENSE, true)
		));

		List<CategoryView> categories = service.findAll(false);

		assertThat(categories).hasSize(1);
		assertThat(categories.getFirst().name()).isEqualTo("Salario");
	}

	@Test
	void shouldArchiveCategoryIdempotently() {
		UUID userId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(categoryStore.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(
			category(categoryId, userId, "Comida", CategoryType.EXPENSE, true)
		));

		assertThatCode(() -> service.archive(categoryId)).doesNotThrowAnyException();

		verify(categoryStore, never()).save(any(Category.class));
	}

	@Test
	void shouldRejectAccessToCategoryOwnedByAnotherUser() {
		UUID userId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(categoryStore.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getById(categoryId))
			.isInstanceOf(ApiException.class)
			.extracting(exception -> ((ApiException) exception).errorCode())
			.isEqualTo(ApiErrorCode.RESOURCE_NOT_FOUND);
	}

	private AuthenticatedUser currentUser(UUID userId) {
		return new AuthenticatedUser(userId, "ada@example.com", Set.of("USER"));
	}

	private Category category(UUID categoryId, UUID userId, String name, CategoryType type, boolean archived) {
		return new Category(
			categoryId,
			userId,
			name,
			type,
			archived,
			Instant.parse("2026-04-10T10:00:00Z"),
			Instant.parse("2026-04-10T10:00:00Z")
		);
	}
}
