package com.gestorgastos.identity.infrastructure.persistence.entity;

import com.gestorgastos.identity.domain.model.UserStatus;
import com.gestorgastos.shared.infrastructure.persistence.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity extends AbstractAuditableEntity {

	@Column(name = "full_name", nullable = false, length = 150)
	private String fullName;

	@Column(nullable = false, length = 320, unique = true)
	private String email;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Column(name = "password_hash", nullable = false, length = 120)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserStatus status;
}
