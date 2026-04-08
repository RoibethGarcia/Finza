package com.gestorgastos.shared.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.UUID;

public final class TraceIdAccessor {

	public static final String TRACE_ID_ATTRIBUTE = TraceIdAccessor.class.getName() + ".traceId";

	private TraceIdAccessor() {
	}

	public static String getTraceId(HttpServletRequest request) {
		return Optional.ofNullable(request.getAttribute(TRACE_ID_ATTRIBUTE))
			.map(Object::toString)
			.orElseGet(() -> UUID.randomUUID().toString());
	}
}
