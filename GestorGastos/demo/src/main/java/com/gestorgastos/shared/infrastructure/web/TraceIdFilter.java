package com.gestorgastos.shared.infrastructure.web;

import com.gestorgastos.shared.infrastructure.config.AppObservabilityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class TraceIdFilter extends OncePerRequestFilter {

	private final AppObservabilityProperties properties;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String traceHeaderName = properties.getTraceHeaderName();
		String traceId = Optional.ofNullable(request.getHeader(traceHeaderName))
			.filter(value -> !value.isBlank())
			.orElseGet(() -> UUID.randomUUID().toString());

		request.setAttribute(TraceIdAccessor.TRACE_ID_ATTRIBUTE, traceId);
		response.setHeader(traceHeaderName, traceId);
		MDC.put("traceId", traceId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove("traceId");
		}
	}
}
