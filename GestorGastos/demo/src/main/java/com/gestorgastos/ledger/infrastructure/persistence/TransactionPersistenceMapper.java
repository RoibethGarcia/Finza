package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.ledger.domain.model.LedgerTransaction;
import com.gestorgastos.ledger.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionPersistenceMapper {

	public TransactionEntity toEntity(LedgerTransaction transaction) {
		TransactionEntity entity = new TransactionEntity();
		entity.setId(transaction.id());
		entity.setUserId(transaction.userId());
		entity.setAccountId(transaction.accountId());
		entity.setCategoryId(transaction.categoryId());
		entity.setType(transaction.type());
		entity.setAmount(transaction.amount());
		entity.setCurrency(transaction.currency());
		entity.setOccurredAt(transaction.occurredAt());
		entity.setDescription(transaction.description());
		entity.setReferenceType(transaction.referenceType());
		entity.setReferenceId(transaction.referenceId());
		entity.setTransferGroupId(transaction.transferGroupId());
		entity.setCreatedAt(transaction.createdAt());
		entity.setUpdatedAt(transaction.updatedAt());
		return entity;
	}

	public LedgerTransaction toDomain(TransactionEntity entity) {
		return new LedgerTransaction(
			entity.getId(),
			entity.getUserId(),
			entity.getAccountId(),
			entity.getCategoryId(),
			entity.getType(),
			entity.getAmount(),
			entity.getCurrency(),
			entity.getOccurredAt(),
			entity.getDescription(),
			entity.getReferenceType(),
			entity.getReferenceId(),
			entity.getTransferGroupId(),
			entity.getCreatedAt(),
			entity.getUpdatedAt()
		);
	}
}
