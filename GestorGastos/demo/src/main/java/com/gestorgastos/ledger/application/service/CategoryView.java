package com.gestorgastos.ledger.application.service;

import com.gestorgastos.ledger.domain.model.CategoryType;

import java.time.Instant;
import java.util.UUID;

public record CategoryView(
	UUID id,
	String name,
	CategoryType type,
	boolean archived,
	Instant createdAt,
	Instant updatedAt
) {
}
