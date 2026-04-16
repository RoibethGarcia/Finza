package com.gestorgastos.ledger.infrastructure.persistence.entity;

import com.gestorgastos.ledger.domain.model.CategoryType;
import com.gestorgastos.shared.infrastructure.persistence.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class CategoryEntity extends AbstractAuditableEntity {

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(nullable = false, length = 120)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CategoryType type;

	@Column(name = "is_archived", nullable = false)
	private boolean archived;
}
