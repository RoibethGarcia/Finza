package com.gestorgastos.identity.api;

import com.gestorgastos.identity.application.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalProbeController {

	private final CurrentUserProvider currentUserProvider;

	@GetMapping("/probe")
	public Map<String, Object> probe() {
		var currentUser = currentUserProvider.requireCurrentUser();
		return Map.of(
			"status", "ok",
			"userId", currentUser.userId(),
			"email", currentUser.email()
		);
	}
}
