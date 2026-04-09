package com.gestorgastos.ledger.application.service;

import com.gestorgastos.identity.application.security.CurrentUserProvider;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.ledger.application.port.AccountStore;
import com.gestorgastos.ledger.domain.model.Account;
import com.gestorgastos.shared.infrastructure.error.ApiErrorCode;
import com.gestorgastos.shared.infrastructure.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AccountManagementService {

	private static final String INVALID_CURRENCY_DETAIL = "Currency must be a valid ISO 4217 code.";
	private static final String ACCOUNT_NOT_FOUND_DETAIL = "Account was not found.";

	private final AccountStore accountStore;
	private final CurrentUserProvider currentUserProvider;

	public AccountManagementService(AccountStore accountStore, CurrentUserProvider currentUserProvider) {
		this.accountStore = accountStore;
		this.currentUserProvider = currentUserProvider;
	}

	@Transactional
	public AccountView create(CreateAccountCommand command) {
		AuthenticatedUser currentUser = currentUserProvider.requireCurrentUser();
		Account account = new Account(
			null,
			currentUser.userId(),
			normalizeName(command.name()),
			command.type(),
			normalizeCurrency(command.currency()),
			command.openingBalance(),
			false,
			null,
			null
		);
		return toView(accountStore.save(account));
	}

	@Transactional(readOnly = true)
	public List<AccountView> findAll(boolean includeArchived) {
		UUID currentUserId = currentUserProvider.requireCurrentUser().userId();
		return accountStore.findAllByUserId(currentUserId).stream()
			.filter(account -> includeArchived || !account.archived())
			.map(this::toView)
			.toList();
	}

	@Transactional(readOnly = true)
	public AccountView getById(UUID accountId) {
		return toView(findOwnedAccount(accountId));
	}

	@Transactional
	public AccountView update(UUID accountId, UpdateAccountCommand command) {
		Account existingAccount = findOwnedAccount(accountId);
		Account updatedAccount = new Account(
			existingAccount.id(),
			existingAccount.userId(),
			normalizeName(command.name()),
			command.type(),
			normalizeCurrency(command.currency()),
			command.openingBalance(),
			existingAccount.archived(),
			existingAccount.createdAt(),
			existingAccount.updatedAt()
		);
		return toView(accountStore.save(updatedAccount));
	}

	@Transactional
	public void archive(UUID accountId) {
		Account existingAccount = findOwnedAccount(accountId);
		if (existingAccount.archived()) {
			return;
		}
		accountStore.save(new Account(
			existingAccount.id(),
			existingAccount.userId(),
			existingAccount.name(),
			existingAccount.type(),
			existingAccount.currency(),
			existingAccount.openingBalance(),
			true,
			existingAccount.createdAt(),
			existingAccount.updatedAt()
		));
	}

	private Account findOwnedAccount(UUID accountId) {
		UUID currentUserId = currentUserProvider.requireCurrentUser().userId();
		return accountStore.findByIdAndUserId(accountId, currentUserId)
			.orElseThrow(() -> new ApiException(
				HttpStatus.NOT_FOUND,
				ApiErrorCode.RESOURCE_NOT_FOUND,
				ACCOUNT_NOT_FOUND_DETAIL
			));
	}

	private AccountView toView(Account account) {
		return new AccountView(
			account.id(),
			account.name(),
			account.type(),
			account.currency(),
			account.openingBalance(),
			account.archived(),
			account.createdAt(),
			account.updatedAt()
		);
	}

	private String normalizeName(String rawName) {
		return rawName == null ? null : rawName.trim().replaceAll("\\s+", " ");
	}

	private String normalizeCurrency(String rawCurrency) {
		if (rawCurrency == null) {
			return null;
		}
		String normalized = rawCurrency.trim().toUpperCase(Locale.ROOT);
		try {
			Currency.getInstance(normalized);
			return normalized;
		} catch (IllegalArgumentException exception) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, INVALID_CURRENCY_DETAIL);
		}
	}
}
