package com.gestorgastos.ledger.infrastructure.persistence.entity;

import com.gestorgastos.ledger.domain.model.AccountType;
import com.gestorgastos.shared.infrastructure.persistence.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class AccountEntity extends AbstractAuditableEntity {

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(nullable = false, length = 120)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AccountType type;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "opening_balance", nullable = false, precision = 19, scale = 2)
	private BigDecimal openingBalance;

	@Column(name = "is_archived", nullable = false)
	private boolean archived;
}
