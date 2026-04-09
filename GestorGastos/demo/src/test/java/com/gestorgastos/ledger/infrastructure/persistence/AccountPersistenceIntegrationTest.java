package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.identity.domain.model.UserStatus;
import com.gestorgastos.identity.infrastructure.persistence.UserJpaRepository;
import com.gestorgastos.identity.infrastructure.persistence.entity.UserEntity;
import com.gestorgastos.ledger.domain.model.AccountType;
import com.gestorgastos.ledger.infrastructure.persistence.entity.AccountEntity;
import com.gestorgastos.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountPersistenceIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private AccountJpaRepository accountJpaRepository;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Test
	void shouldPersistAccountAndLookupByOwner() {
		UUID ownerId = createUser("ada@example.com");
		AccountEntity entity = new AccountEntity();
		entity.setId(UUID.randomUUID());
		entity.setUserId(ownerId);
		entity.setName("Caja diaria");
		entity.setType(AccountType.CASH);
		entity.setCurrency("UYU");
		entity.setOpeningBalance(new BigDecimal("450.00"));
		entity.setArchived(false);
		accountJpaRepository.saveAndFlush(entity);

		assertThat(accountJpaRepository.findByIdAndUserId(entity.getId(), ownerId))
			.isPresent()
			.get()
			.extracting(AccountEntity::getName)
			.isEqualTo("Caja diaria");
	}

	@Test
	void shouldListAccountsOnlyForRequestedUser() {
		UUID ownerId = createUser("ada@example.com");
		UUID otherUserId = createUser("grace@example.com");

		accountJpaRepository.saveAndFlush(accountEntity(ownerId, "Caja diaria"));
		accountJpaRepository.saveAndFlush(accountEntity(otherUserId, "Caja ajena"));

		assertThat(accountJpaRepository.findAllByUserIdOrderByCreatedAtDesc(ownerId))
			.extracting(AccountEntity::getName)
			.containsExactly("Caja diaria");
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

	private AccountEntity accountEntity(UUID userId, String name) {
		AccountEntity account = new AccountEntity();
		account.setId(UUID.randomUUID());
		account.setUserId(userId);
		account.setName(name);
		account.setType(AccountType.BANK);
		account.setCurrency("UYU");
		account.setOpeningBalance(new BigDecimal("100.00"));
		account.setArchived(false);
		return account;
	}
}
