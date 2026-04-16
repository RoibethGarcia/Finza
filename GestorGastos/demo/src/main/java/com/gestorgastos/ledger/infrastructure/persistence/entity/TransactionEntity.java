package com.gestorgastos.ledger.infrastructure.persistence.entity;

import com.gestorgastos.ledger.domain.model.TransactionType;
import com.gestorgastos.shared.infrastructure.persistence.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class TransactionEntity extends AbstractAuditableEntity {

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "account_id", nullable = false)
	private UUID accountId;

	@Column(name = "category_id")
	private UUID categoryId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TransactionType type;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(length = 255)
	private String description;

	@Column(name = "reference_type", length = 100)
	private String referenceType;

	@Column(name = "reference_id")
	private UUID referenceId;

	@Column(name = "transfer_group_id")
	private UUID transferGroupId;
}
