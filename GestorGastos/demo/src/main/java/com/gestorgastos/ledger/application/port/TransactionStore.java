package com.gestorgastos.ledger.application.port;

import com.gestorgastos.ledger.domain.model.LedgerTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionStore {

	LedgerTransaction save(LedgerTransaction transaction);

	List<LedgerTransaction> saveAll(List<LedgerTransaction> transactions);

	Optional<LedgerTransaction> findByIdAndUserId(UUID transactionId, UUID userId);

	List<LedgerTransaction> findAllByUserId(UUID userId);
}
