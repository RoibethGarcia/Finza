package com.gestorgastos.identity.application.service;

import com.gestorgastos.identity.domain.model.UserStatus;

import java.time.LocalDate;
import java.util.UUID;

public record UserProfile(
	UUID id,
	String fullName,
	String email,
	LocalDate birthDate,
	UserStatus status
) {
}
