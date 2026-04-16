package com.gestorgastos.ledger.application.service;

import com.gestorgastos.identity.application.security.CurrentUserProvider;
import com.gestorgastos.ledger.application.port.AccountStore;
import com.gestorgastos.ledger.application.port.CategoryStore;
import com.gestorgastos.ledger.application.port.TransactionStore;
import com.gestorgastos.ledger.domain.model.Account;
import com.gestorgastos.ledger.domain.model.Category;
import com.gestorgastos.ledger.domain.model.LedgerTransaction;
import com.gestorgastos.ledger.domain.model.TransactionType;
import com.gestorgastos.shared.infrastructure.error.ApiErrorCode;
import com.gestorgastos.shared.infrastructure.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransactionManagementService {

	private static final String ACCOUNT_NOT_FOUND_DETAIL = "Account was not found.";
	private static final String CATEGORY_NOT_FOUND_DETAIL = "Category was not found.";
	private static final String TRANSACTION_NOT_FOUND_DETAIL = "Transaction was not found.";

	private final TransactionStore transactionStore;
	private final AccountStore accountStore;
	private final CategoryStore categoryStore;
	private final CurrentUserProvider currentUserProvider;
	private final ZoneId applicationZoneId;

	public TransactionManagementService(
			TransactionStore transactionStore,
			AccountStore accountStore,
			CategoryStore categoryStore,
			CurrentUserProvider currentUserProvider,
			ZoneId applicationZoneId
	) {
		this.transactionStore = transactionStore;
		this.accountStore = accountStore;
		this.categoryStore = categoryStore;
		this.currentUserProvider = currentUserProvider;
		this.applicationZoneId = applicationZoneId;
	}

	@Transactional
	public TransactionView create(CreateTransactionCommand command) {
		UUID currentUserId = currentUserProvider.requireCurrentUser().userId();
		Account account = requireActiveOwnedAccount(command.accountId(), currentUserId);
		validateManualTransactionType(command.type());
		validatePositiveAmount(command.amount());
		Category category = requireMatchingCategory(command.categoryId(), currentUserId, command.type());

		LedgerTransaction transaction = new LedgerTransaction(
			null,
			currentUserId,
			account.id(),
			category.id(),
			command.type(),
			command.amount(),
			account.currency(),
			normalizeOccurredAt(command.occurredAt()),
			normalizeDescription(command.description()),
			null,
			null,
			null,
			null,
			null
		);

		Map<UUID, Account> accountsById = Map.of(account.id(), account);
		Map<UUID, Category> categoriesById = Map.of(category.id(), category);
		return toView(transactionStore.save(transaction), accountsById, categoriesById);
	}

	@Transactional(readOnly = true)
	public List<TransactionView> findAll(TransactionFilters filters) {
		UUID currentUserId = currentUserProvider.requireCurrentUser().userId();
		Map<UUID, Account> accountsById = accountStore.findAllByUserId(currentUserId).stream()
			.collect(Collectors.toMap(Account::id, Function.identity()));
		Map<UUID, Category> categoriesById = categoryStore.findAllByUserId(currentUserId).stream()
			.collect(Collectors.toMap(Category::id, Function.identity()));

		return transactionStore.findAllByUserId(currentUserId).stream()
			.filter(transaction -> filters.accountId() == null || transaction.accountId().equals(filters.accountId()))
			.filter(transaction -> filters.type() == null || transaction.type() == filters.type())
			.filter(transaction -> isWithinRange(transaction.occurredAt(), filters.from(), filters.to()))
			.sorted(Comparator.comparing(LedgerTransaction::occurredAt).reversed()
				.thenComparing(LedgerTransaction::createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
			.map(transaction -> toView(transaction, accountsById, categoriesById))
			.toList();
	}

	@Transactional(readOnly = true)
	public TransactionView getById(UUID transactionId) {
		UUID currentUserId = currentUserProvider.requireCurrentUser().userId();
		LedgerTransaction transaction = transactionStore.findByIdAndUserId(transactionId, currentUserId)
			.orElseThrow(() -> new ApiException(
				HttpStatus.NOT_FOUND,
				ApiErrorCode.RESOURCE_NOT_FOUND,
				TRANSACTION_NOT_FOUND_DETAIL
			));
		Map<UUID, Account> accountsById = accountStore.findAllByUserId(currentUserId).stream()
			.collect(Collectors.toMap(Account::id, Function.identity()));
		Map<UUID, Category> categoriesById = categoryStore.findAllByUserId(currentUserId).stream()
			.collect(Collectors.toMap(Category::id, Function.identity()));
		return toView(transaction, accountsById, categoriesById);
	}

	@Transactional
	public TransferView transfer(TransferBetweenAccountsCommand command) {
		UUID currentUserId = currentUserProvider.requireCurrentUser().userId();
		validatePositiveAmount(command.amount());
		if (command.sourceAccountId().equals(command.targetAccountId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, "Source and target accounts must be different.");
		}

		Account sourceAccount = requireActiveOwnedAccount(command.sourceAccountId(), currentUserId);
		Account targetAccount = requireActiveOwnedAccount(command.targetAccountId(), currentUserId);
		if (!sourceAccount.currency().equals(targetAccount.currency())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, "Transfers between different currencies are not supported yet.");
		}

		Instant occurredAt = normalizeOccurredAt(command.occurredAt());
		String description = normalizeDescription(command.description());
		UUID transferGroupId = UUID.randomUUID();

		LedgerTransaction outgoing = new LedgerTransaction(
			null,
			currentUserId,
			sourceAccount.id(),
			null,
			TransactionType.TRANSFER_OUT,
			command.amount(),
			sourceAccount.currency(),
			occurredAt,
			description,
			null,
			null,
			transferGroupId,
			null,
			null
		);

		LedgerTransaction incoming = new LedgerTransaction(
			null,
			currentUserId,
			targetAccount.id(),
			null,
			TransactionType.TRANSFER_IN,
			command.amount(),
			targetAccount.currency(),
			occurredAt,
			description,
			null,
			null,
			transferGroupId,
			null,
			null
		);

		List<LedgerTransaction> persisted = transactionStore.saveAll(List.of(outgoing, incoming));
		Map<UUID, Account> accountsById = Map.of(sourceAccount.id(), sourceAccount, targetAccount.id(), targetAccount);
		Map<UUID, Category> categoriesById = Map.of();

		TransactionView outgoingView = toView(
			persisted.stream().filter(transaction -> transaction.type() == TransactionType.TRANSFER_OUT).findFirst().orElseThrow(),
			accountsById,
			categoriesById
		);
		TransactionView incomingView = toView(
			persisted.stream().filter(transaction -> transaction.type() == TransactionType.TRANSFER_IN).findFirst().orElseThrow(),
			accountsById,
			categoriesById
		);
		return new TransferView(transferGroupId, outgoingView, incomingView);
	}

	private Account requireActiveOwnedAccount(UUID accountId, UUID currentUserId) {
		Account account = accountStore.findByIdAndUserId(accountId, currentUserId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, ACCOUNT_NOT_FOUND_DETAIL));
		if (account.archived()) {
			throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, "Archived accounts cannot receive new transactions.");
		}
		return account;
	}

	private Category requireMatchingCategory(UUID categoryId, UUID currentUserId, TransactionType transactionType) {
		if (categoryId == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, "Category is required for income and expense transactions.");
		}

		Category category = categoryStore.findByIdAndUserId(categoryId, currentUserId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, CATEGORY_NOT_FOUND_DETAIL));
		if (category.archived()) {
			throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, "Archived categories cannot be used in new transactions.");
		}
		if (!category.type().name().equals(transactionType.name())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, "Category type must match the transaction type.");
		}
		return category;
	}

	private void validateManualTransactionType(TransactionType transactionType) {
		if (transactionType != TransactionType.INCOME && transactionType != TransactionType.EXPENSE) {
			throw new ApiException(
				HttpStatus.BAD_REQUEST,
				ApiErrorCode.VALIDATION_ERROR,
				"Manual transaction type must be INCOME or EXPENSE."
			);
		}
	}

	private void validatePositiveAmount(BigDecimal amount) {
		if (amount == null || amount.signum() <= 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, "Amount must be greater than zero.");
		}
	}

	private boolean isWithinRange(Instant occurredAt, LocalDate from, LocalDate to) {
		LocalDate occurredDate = occurredAt.atZone(applicationZoneId).toLocalDate();
		boolean afterStart = from == null || !occurredDate.isBefore(from);
		boolean beforeEnd = to == null || !occurredDate.isAfter(to);
		return afterStart && beforeEnd;
	}

	private TransactionView toView(
			LedgerTransaction transaction,
			Map<UUID, Account> accountsById,
			Map<UUID, Category> categoriesById
	) {
		Account account = accountsById.get(transaction.accountId());
		Category category = transaction.categoryId() == null ? null : categoriesById.get(transaction.categoryId());
		return new TransactionView(
			transaction.id(),
			transaction.accountId(),
			account == null ? null : account.name(),
			transaction.categoryId(),
			category == null ? null : category.name(),
			transaction.type(),
			transaction.amount(),
			transaction.currency(),
			transaction.occurredAt(),
			transaction.description(),
			transaction.transferGroupId(),
			transaction.referenceType(),
			transaction.referenceId(),
			transaction.createdAt(),
			transaction.updatedAt()
		);
	}

	private Instant normalizeOccurredAt(Instant occurredAt) {
		return occurredAt == null ? Instant.now() : occurredAt;
	}

	private String normalizeDescription(String rawDescription) {
		if (rawDescription == null) {
			return null;
		}
		String normalized = rawDescription.trim().replaceAll("\\s+", " ");
		return normalized.isBlank() ? null : normalized;
	}
}
