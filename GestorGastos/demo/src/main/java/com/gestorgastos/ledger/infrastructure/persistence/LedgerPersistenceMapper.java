package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.ledger.domain.model.Account;
import com.gestorgastos.ledger.infrastructure.persistence.entity.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class LedgerPersistenceMapper {

	public AccountEntity toEntity(Account account) {
		AccountEntity entity = new AccountEntity();
		entity.setId(account.id());
		entity.setUserId(account.userId());
		entity.setName(account.name());
		entity.setType(account.type());
		entity.setCurrency(account.currency());
		entity.setOpeningBalance(account.openingBalance());
		entity.setArchived(account.archived());
		entity.setCreatedAt(account.createdAt());
		entity.setUpdatedAt(account.updatedAt());
		return entity;
	}

	public Account toDomain(AccountEntity entity) {
		return new Account(
			entity.getId(),
			entity.getUserId(),
			entity.getName(),
			entity.getType(),
			entity.getCurrency(),
			entity.getOpeningBalance(),
			entity.isArchived(),
			entity.getCreatedAt(),
			entity.getUpdatedAt()
		);
	}
}
