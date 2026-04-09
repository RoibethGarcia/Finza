package com.gestorgastos.ledger.application.port;

import com.gestorgastos.ledger.domain.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountStore {

	Account save(Account account);

	Optional<Account> findByIdAndUserId(UUID accountId, UUID userId);

	List<Account> findAllByUserId(UUID userId);
}
