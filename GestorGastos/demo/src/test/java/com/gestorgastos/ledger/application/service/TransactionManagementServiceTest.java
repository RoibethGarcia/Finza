package com.gestorgastos.ledger.application.service;

import com.gestorgastos.identity.application.security.CurrentUserProvider;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.ledger.application.port.AccountStore;
import com.gestorgastos.ledger.application.port.CategoryStore;
import com.gestorgastos.ledger.application.port.TransactionStore;
import com.gestorgastos.ledger.domain.model.Account;
import com.gestorgastos.ledger.domain.model.AccountType;
import com.gestorgastos.ledger.domain.model.Category;
import com.gestorgastos.ledger.domain.model.CategoryType;
import com.gestorgastos.ledger.domain.model.LedgerTransaction;
import com.gestorgastos.ledger.domain.model.TransactionType;
import com.gestorgastos.shared.infrastructure.error.ApiErrorCode;
import com.gestorgastos.shared.infrastructure.error.ApiException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionManagementServiceTest {

	private final TransactionStore transactionStore = mock(TransactionStore.class);
	private final AccountStore accountStore = mock(AccountStore.class);
	private final CategoryStore categoryStore = mock(CategoryStore.class);
	private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);

	private final TransactionManagementService service = new TransactionManagementService(
		transactionStore,
		accountStore,
		categoryStore,
		currentUserProvider,
		ZoneId.of("UTC")
	);

	@Test
	void shouldCreateExpenseTransactionForOwnedAccountAndCategory() {
		UUID userId = UUID.randomUUID();
		UUID accountId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(accountStore.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account(accountId, userId, "Cuenta principal", "UYU", false)));
		when(categoryStore.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category(categoryId, userId, "Comida", CategoryType.EXPENSE, false)));
		when(transactionStore.save(any(LedgerTransaction.class))).thenAnswer(invocation -> {
			LedgerTransaction transaction = invocation.getArgument(0);
			return new LedgerTransaction(
				UUID.randomUUID(),
				transaction.userId(),
				transaction.accountId(),
				transaction.categoryId(),
				transaction.type(),
				transaction.amount(),
				transaction.currency(),
				transaction.occurredAt(),
				transaction.description(),
				transaction.referenceType(),
				transaction.referenceId(),
				transaction.transferGroupId(),
				Instant.parse("2026-04-10T12:00:00Z"),
				Instant.parse("2026-04-10T12:00:00Z")
			);
		});

		TransactionView created = service.create(new CreateTransactionCommand(
			accountId,
			categoryId,
			TransactionType.EXPENSE,
			new BigDecimal("250.00"),
			Instant.parse("2026-04-10T11:00:00Z"),
			"  Almuerzo del día  "
		));

		ArgumentCaptor<LedgerTransaction> captor = ArgumentCaptor.forClass(LedgerTransaction.class);
		verify(transactionStore).save(captor.capture());
		assertThat(captor.getValue().currency()).isEqualTo("UYU");
		assertThat(captor.getValue().description()).isEqualTo("Almuerzo del día");
		assertThat(created.categoryName()).isEqualTo("Comida");
	}

	@Test
	void shouldRejectManualTransferTypeFromManualEndpoint() {
		UUID userId = UUID.randomUUID();
		UUID accountId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(accountStore.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account(accountId, userId, "Cuenta principal", "UYU", false)));

		assertThatThrownBy(() -> service.create(new CreateTransactionCommand(
			accountId,
			categoryId,
			TransactionType.TRANSFER_OUT,
			new BigDecimal("10.00"),
			Instant.parse("2026-04-10T11:00:00Z"),
			"Transferencia"
		)))
			.isInstanceOf(ApiException.class)
			.extracting(exception -> ((ApiException) exception).errorCode())
			.isEqualTo(ApiErrorCode.VALIDATION_ERROR);
	}

	@Test
	void shouldRejectCategoryTypeThatDoesNotMatchTransactionType() {
		UUID userId = UUID.randomUUID();
		UUID accountId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(accountStore.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account(accountId, userId, "Cuenta principal", "UYU", false)));
		when(categoryStore.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category(categoryId, userId, "Salario", CategoryType.INCOME, false)));

		assertThatThrownBy(() -> service.create(new CreateTransactionCommand(
			accountId,
			categoryId,
			TransactionType.EXPENSE,
			new BigDecimal("10.00"),
			Instant.parse("2026-04-10T11:00:00Z"),
			"Compra"
		)))
			.isInstanceOf(ApiException.class)
			.extracting(exception -> ((ApiException) exception).errorCode())
			.isEqualTo(ApiErrorCode.VALIDATION_ERROR);
	}

	@Test
	void shouldCreateTwoTransactionsForTransfer() {
		UUID userId = UUID.randomUUID();
		UUID sourceAccountId = UUID.randomUUID();
		UUID targetAccountId = UUID.randomUUID();
		when(currentUserProvider.requireCurrentUser()).thenReturn(currentUser(userId));
		when(accountStore.findByIdAndUserId(sourceAccountId, userId)).thenReturn(Optional.of(account(sourceAccountId, userId, "Banco", "UYU", false)));
		when(accountStore.findByIdAndUserId(targetAccountId, userId)).thenReturn(Optional.of(account(targetAccountId, userId, "Caja", "UYU", false)));
		when(transactionStore.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

		TransferView transfer = service.transfer(new TransferBetweenAccountsCommand(
			sourceAccountId,
			targetAccountId,
			new BigDecimal("500.00"),
			Instant.parse("2026-04-10T11:00:00Z"),
			"Fondeo"
		));

		assertThat(transfer.outgoingTransaction().type()).isEqualTo(TransactionType.TRANSFER_OUT);
		assertThat(transfer.incomingTransaction().type()).isEqualTo(TransactionType.TRANSFER_IN);
		assertThat(transfer.outgoingTransaction().transferGroupId()).isEqualTo(transfer.transferGroupId());
		assertThat(transfer.incomingTransaction().transferGroupId()).isEqualTo(transfer.transferGroupId());
	}

	private AuthenticatedUser currentUser(UUID userId) {
		return new AuthenticatedUser(userId, "ada@example.com", Set.of("USER"));
	}

	private Account account(UUID accountId, UUID userId, String name, String currency, boolean archived) {
		return new Account(
			accountId,
			userId,
			name,
			AccountType.BANK,
			currency,
			new BigDecimal("1000.00"),
			archived,
			Instant.parse("2026-04-10T10:00:00Z"),
			Instant.parse("2026-04-10T10:00:00Z")
		);
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
