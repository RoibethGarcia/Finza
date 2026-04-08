package com.gestorgastos.shared.infrastructure.web;

import com.gestorgastos.identity.application.security.TokenService;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TokenService tokenService;

	@Test
	void shouldAllowActuatorHealthWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk())
			.andExpect(header().exists("X-Trace-Id"));
	}

	@Test
	void shouldRejectProtectedEndpointWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/internal/probe"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH-001"));
	}

	@Test
	void shouldAllowProtectedEndpointWithValidBearerToken() throws Exception {
		UUID userId = UUID.randomUUID();
		String token = tokenService.issueAccessToken(new AuthenticatedUser(userId, "tester@example.com", Set.of("USER"))).value();

		mockMvc.perform(get("/api/v1/internal/probe")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value(userId.toString()))
			.andExpect(jsonPath("$.email").value("tester@example.com"));
	}
}
