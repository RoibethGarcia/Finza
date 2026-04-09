package com.gestorgastos.identity.api;

import com.gestorgastos.identity.application.service.AuthenticatedSession;
import com.gestorgastos.identity.application.service.IdentityAuthenticationService;
import com.gestorgastos.identity.application.service.LoginIdentityCommand;
import com.gestorgastos.identity.application.service.RegisterIdentityCommand;
import com.gestorgastos.identity.application.service.UserProfile;
import com.gestorgastos.identity.application.security.TokenService;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.identity.domain.model.UserStatus;
import com.gestorgastos.identity.infrastructure.config.SecurityConfiguration;
import com.gestorgastos.identity.infrastructure.security.JwtAuthenticationFilter;
import com.gestorgastos.shared.infrastructure.config.AppObservabilityProperties;
import com.gestorgastos.shared.infrastructure.config.AppCorsProperties;
import com.gestorgastos.shared.infrastructure.error.ApiProblemFactory;
import com.gestorgastos.shared.infrastructure.error.GlobalExceptionHandler;
import com.gestorgastos.shared.infrastructure.web.RestAccessDeniedHandler;
import com.gestorgastos.shared.infrastructure.web.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
	SecurityConfiguration.class,
	JwtAuthenticationFilter.class,
	RestAuthenticationEntryPoint.class,
	RestAccessDeniedHandler.class,
	ApiProblemFactory.class,
	GlobalExceptionHandler.class,
	AuthControllerWebMvcTest.TestConfiguration.class
})
@TestPropertySource(properties = "app.cors.allowed-origins[0]=http://localhost:4173")
class AuthControllerWebMvcTest {

	@MockitoBean
	private IdentityAuthenticationService authenticationService;

	@MockitoBean
	private TokenService tokenService;

	@org.springframework.beans.factory.annotation.Autowired
	private MockMvc mockMvc;

	@Test
	void shouldRegisterAndReturnSessionPayload() throws Exception {
		UUID userId = UUID.randomUUID();
		when(authenticationService.register(any(RegisterIdentityCommand.class))).thenReturn(new AuthenticatedSession(
			new UserProfile(userId, "Ada Lovelace", "ada@example.com", LocalDate.of(1815, 12, 10), UserStatus.ACTIVE),
			"access-token",
			Instant.parse("2026-04-09T12:15:00Z"),
			"refresh-token",
			Instant.parse("2026-04-16T12:00:00Z")
		));

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "fullName": "Ada Lovelace",
					  "email": "ada@example.com",
					  "birthDate": "1815-12-10",
					  "password": "super-secret"
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.accessToken").value("access-token"))
			.andExpect(jsonPath("$.refreshToken").value("refresh-token"))
			.andExpect(jsonPath("$.user.id").value(userId.toString()))
			.andExpect(jsonPath("$.user.email").value("ada@example.com"));
	}

	@Test
	void shouldLoginWithoutAuthentication() throws Exception {
		UUID userId = UUID.randomUUID();
		when(authenticationService.login(any(LoginIdentityCommand.class))).thenReturn(new AuthenticatedSession(
			new UserProfile(userId, "Ada Lovelace", "ada@example.com", null, UserStatus.ACTIVE),
			"access-token",
			Instant.parse("2026-04-09T12:15:00Z"),
			"refresh-token",
			Instant.parse("2026-04-16T12:00:00Z")
		));

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "ada@example.com",
					  "password": "super-secret"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value("access-token"));
	}

	@Test
	void shouldRejectMeWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/auth/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH-001"));
	}

	@Test
	void shouldReturnCurrentUserForAuthenticatedRequest() throws Exception {
		UUID userId = UUID.randomUUID();
		when(authenticationService.me()).thenReturn(new UserProfile(
			userId,
			"Ada Lovelace",
			"ada@example.com",
			LocalDate.of(1815, 12, 10),
			UserStatus.ACTIVE
		));

		mockMvc.perform(get("/api/v1/auth/me")
				.with(authentication(new UsernamePasswordAuthenticationToken(
					new AuthenticatedUser(userId, "ada@example.com", Set.of("USER")),
					"n/a",
					Set.of(new SimpleGrantedAuthority("ROLE_USER"))
				))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(userId.toString()))
			.andExpect(jsonPath("$.email").value("ada@example.com"))
			.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void shouldLogoutWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/v1/auth/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "refreshToken": "refresh-token"
					}
					"""))
			.andExpect(status().isNoContent());

		verify(authenticationService).logout(eq("refresh-token"));
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
