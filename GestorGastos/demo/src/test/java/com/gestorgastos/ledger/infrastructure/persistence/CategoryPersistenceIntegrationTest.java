package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.identity.domain.model.UserStatus;
import com.gestorgastos.identity.infrastructure.persistence.UserJpaRepository;
import com.gestorgastos.identity.infrastructure.persistence.entity.UserEntity;
import com.gestorgastos.ledger.domain.model.CategoryType;
import com.gestorgastos.ledger.infrastructure.persistence.entity.CategoryEntity;
import com.gestorgastos.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryPersistenceIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private CategoryJpaRepository categoryJpaRepository;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Test
	void shouldPersistCategoryAndLookupByOwner() {
		UUID ownerId = createUser("ada@example.com");
		CategoryEntity entity = new CategoryEntity();
		entity.setId(UUID.randomUUID());
		entity.setUserId(ownerId);
		entity.setName("Salario");
		entity.setType(CategoryType.INCOME);
		entity.setArchived(false);
		categoryJpaRepository.saveAndFlush(entity);

		assertThat(categoryJpaRepository.findByIdAndUserId(entity.getId(), ownerId))
			.isPresent()
			.get()
			.extracting(CategoryEntity::getName)
			.isEqualTo("Salario");
	}

	@Test
	void shouldListCategoriesOnlyForRequestedUser() {
		UUID ownerId = createUser("ada@example.com");
		UUID otherUserId = createUser("grace@example.com");

		categoryJpaRepository.saveAndFlush(categoryEntity(ownerId, "Salario", CategoryType.INCOME));
		categoryJpaRepository.saveAndFlush(categoryEntity(otherUserId, "Ajena", CategoryType.EXPENSE));

		assertThat(categoryJpaRepository.findAllByUserIdOrderByCreatedAtDesc(ownerId))
			.extracting(CategoryEntity::getName)
			.containsExactly("Salario");
	}

	private UUID createUser(String email) {
		UserEntity user = new UserEntity();
		UUID userId = UUID.randomUUID();
		user.setId(userId);
		user.setFullName("Ada Lovelace");
		user.setEmail(email);
		user.setBirthDate(LocalDate.of(1815, 12, 10));
		user.setPasswordHash("hashed-password");
		user.setStatus(UserStatus.ACTIVE);
		userJpaRepository.saveAndFlush(user);
		return userId;
	}

	private CategoryEntity categoryEntity(UUID userId, String name, CategoryType type) {
		CategoryEntity category = new CategoryEntity();
		category.setId(UUID.randomUUID());
		category.setUserId(userId);
		category.setName(name);
		category.setType(type);
		category.setArchived(false);
		return category;
	}
}
