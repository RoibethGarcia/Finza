package com.gestorgastos.identity.infrastructure.security;

import com.gestorgastos.identity.application.security.TokenService;
import com.gestorgastos.identity.domain.exception.InvalidAccessTokenException;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.shared.infrastructure.error.ApiErrorCode;
import com.gestorgastos.shared.infrastructure.web.RestAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final TokenService tokenService;
	private final RestAuthenticationEntryPoint authenticationEntryPoint;

	public JwtAuthenticationFilter(TokenService tokenService, RestAuthenticationEntryPoint authenticationEntryPoint) {
		this.tokenService = tokenService;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorizationHeader.substring(7);
		try {
			AuthenticatedUser authenticatedUser = tokenService.parseAccessToken(token);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				authenticatedUser,
				token,
				authenticatedUser.roles().stream()
					.map(this::toAuthority)
					.collect(Collectors.toSet())
			);
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} catch (InvalidAccessTokenException exception) {
			SecurityContextHolder.clearContext();
			request.setAttribute(RestAuthenticationEntryPoint.AUTH_ERROR_CODE_ATTRIBUTE, ApiErrorCode.INVALID_ACCESS_TOKEN);
			authenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid access token.", exception));
		}
	}

	private SimpleGrantedAuthority toAuthority(String role) {
		String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
		return new SimpleGrantedAuthority(normalizedRole);
	}
}
