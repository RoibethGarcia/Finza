package com.gestorgastos.ledger.api;

import com.gestorgastos.identity.application.security.TokenService;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.identity.infrastructure.config.SecurityConfiguration;
import com.gestorgastos.identity.infrastructure.security.JwtAuthenticationFilter;
import com.gestorgastos.ledger.application.service.CategoryManagementService;
import com.gestorgastos.ledger.application.service.CategoryView;
import com.gestorgastos.ledger.domain.model.CategoryType;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import({
	SecurityConfiguration.class,
	JwtAuthenticationFilter.class,
	RestAuthenticationEntryPoint.class,
	RestAccessDeniedHandler.class,
	ApiProblemFactory.class,
	GlobalExceptionHandler.class,
	CategoryControllerWebMvcTest.TestConfiguration.class
})
@TestPropertySource(properties = "app.cors.allowed-origins[0]=http://localhost:4173")
class CategoryControllerWebMvcTest {

	@MockitoBean
	private CategoryManagementService categoryManagementService;

	@MockitoBean
	private TokenService tokenService;

	@org.springframework.beans.factory.annotation.Autowired
	private MockMvc mockMvc;

	@Test
	void shouldCreateCategoryWhenAuthenticated() throws Exception {
		UUID categoryId = UUID.randomUUID();
		when(categoryManagementService.create(any())).thenReturn(categoryView(categoryId, "Salario", CategoryType.INCOME, false));

		mockMvc.perform(post("/api/v1/categories")
				.with(authenticatedUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "Salario",
					  "type": "INCOME"
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(categoryId.toString()))
			.andExpect(jsonPath("$.type").value("INCOME"));
	}

	@Test
	void shouldListCategoriesWhenAuthenticated() throws Exception {
		when(categoryManagementService.findAll(false)).thenReturn(List.of(
			categoryView(UUID.randomUUID(), "Salario", CategoryType.INCOME, false),
			categoryView(UUID.randomUUID(), "Comida", CategoryType.EXPENSE, false)
		));

		mockMvc.perform(get("/api/v1/categories").with(authenticatedUser()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void shouldArchiveCategoryWhenAuthenticated() throws Exception {
		UUID categoryId = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/categories/{categoryId}", categoryId).with(authenticatedUser()))
			.andExpect(status().isNoContent());

		verify(categoryManagementService).archive(categoryId);
	}

	private CategoryView categoryView(UUID categoryId, String name, CategoryType type, boolean archived) {
		return new CategoryView(
			categoryId,
			name,
			type,
			archived,
			Instant.parse("2026-04-10T10:00:00Z"),
			Instant.parse("2026-04-10T10:00:00Z")
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
