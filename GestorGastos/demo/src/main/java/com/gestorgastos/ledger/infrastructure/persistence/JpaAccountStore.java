package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.ledger.application.port.AccountStore;
import com.gestorgastos.ledger.domain.model.Account;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaAccountStore implements AccountStore {

	private final AccountJpaRepository accountJpaRepository;
	private final LedgerPersistenceMapper mapper;

	public JpaAccountStore(AccountJpaRepository accountJpaRepository, LedgerPersistenceMapper mapper) {
		this.accountJpaRepository = accountJpaRepository;
		this.mapper = mapper;
	}

	@Override
	public Account save(Account account) {
		return mapper.toDomain(accountJpaRepository.save(mapper.toEntity(account)));
	}

	@Override
	public Optional<Account> findByIdAndUserId(UUID accountId, UUID userId) {
		return accountJpaRepository.findByIdAndUserId(accountId, userId).map(mapper::toDomain);
	}

	@Override
	public List<Account> findAllByUserId(UUID userId) {
		return accountJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
			.map(mapper::toDomain)
			.toList();
	}
}
