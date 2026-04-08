package com.gestorgastos.identity.application.port;

import com.gestorgastos.identity.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserStore {

	Optional<User> findById(UUID userId);
	Optional<User> findByEmail(String email);
	User save(User user);
}
