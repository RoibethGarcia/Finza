package com.gestorgastos.ledger.api;

import com.gestorgastos.identity.application.security.TokenService;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.identity.infrastructure.config.SecurityConfiguration;
import com.gestorgastos.identity.infrastructure.security.JwtAuthenticationFilter;
import com.gestorgastos.ledger.application.service.AccountManagementService;
import com.gestorgastos.ledger.application.service.AccountView;
import com.gestorgastos.ledger.domain.model.AccountType;
import com.gestorgastos.shared.infrastructure.config.AppCorsProperties;
import com.gestorgastos.shared.infrastructure.config.AppObservabilityProperties;
import com.gestorgastos.shared.infrastructure.error.ApiProblemFactory;
import com.gestorgastos.shared.infrastructure.error.GlobalExceptionHandler;
import com.gestorgastos.shared.infrastructure.web.RestAccessDeniedHandler;
import com.gestorgastos.shared.infrastructure.web.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({
	SecurityConfiguration.class,
	JwtAuthenticationFilter.class,
	RestAuthenticationEntryPoint.class,
	RestAccessDeniedHandler.class,
	ApiProblemFactory.class,
	GlobalExceptionHandler.class,
	AccountControllerWebMvcTest.TestConfiguration.class
})
@TestPropertySource(properties = "app.cors.allowed-origins[0]=http://localhost:4173")
class AccountControllerWebMvcTest {

	@MockitoBean
	private AccountManagementService accountManagementService;

	@MockitoBean
	private TokenService tokenService;

	@org.springframework.beans.factory.annotation.Autowired
	private MockMvc mockMvc;

	@Test
	void shouldRejectListWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/accounts"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH-001"));
	}

	@Test
	void shouldCreateAccountWhenAuthenticated() throws Exception {
		UUID accountId = UUID.randomUUID();
		when(accountManagementService.create(any())).thenReturn(accountView(accountId, false));

		mockMvc.perform(post("/api/v1/accounts")
				.with(authenticatedUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "Cuenta principal",
					  "type": "BANK",
					  "currency": "UYU",
					  "openingBalance": 1500.00
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(accountId.toString()))
			.andExpect(jsonPath("$.name").value("Cuenta principal"))
			.andExpect(jsonPath("$.currency").value("UYU"))
			.andExpect(jsonPath("$.archived").value(false));
	}

	@Test
	void shouldListAccountsForAuthenticatedUser() throws Exception {
		when(accountManagementService.findAll(false)).thenReturn(List.of(
			accountView(UUID.randomUUID(), false),
			accountView(UUID.randomUUID(), false)
		));

		mockMvc.perform(get("/api/v1/accounts").with(authenticatedUser()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));

		verify(accountManagementService).findAll(false);
	}

	@Test
	void shouldUpdateOwnedAccount() throws Exception {
		UUID accountId = UUID.randomUUID();
		when(accountManagementService.update(eq(accountId), any())).thenReturn(new AccountView(
			accountId,
			"Caja USD",
			AccountType.CASH,
			"USD",
			new BigDecimal("99.90"),
			false,
			Instant.parse("2026-04-09T12:00:00Z"),
			Instant.parse("2026-04-09T12:05:00Z")
		));

		mockMvc.perform(put("/api/v1/accounts/{accountId}", accountId)
				.with(authenticatedUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "Caja USD",
					  "type": "CASH",
					  "currency": "USD",
					  "openingBalance": 99.90
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(accountId.toString()))
			.andExpect(jsonPath("$.name").value("Caja USD"))
			.andExpect(jsonPath("$.type").value("CASH"))
			.andExpect(jsonPath("$.currency").value("USD"));
	}

	@Test
	void shouldArchiveOwnedAccount() throws Exception {
		UUID accountId = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/accounts/{accountId}", accountId).with(authenticatedUser()))
			.andExpect(status().isNoContent());

		verify(accountManagementService).archive(accountId);
	}

	private AccountView accountView(UUID accountId, boolean archived) {
		return new AccountView(
			accountId,
			"Cuenta principal",
			AccountType.BANK,
			"UYU",
			new BigDecimal("1500.00"),
			archived,
			Instant.parse("2026-04-09T12:00:00Z"),
			Instant.parse("2026-04-09T12:00:00Z")
		);
	}

	private org.springframework.test.web.servlet.request.RequestPostProcessor authenticatedUser() {
		UUID userId = UUID.randomUUID();
		return authentication(new UsernamePasswordAuthenticationToken(
			new AuthenticatedUser(userId, "ada@example.com", Set.of("USER")),
			"n/a",
			Set.of(new SimpleGrantedAuthority("ROLE_USER"))
		));
	}

	static class TestConfiguration {

		@Bean
		Clock clock() {
			return Clock.fixed(Instant.parse("2026-04-09T12:00:00Z"), ZoneOffset.UTC);
		}

		@Bean
		AppCorsProperties appCorsProperties() {
			AppCorsProperties properties = new AppCorsProperties();
			properties.getAllowedOrigins().add("http://localhost:4173");
			return properties;
		}

		@Bean
		AppObservabilityProperties appObservabilityProperties() {
			return new AppObservabilityProperties();
		}
	}
}
