package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.ledger.application.port.TransactionStore;
import com.gestorgastos.ledger.domain.model.LedgerTransaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaTransactionStore implements TransactionStore {

	private final TransactionJpaRepository transactionJpaRepository;
	private final TransactionPersistenceMapper mapper;

	public JpaTransactionStore(TransactionJpaRepository transactionJpaRepository, TransactionPersistenceMapper mapper) {
		this.transactionJpaRepository = transactionJpaRepository;
		this.mapper = mapper;
	}

	@Override
	public LedgerTransaction save(LedgerTransaction transaction) {
		return mapper.toDomain(transactionJpaRepository.save(mapper.toEntity(transaction)));
	}

	@Override
	public List<LedgerTransaction> saveAll(List<LedgerTransaction> transactions) {
		return transactionJpaRepository.saveAll(transactions.stream().map(mapper::toEntity).toList()).stream()
			.map(mapper::toDomain)
			.toList();
	}

	@Override
	public Optional<LedgerTransaction> findByIdAndUserId(UUID transactionId, UUID userId) {
		return transactionJpaRepository.findByIdAndUserId(transactionId, userId).map(mapper::toDomain);
	}

	@Override
	public List<LedgerTransaction> findAllByUserId(UUID userId) {
		return transactionJpaRepository.findAllByUserIdOrderByOccurredAtDescCreatedAtDesc(userId).stream()
			.map(mapper::toDomain)
			.toList();
	}
}
