package com.gestorgastos.reporting.application.service;

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
import java.time.Clock;
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
public class DashboardSummaryService {

	private final AccountStore accountStore;
	private final CategoryStore categoryStore;
	private final TransactionStore transactionStore;
	private final CurrentUserProvider currentUserProvider;
	private final Clock clock;
	private final ZoneId applicationZoneId;

	public DashboardSummaryService(
			AccountStore accountStore,
			CategoryStore categoryStore,
			TransactionStore transactionStore,
			CurrentUserProvider currentUserProvider,
			Clock applicationClock,
			ZoneId applicationZoneId
	) {
		this.accountStore = accountStore;
		this.categoryStore = categoryStore;
		this.transactionStore = transactionStore;
		this.currentUserProvider = currentUserProvider;
		this.clock = applicationClock;
		this.applicationZoneId = applicationZoneId;
	}

	@Transactional(readOnly = true)
	public DashboardSummaryView getSummary(LocalDate from, LocalDate to) {
		LocalDate periodEnd = to == null ? LocalDate.now(clock) : to;
		LocalDate periodStart = from == null ? periodEnd.minusDays(29) : from;
		validatePeriod(periodStart, periodEnd);

		UUID currentUserId = currentUserProvider.requireCurrentUser().userId();
		List<Account> accounts = accountStore.findAllByUserId(currentUserId).stream()
			.filter(account -> !account.archived())
			.toList();
		List<LedgerTransaction> transactions = transactionStore.findAllByUserId(currentUserId);
		Map<UUID, Category> categoriesById = categoryStore.findAllByUserId(currentUserId).stream()
			.collect(Collectors.toMap(Category::id, Function.identity()));

		List<AccountBalanceView> accountBalances = accounts.stream()
			.map(account -> toAccountBalance(account, transactions, periodStart, periodEnd))
			.sorted(Comparator.comparing(AccountBalanceView::currency).thenComparing(AccountBalanceView::accountName))
			.toList();

		List<CurrencySummaryView> currencies = accountBalances.stream()
			.collect(Collectors.groupingBy(AccountBalanceView::currency))
			.entrySet().stream()
			.map(entry -> {
				String currency = entry.getKey();
				List<AccountBalanceView> balanceViews = entry.getValue();
				BigDecimal totalIncome = sum(balanceViews.stream().map(AccountBalanceView::periodIncome).toList());
				BigDecimal totalExpense = sum(balanceViews.stream().map(AccountBalanceView::periodExpense).toList());
				BigDecimal availableBalance = sum(balanceViews.stream().map(AccountBalanceView::currentBalance).toList());
				return new CurrencySummaryView(currency, totalIncome, totalExpense, totalIncome.subtract(totalExpense), availableBalance);
			})
			.sorted(Comparator.comparing(CurrencySummaryView::currency))
			.toList();

		Map<UUID, Account> accountsById = accounts.stream()
			.collect(Collectors.toMap(Account::id, Function.identity()));
		List<RecentTransactionView> recentTransactions = transactions.stream()
			.sorted(Comparator.comparing(LedgerTransaction::occurredAt).reversed())
			.limit(5)
			.map(transaction -> toRecentTransaction(transaction, accountsById, categoriesById))
			.toList();

		return new DashboardSummaryView(periodStart, periodEnd, currencies, accountBalances, recentTransactions);
	}

	@Transactional(readOnly = true)
	public BalanceReportView getBalanceReport(LocalDate from, LocalDate to) {
		DashboardSummaryView summary = getSummary(from, to);
		return new BalanceReportView(summary.periodStart(), summary.periodEnd(), summary.currencies(), summary.accounts());
	}

	private AccountBalanceView toAccountBalance(
			Account account,
			List<LedgerTransaction> transactions,
			LocalDate periodStart,
			LocalDate periodEnd
	) {
		List<LedgerTransaction> accountTransactions = transactions.stream()
			.filter(transaction -> transaction.accountId().equals(account.id()))
			.toList();
		List<LedgerTransaction> periodTransactions = accountTransactions.stream()
			.filter(transaction -> isWithinPeriod(transaction.occurredAt(), periodStart, periodEnd))
			.toList();

		BigDecimal currentBalance = account.openingBalance().add(sumSigned(accountTransactions));
		BigDecimal periodIncome = sumByType(periodTransactions, TransactionType.INCOME);
		BigDecimal periodExpense = sumByType(periodTransactions, TransactionType.EXPENSE);
		return new AccountBalanceView(
			account.id(),
			account.name(),
			account.currency(),
			account.openingBalance(),
			currentBalance,
			periodIncome,
			periodExpense,
			periodIncome.subtract(periodExpense)
		);
	}

	private RecentTransactionView toRecentTransaction(
			LedgerTransaction transaction,
			Map<UUID, Account> accountsById,
			Map<UUID, Category> categoriesById
	) {
		Account account = accountsById.get(transaction.accountId());
		Category category = transaction.categoryId() == null ? null : categoriesById.get(transaction.categoryId());
		return new RecentTransactionView(
			transaction.id(),
			transaction.accountId(),
			account == null ? null : account.name(),
			transaction.categoryId(),
			category == null ? null : category.name(),
			transaction.type(),
			transaction.amount(),
			transaction.currency(),
			transaction.occurredAt(),
			transaction.description()
		);
	}

	private boolean isWithinPeriod(Instant occurredAt, LocalDate periodStart, LocalDate periodEnd) {
		LocalDate occurredDate = occurredAt.atZone(applicationZoneId).toLocalDate();
		return !occurredDate.isBefore(periodStart) && !occurredDate.isAfter(periodEnd);
	}

	private BigDecimal sumByType(List<LedgerTransaction> transactions, TransactionType transactionType) {
		return sum(transactions.stream()
			.filter(transaction -> transaction.type() == transactionType)
			.map(LedgerTransaction::amount)
			.toList());
	}

	private BigDecimal sumSigned(List<LedgerTransaction> transactions) {
		return transactions.stream()
			.map(this::signedAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal signedAmount(LedgerTransaction transaction) {
		return switch (transaction.type()) {
			case INCOME, TRANSFER_IN -> transaction.amount();
			case EXPENSE, TRANSFER_OUT -> transaction.amount().negate();
			case ADJUSTMENT -> BigDecimal.ZERO;
		};
	}

	private BigDecimal sum(List<BigDecimal> amounts) {
		return amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private void validatePeriod(LocalDate from, LocalDate to) {
		if (from.isAfter(to)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, "Period start must be before or equal to period end.");
		}
	}
}
