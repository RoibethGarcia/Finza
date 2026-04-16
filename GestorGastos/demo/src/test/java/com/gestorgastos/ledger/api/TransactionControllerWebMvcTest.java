package com.gestorgastos.ledger.api;

import com.gestorgastos.identity.application.security.TokenService;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.identity.infrastructure.config.SecurityConfiguration;
import com.gestorgastos.identity.infrastructure.security.JwtAuthenticationFilter;
import com.gestorgastos.ledger.application.service.TransactionManagementService;
import com.gestorgastos.ledger.application.service.TransactionView;
import com.gestorgastos.ledger.application.service.TransferView;
import com.gestorgastos.ledger.domain.model.TransactionType;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import({
	SecurityConfiguration.class,
	JwtAuthenticationFilter.class,
	RestAuthenticationEntryPoint.class,
	RestAccessDeniedHandler.class,
	ApiProblemFactory.class,
	GlobalExceptionHandler.class,
	TransactionControllerWebMvcTest.TestConfiguration.class
})
@TestPropertySource(properties = "app.cors.allowed-origins[0]=http://localhost:4173")
class TransactionControllerWebMvcTest {

	@MockitoBean
	private TransactionManagementService transactionManagementService;

	@MockitoBean
	private TokenService tokenService;

	@org.springframework.beans.factory.annotation.Autowired
	private MockMvc mockMvc;

	@Test
	void shouldCreateTransactionWhenAuthenticated() throws Exception {
		UUID transactionId = UUID.randomUUID();
		UUID accountId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		when(transactionManagementService.create(any())).thenReturn(transactionView(
			transactionId,
			accountId,
			"Cuenta principal",
			categoryId,
			"Comida",
			TransactionType.EXPENSE,
			new BigDecimal("150.00"),
			"UYU",
			null
		));

		mockMvc.perform(post("/api/v1/transactions")
				.with(authenticatedUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "accountId": "%s",
					  "categoryId": "%s",
					  "type": "EXPENSE",
					  "amount": 150.00,
					  "occurredAt": "2026-04-10T12:00:00Z",
					  "description": "Compra supermercado"
					}
					""".formatted(accountId, categoryId)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(transactionId.toString()))
			.andExpect(jsonPath("$.categoryName").value("Comida"));
	}

	@Test
	void shouldCreateTransferWhenAuthenticated() throws Exception {
		UUID transferGroupId = UUID.randomUUID();
		UUID sourceAccountId = UUID.randomUUID();
		UUID targetAccountId = UUID.randomUUID();
		when(transactionManagementService.transfer(any())).thenReturn(new TransferView(
			transferGroupId,
			transactionView(UUID.randomUUID(), sourceAccountId, "Banco", null, null, TransactionType.TRANSFER_OUT, new BigDecimal("300.00"), "UYU", transferGroupId),
			transactionView(UUID.randomUUID(), targetAccountId, "Caja", null, null, TransactionType.TRANSFER_IN, new BigDecimal("300.00"), "UYU", transferGroupId)
		));

		mockMvc.perform(post("/api/v1/transactions/transfers")
				.with(authenticatedUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "sourceAccountId": "%s",
					  "targetAccountId": "%s",
					  "amount": 300.00,
					  "occurredAt": "2026-04-10T12:00:00Z",
					  "description": "Movimiento interno"
					}
					""".formatted(sourceAccountId, targetAccountId)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.transferGroupId").value(transferGroupId.toString()))
			.andExpect(jsonPath("$.outgoingTransaction.type").value("TRANSFER_OUT"))
			.andExpect(jsonPath("$.incomingTransaction.type").value("TRANSFER_IN"));
	}

	@Test
	void shouldListTransactionsWhenAuthenticated() throws Exception {
		when(transactionManagementService.findAll(any())).thenReturn(List.of(
			transactionView(UUID.randomUUID(), UUID.randomUUID(), "Banco", UUID.randomUUID(), "Salario", TransactionType.INCOME, new BigDecimal("5000.00"), "UYU", null),
			transactionView(UUID.randomUUID(), UUID.randomUUID(), "Caja", UUID.randomUUID(), "Comida", TransactionType.EXPENSE, new BigDecimal("200.00"), "UYU", null)
		));

		mockMvc.perform(get("/api/v1/transactions").with(authenticatedUser()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));
	}

	private TransactionView transactionView(
			UUID transactionId,
			UUID accountId,
			String accountName,
			UUID categoryId,
			String categoryName,
			TransactionType type,
			BigDecimal amount,
			String currency,
			UUID transferGroupId
	) {
		return new TransactionView(
			transactionId,
			accountId,
			accountName,
			categoryId,
			categoryName,
			type,
			amount,
			currency,
			Instant.parse("2026-04-10T12:00:00Z"),
			"Detalle",
			transferGroupId,
			null,
			null,
			Instant.parse("2026-04-10T12:00:00Z"),
			Instant.parse("2026-04-10T12:00:00Z")
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
			return Clock.fixed(Instant.parse("2026-04-10T10:00:00Z"), ZoneOffset.UTC);
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
