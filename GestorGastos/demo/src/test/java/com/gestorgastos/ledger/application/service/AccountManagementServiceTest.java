package com.gestorgastos.ledger.application.service;

import com.gestorgastos.identity.application.security.CurrentUserProvider;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.ledger.application.port.AccountStore;
import com.gestorgastos.ledger.domain.model.Account;
import com.gestorgastos.ledger.domain.model.AccountType;
import com.gestorgastos.shared.infrastructure.error.ApiErrorCode;
import com.gestorgastos.shared.infrastructure.error.ApiException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
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

class AccountManagementServiceTest {

	private final AccountStore accountStore = mock(AccountStore.class);
	private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);

	private final AccountManagementService service = new AccountManagementService(accountStore, currentUserProvider);

	@Test
	void shouldCreateAccountForCurrentUserNormalizingNameAndCurrency() {
		UUID userId = UUID.randomUUID();
		UUID accountId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(accountStore.save(any(Account.class))).thenAnswer(invocation -> {
			Account account = invocation.getArgument(0);
			return new Account(
				accountId,
				account.userId(),
				account.name(),
				account.type(),
				account.currency(),
				account.openingBalance(),
				account.archived(),
				Instant.parse("2026-04-09T12:00:00Z"),
				Instant.parse("2026-04-09T12:00:00Z")
			);
		});

		AccountView created = service.create(new CreateAccountCommand(
			"  Cuenta principal  ",
			AccountType.BANK,
			"uyu",
			new BigDecimal("1500.00")
		));

		ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
		verify(accountStore).save(accountCaptor.capture());
		Account savedAccount = accountCaptor.getValue();

		assertThat(savedAccount.userId()).isEqualTo(userId);
		assertThat(savedAccount.name()).isEqualTo("Cuenta principal");
		assertThat(savedAccount.currency()).isEqualTo("UYU");
		assertThat(savedAccount.archived()).isFalse();
		assertThat(created.id()).isEqualTo(accountId);
		assertThat(created.currency()).isEqualTo("UYU");
	}

	@Test
	void shouldRejectInvalidCurrencyCode() {
		UUID userId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));

		assertThatThrownBy(() -> service.create(new CreateAccountCommand(
			"Cuenta principal",
			AccountType.BANK,
			"INVALID",
			new BigDecimal("10.00")
		)))
			.isInstanceOf(ApiException.class)
			.extracting(exception -> ((ApiException) exception).errorCode())
			.isEqualTo(ApiErrorCode.VALIDATION_ERROR);
	}

	@Test
	void shouldListOnlyActiveAccountsByDefault() {
		UUID userId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(accountStore.findAllByUserId(userId)).thenReturn(List.of(
			account(UUID.randomUUID(), userId, "Caja diaria", false),
			account(UUID.randomUUID(), userId, "Ahorros 2025", true)
		));

		List<AccountView> accounts = service.findAll(false);

		assertThat(accounts).hasSize(1);
		assertThat(accounts.getFirst().name()).isEqualTo("Caja diaria");
	}

	@Test
	void shouldReturnArchivedAccountsWhenRequested() {
		UUID userId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(accountStore.findAllByUserId(userId)).thenReturn(List.of(
			account(UUID.randomUUID(), userId, "Caja diaria", false),
			account(UUID.randomUUID(), userId, "Ahorros 2025", true)
		));

		List<AccountView> accounts = service.findAll(true);

		assertThat(accounts).hasSize(2);
		assertThat(accounts).extracting(AccountView::archived).containsExactly(false, true);
	}

	@Test
	void shouldUpdateOwnedAccount() {
		UUID userId = UUID.randomUUID();
		UUID accountId = UUID.randomUUID();
		Account existingAccount = account(accountId, userId, "Caja diaria", false);

		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(accountStore.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(existingAccount));
		when(accountStore.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AccountView updated = service.update(accountId, new UpdateAccountCommand(
			"Caja USD",
			AccountType.CASH,
			"usd",
			new BigDecimal("99.90")
		));

		ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
		verify(accountStore).save(accountCaptor.capture());
		Account savedAccount = accountCaptor.getValue();

		assertThat(savedAccount.id()).isEqualTo(accountId);
		assertThat(savedAccount.userId()).isEqualTo(userId);
		assertThat(savedAccount.name()).isEqualTo("Caja USD");
		assertThat(savedAccount.type()).isEqualTo(AccountType.CASH);
		assertThat(savedAccount.currency()).isEqualTo("USD");
		assertThat(savedAccount.openingBalance()).isEqualByComparingTo("99.90");
		assertThat(updated.name()).isEqualTo("Caja USD");
	}

	@Test
	void shouldArchiveOwnedAccountIdempotently() {
		UUID userId = UUID.randomUUID();
		UUID accountId = UUID.randomUUID();
		Account archivedAccount = new Account(
			accountId,
			userId,
			"Caja diaria",
			AccountType.BANK,
			"UYU",
			new BigDecimal("0.00"),
			true,
			Instant.parse("2026-04-09T12:00:00Z"),
			Instant.parse("2026-04-09T12:00:00Z")
		);

		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(accountStore.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(archivedAccount));

		assertThatCode(() -> service.archive(accountId)).doesNotThrowAnyException();

		verify(accountStore, never()).save(any(Account.class));
	}

	@Test
	void shouldRejectAccessToAccountOwnedByAnotherUser() {
		UUID userId = UUID.randomUUID();
		UUID accountId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(accountStore.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getById(accountId))
			.isInstanceOf(ApiException.class)
			.extracting(exception -> ((ApiException) exception).errorCode())
			.isEqualTo(ApiErrorCode.RESOURCE_NOT_FOUND);
	}

	private AuthenticatedUser currentUser(UUID userId) {
		return new AuthenticatedUser(userId, "ada@example.com", Set.of("USER"));
	}

	private Account account(UUID accountId, UUID userId, String name, boolean archived) {
		return new Account(
			accountId,
			userId,
			name,
			AccountType.BANK,
			"UYU",
			new BigDecimal("1500.00"),
			archived,
			Instant.parse("2026-04-09T12:00:00Z"),
			Instant.parse("2026-04-09T12:00:00Z")
		);
	}
}
