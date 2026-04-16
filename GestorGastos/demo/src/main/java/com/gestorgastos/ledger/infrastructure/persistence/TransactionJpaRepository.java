package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.ledger.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

	Optional<TransactionEntity> findByIdAndUserId(UUID transactionId, UUID userId);

	List<TransactionEntity> findAllByUserIdOrderByOccurredAtDescCreatedAtDesc(UUID userId);
}
