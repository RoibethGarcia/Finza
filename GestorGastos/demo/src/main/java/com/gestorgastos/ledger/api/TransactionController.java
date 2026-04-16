package com.gestorgastos.ledger.api;

import com.gestorgastos.ledger.application.service.CreateTransactionCommand;
import com.gestorgastos.ledger.application.service.TransactionFilters;
import com.gestorgastos.ledger.application.service.TransactionManagementService;
import com.gestorgastos.ledger.application.service.TransactionView;
import com.gestorgastos.ledger.application.service.TransferBetweenAccountsCommand;
import com.gestorgastos.ledger.application.service.TransferView;
import com.gestorgastos.ledger.domain.model.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

	private final TransactionManagementService transactionManagementService;

	public TransactionController(TransactionManagementService transactionManagementService) {
		this.transactionManagementService = transactionManagementService;
	}

	@PostMapping
	public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(transactionManagementService.create(
			new CreateTransactionCommand(
				request.accountId(),
				request.categoryId(),
				request.type(),
				request.amount(),
				request.occurredAt(),
				request.description()
			)
		)));
	}

	@PostMapping("/transfers")
	public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody CreateTransferRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(transactionManagementService.transfer(
			new TransferBetweenAccountsCommand(
				request.sourceAccountId(),
				request.targetAccountId(),
				request.amount(),
				request.occurredAt(),
				request.description()
			)
		)));
	}

	@GetMapping
	public List<TransactionResponse> findAll(
			@RequestParam(required = false) UUID accountId,
			@RequestParam(required = false) TransactionType type,
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to
	) {
		return transactionManagementService.findAll(new TransactionFilters(accountId, type, from, to)).stream()
			.map(this::toResponse)
			.toList();
	}

	@GetMapping("/{transactionId}")
	public TransactionResponse getById(@PathVariable UUID transactionId) {
		return toResponse(transactionManagementService.getById(transactionId));
	}

	private TransferResponse toResponse(TransferView transferView) {
		return new TransferResponse(
			transferView.transferGroupId(),
			toResponse(transferView.outgoingTransaction()),
			toResponse(transferView.incomingTransaction())
		);
	}

	private TransactionResponse toResponse(TransactionView transactionView) {
		return new TransactionResponse(
			transactionView.id(),
			transactionView.accountId(),
			transactionView.accountName(),
			transactionView.categoryId(),
			transactionView.categoryName(),
			transactionView.type(),
			transactionView.amount(),
			transactionView.currency(),
			transactionView.occurredAt(),
			transactionView.description(),
			transactionView.transferGroupId(),
			transactionView.referenceType(),
			transactionView.referenceId(),
			transactionView.createdAt(),
			transactionView.updatedAt()
		);
	}
}

record CreateTransactionRequest(
	@NotNull UUID accountId,
	@NotNull UUID categoryId,
	@NotNull TransactionType type,
	@NotNull @Digits(integer = 17, fraction = 2) BigDecimal amount,
	@NotNull Instant occurredAt,
	@Size(max = 255) String description
) {
}

record CreateTransferRequest(
	@NotNull UUID sourceAccountId,
	@NotNull UUID targetAccountId,
	@NotNull @Digits(integer = 17, fraction = 2) BigDecimal amount,
	@NotNull Instant occurredAt,
	@Size(max = 255) String description
) {
}

record TransactionResponse(
	UUID id,
	UUID accountId,
	String accountName,
	UUID categoryId,
	String categoryName,
	TransactionType type,
	BigDecimal amount,
	String currency,
	Instant occurredAt,
	String description,
	UUID transferGroupId,
	String referenceType,
	UUID referenceId,
	Instant createdAt,
	Instant updatedAt
) {
}

record TransferResponse(
	UUID transferGroupId,
	TransactionResponse outgoingTransaction,
	TransactionResponse incomingTransaction
) {
}
