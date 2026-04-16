package com.gestorgastos.reporting.api;

import com.gestorgastos.reporting.application.service.AccountBalanceView;
import com.gestorgastos.reporting.application.service.CurrencySummaryView;
import com.gestorgastos.reporting.application.service.DashboardSummaryService;
import com.gestorgastos.reporting.application.service.DashboardSummaryView;
import com.gestorgastos.reporting.application.service.RecentTransactionView;
import com.gestorgastos.ledger.domain.model.TransactionType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

	private final DashboardSummaryService dashboardSummaryService;

	public DashboardController(DashboardSummaryService dashboardSummaryService) {
		this.dashboardSummaryService = dashboardSummaryService;
	}

	@GetMapping
	public DashboardSummaryResponse summary(
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to
	) {
		return toResponse(dashboardSummaryService.getSummary(from, to));
	}

	private DashboardSummaryResponse toResponse(DashboardSummaryView summary) {
		return new DashboardSummaryResponse(
			summary.periodStart(),
			summary.periodEnd(),
			summary.currencies().stream().map(this::toResponse).toList(),
			summary.accounts().stream().map(this::toResponse).toList(),
			summary.recentTransactions().stream().map(this::toResponse).toList()
		);
	}

	private CurrencySummaryResponse toResponse(CurrencySummaryView summary) {
		return new CurrencySummaryResponse(
			summary.currency(),
			summary.totalIncome(),
			summary.totalExpense(),
			summary.netAmount(),
			summary.availableBalance()
		);
	}

	private AccountBalanceResponse toResponse(AccountBalanceView balanceView) {
		return new AccountBalanceResponse(
			balanceView.accountId(),
			balanceView.accountName(),
			balanceView.currency(),
			balanceView.openingBalance(),
			balanceView.currentBalance(),
			balanceView.periodIncome(),
			balanceView.periodExpense(),
			balanceView.periodNet()
		);
	}

	private RecentTransactionResponse toResponse(RecentTransactionView transactionView) {
		return new RecentTransactionResponse(
			transactionView.transactionId(),
			transactionView.accountId(),
			transactionView.accountName(),
			transactionView.categoryId(),
			transactionView.categoryName(),
			transactionView.type(),
			transactionView.amount(),
			transactionView.currency(),
			transactionView.occurredAt(),
			transactionView.description()
		);
	}
}

record DashboardSummaryResponse(
	LocalDate periodStart,
	LocalDate periodEnd,
	List<CurrencySummaryResponse> currencies,
	List<AccountBalanceResponse> accounts,
	List<RecentTransactionResponse> recentTransactions
) {
}

record CurrencySummaryResponse(
	String currency,
	BigDecimal totalIncome,
	BigDecimal totalExpense,
	BigDecimal netAmount,
	BigDecimal availableBalance
) {
}

record AccountBalanceResponse(
	UUID accountId,
	String accountName,
	String currency,
	BigDecimal openingBalance,
	BigDecimal currentBalance,
	BigDecimal periodIncome,
	BigDecimal periodExpense,
	BigDecimal periodNet
) {
}

record RecentTransactionResponse(
	UUID transactionId,
	UUID accountId,
	String accountName,
	UUID categoryId,
	String categoryName,
	TransactionType type,
	BigDecimal amount,
	String currency,
	Instant occurredAt,
	String description
) {
}
