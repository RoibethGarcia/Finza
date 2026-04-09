package com.gestorgastos.ledger.api;

import com.gestorgastos.ledger.application.service.AccountManagementService;
import com.gestorgastos.ledger.application.service.AccountView;
import com.gestorgastos.ledger.application.service.CreateAccountCommand;
import com.gestorgastos.ledger.application.service.UpdateAccountCommand;
import com.gestorgastos.ledger.domain.model.AccountType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

	private final AccountManagementService accountManagementService;

	public AccountController(AccountManagementService accountManagementService) {
		this.accountManagementService = accountManagementService;
	}

	@PostMapping
	public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(accountManagementService.create(
			new CreateAccountCommand(request.name(), request.type(), request.currency(), request.openingBalance())
		)));
	}

	@GetMapping
	public List<AccountResponse> findAll(@RequestParam(defaultValue = "false") boolean includeArchived) {
		return accountManagementService.findAll(includeArchived).stream()
			.map(this::toResponse)
			.toList();
	}

	@GetMapping("/{accountId}")
	public AccountResponse getById(@PathVariable UUID accountId) {
		return toResponse(accountManagementService.getById(accountId));
	}

	@PutMapping("/{accountId}")
	public AccountResponse update(@PathVariable UUID accountId, @Valid @RequestBody UpdateAccountRequest request) {
		return toResponse(accountManagementService.update(
			accountId,
			new UpdateAccountCommand(request.name(), request.type(), request.currency(), request.openingBalance())
		));
	}

	@DeleteMapping("/{accountId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void archive(@PathVariable UUID accountId) {
		accountManagementService.archive(accountId);
	}

	private AccountResponse toResponse(AccountView accountView) {
		return new AccountResponse(
			accountView.id(),
			accountView.name(),
			accountView.type(),
			accountView.currency(),
			accountView.openingBalance(),
			accountView.archived(),
			accountView.createdAt(),
			accountView.updatedAt()
		);
	}
}

record CreateAccountRequest(
	@NotBlank @Size(max = 120) String name,
	@NotNull AccountType type,
	@NotBlank @Size(min = 3, max = 3) String currency,
	@NotNull @Digits(integer = 17, fraction = 2) BigDecimal openingBalance
) {
}

record UpdateAccountRequest(
	@NotBlank @Size(max = 120) String name,
	@NotNull AccountType type,
	@NotBlank @Size(min = 3, max = 3) String currency,
	@NotNull @Digits(integer = 17, fraction = 2) BigDecimal openingBalance
) {
}

record AccountResponse(
	UUID id,
	String name,
	AccountType type,
	String currency,
	BigDecimal openingBalance,
	boolean archived,
	Instant createdAt,
	Instant updatedAt
) {
}
