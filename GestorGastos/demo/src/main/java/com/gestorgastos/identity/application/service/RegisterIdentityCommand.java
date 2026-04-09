package com.gestorgastos.identity.application.service;

import java.time.LocalDate;

public record RegisterIdentityCommand(
	String fullName,
	String email,
	LocalDate birthDate,
	String password
) {
}
