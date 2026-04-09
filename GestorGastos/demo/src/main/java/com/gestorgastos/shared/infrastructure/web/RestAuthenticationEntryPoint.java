package com.gestorgastos.shared.infrastructure.web;

import com.gestorgastos.shared.infrastructure.error.ApiErrorCode;
import com.gestorgastos.shared.infrastructure.error.ApiProblemFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	public static final String AUTH_ERROR_CODE_ATTRIBUTE = RestAuthenticationEntryPoint.class.getName() + ".errorCode";

	private final ObjectMapper objectMapper;
	private final ApiProblemFactory apiProblemFactory;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
			throws IOException {
		ApiErrorCode code = request.getAttribute(AUTH_ERROR_CODE_ATTRIBUTE) instanceof ApiErrorCode apiErrorCode
			? apiErrorCode
			: ApiErrorCode.AUTHENTICATION_REQUIRED;

		response.setStatus(code.httpStatus().value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), apiProblemFactory.create(code.httpStatus(), code, code.defaultDetail(), request));
	}
}
