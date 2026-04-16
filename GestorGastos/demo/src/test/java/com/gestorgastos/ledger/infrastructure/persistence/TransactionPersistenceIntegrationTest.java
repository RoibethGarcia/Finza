package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.identity.domain.model.UserStatus;
import com.gestorgastos.identity.infrastructure.persistence.UserJpaRepository;
import com.gestorgastos.identity.infrastructure.persistence.entity.UserEntity;
import com.gestorgastos.ledger.domain.model.AccountType;
import com.gestorgastos.ledger.domain.model.CategoryType;
import com.gestorgastos.ledger.domain.model.TransactionType;
import com.gestorgastos.ledger.infrastructure.persistence.entity.AccountEntity;
import com.gestorgastos.ledger.infrastructure.persistence.entity.CategoryEntity;
import com.gestorgastos.ledger.infrastructure.persistence.entity.TransactionEntity;
import com.gestorgastos.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionPersistenceIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private TransactionJpaRepository transactionJpaRepository;

	@Autowired
	private AccountJpaRepository accountJpaRepository;

	@Autowired
	private CategoryJpaRepository categoryJpaRepository;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Test
	void shouldPersistTransactionAndLookupByOwner() {
		UUID ownerId = createUser("ada@example.com");
		UUID accountId = createAccount(ownerId);
		UUID categoryId = createCategory(ownerId);
		TransactionEntity entity = new TransactionEntity();
		entity.setId(UUID.randomUUID());
		entity.setUserId(ownerId);
		entity.setAccountId(accountId);
		entity.setCategoryId(categoryId);
		entity.setType(TransactionType.EXPENSE);
		entity.setAmount(new BigDecimal("100.00"));
		entity.setCurrency("UYU");
		entity.setOccurredAt(Instant.parse("2026-04-10T12:00:00Z"));
		entity.setDescription("Compra");
		transactionJpaRepository.saveAndFlush(entity);

		assertThat(transactionJpaRepository.findByIdAndUserId(entity.getId(), ownerId))
			.isPresent()
			.get()
			.extracting(TransactionEntity::getDescription)
			.isEqualTo("Compra");
	}

	@Test
	void shouldListTransactionsOnlyForRequestedUser() {
		UUID ownerId = createUser("ada@example.com");
		UUID otherUserId = createUser("grace@example.com");

		transactionJpaRepository.saveAndFlush(transactionEntity(ownerId, createAccount(ownerId), createCategory(ownerId), "Compra propia"));
		transactionJpaRepository.saveAndFlush(transactionEntity(otherUserId, createAccount(otherUserId), createCategory(otherUserId), "Compra ajena"));

		assertThat(transactionJpaRepository.findAllByUserIdOrderByOccurredAtDescCreatedAtDesc(ownerId))
			.extracting(TransactionEntity::getDescription)
			.containsExactly("Compra propia");
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

	private UUID createAccount(UUID userId) {
		AccountEntity account = new AccountEntity();
		account.setId(UUID.randomUUID());
		account.setUserId(userId);
		account.setName("Cuenta principal");
		account.setType(AccountType.BANK);
		account.setCurrency("UYU");
		account.setOpeningBalance(new BigDecimal("100.00"));
		account.setArchived(false);
		accountJpaRepository.saveAndFlush(account);
		return account.getId();
	}

	private UUID createCategory(UUID userId) {
		CategoryEntity category = new CategoryEntity();
		category.setId(UUID.randomUUID());
		category.setUserId(userId);
		category.setName("Comida");
		category.setType(CategoryType.EXPENSE);
		category.setArchived(false);
		categoryJpaRepository.saveAndFlush(category);
		return category.getId();
	}

	private TransactionEntity transactionEntity(UUID userId, UUID accountId, UUID categoryId, String description) {
		TransactionEntity transaction = new TransactionEntity();
		transaction.setId(UUID.randomUUID());
		transaction.setUserId(userId);
		transaction.setAccountId(accountId);
		transaction.setCategoryId(categoryId);
		transaction.setType(TransactionType.EXPENSE);
		transaction.setAmount(new BigDecimal("100.00"));
		transaction.setCurrency("UYU");
		transaction.setOccurredAt(Instant.parse("2026-04-10T12:00:00Z"));
		transaction.setDescription(description);
		return transaction;
	}
}
