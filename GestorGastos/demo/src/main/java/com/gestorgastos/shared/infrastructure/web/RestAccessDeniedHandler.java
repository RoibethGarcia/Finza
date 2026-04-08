package com.gestorgastos.shared.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestorgastos.shared.infrastructure.error.ApiErrorCode;
import com.gestorgastos.shared.infrastructure.error.ApiProblemFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;
	private final ApiProblemFactory apiProblemFactory;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
			throws IOException {
		response.setStatus(ApiErrorCode.ACCESS_DENIED.httpStatus().value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), apiProblemFactory.create(
			ApiErrorCode.ACCESS_DENIED.httpStatus(),
			ApiErrorCode.ACCESS_DENIED,
			ApiErrorCode.ACCESS_DENIED.defaultDetail(),
			request
		));
	}
}
