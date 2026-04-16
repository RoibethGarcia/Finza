package com.gestorgastos.reporting.api;

import com.gestorgastos.identity.application.security.TokenService;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.identity.infrastructure.config.SecurityConfiguration;
import com.gestorgastos.identity.infrastructure.security.JwtAuthenticationFilter;
import com.gestorgastos.reporting.application.service.AccountBalanceView;
import com.gestorgastos.reporting.application.service.CurrencySummaryView;
import com.gestorgastos.reporting.application.service.DashboardSummaryService;
import com.gestorgastos.reporting.application.service.DashboardSummaryView;
import com.gestorgastos.reporting.application.service.RecentTransactionView;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({DashboardController.class, BalanceReportController.class})
@Import({
	SecurityConfiguration.class,
	JwtAuthenticationFilter.class,
	RestAuthenticationEntryPoint.class,
	RestAccessDeniedHandler.class,
	ApiProblemFactory.class,
	GlobalExceptionHandler.class,
	DashboardControllerWebMvcTest.TestConfiguration.class
})
@TestPropertySource(properties = "app.cors.allowed-origins[0]=http://localhost:4173")
class DashboardControllerWebMvcTest {

	@MockitoBean
	private DashboardSummaryService dashboardSummaryService;

	@MockitoBean
	private TokenService tokenService;

	@org.springframework.beans.factory.annotation.Autowired
	private MockMvc mockMvc;

	@Test
	void shouldReturnDashboardSummaryWhenAuthenticated() throws Exception {
		when(dashboardSummaryService.getSummary(eq(LocalDate.of(2026, 4, 1)), eq(LocalDate.of(2026, 4, 30))))
			.thenReturn(summaryView());

		mockMvc.perform(get("/api/v1/dashboard?from=2026-04-01&to=2026-04-30").with(authenticatedUser()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.currencies[0].currency").value("UYU"))
			.andExpect(jsonPath("$.accounts[0].currentBalance").value(5300.00))
			.andExpect(jsonPath("$.recentTransactions.length()").value(1));
	}

	@Test
	void shouldReturnBalanceReportWhenAuthenticated() throws Exception {
		when(dashboardSummaryService.getBalanceReport(eq(LocalDate.of(2026, 4, 1)), eq(LocalDate.of(2026, 4, 30))))
			.thenReturn(new com.gestorgastos.reporting.application.service.BalanceReportView(
				LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 30),
				summaryView().currencies(),
				summaryView().accounts()
			));

		mockMvc.perform(get("/api/v1/reports/balance?from=2026-04-01&to=2026-04-30").with(authenticatedUser()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.currencies[0].netAmount").value(1200.00))
			.andExpect(jsonPath("$.accounts[0].periodExpense").value(300.00));
	}

	private DashboardSummaryView summaryView() {
		return new DashboardSummaryView(
			LocalDate.of(2026, 4, 1),
			LocalDate.of(2026, 4, 30),
			List.of(new CurrencySummaryView("UYU", new BigDecimal("1500.00"), new BigDecimal("300.00"), new BigDecimal("1200.00"), new BigDecimal("5300.00"))),
			List.of(new AccountBalanceView(
				UUID.randomUUID(),
				"Cuenta principal",
				"UYU",
				new BigDecimal("4100.00"),
				new BigDecimal("5300.00"),
				new BigDecimal("1500.00"),
				new BigDecimal("300.00"),
				new BigDecimal("1200.00")
			)),
			List.of(new RecentTransactionView(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"Cuenta principal",
				UUID.randomUUID(),
				"Salario",
				TransactionType.INCOME,
				new BigDecimal("1500.00"),
				"UYU",
				Instant.parse("2026-04-10T12:00:00Z"),
				"Ingreso mensual"
			))
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
