package com.gestorgastos.identity.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record User(
	UUID id,
	String fullName,
	String email,
	LocalDate birthDate,
	String passwordHash,
	UserStatus status,
	Instant createdAt,
	Instant updatedAt
) {
}
