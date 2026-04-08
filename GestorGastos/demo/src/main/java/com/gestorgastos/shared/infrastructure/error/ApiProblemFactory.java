package com.gestorgastos.shared.infrastructure.error;

import com.gestorgastos.shared.infrastructure.web.TraceIdAccessor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;

@Component
public class ApiProblemFactory {

	private final Clock clock;

	public ApiProblemFactory(Clock clock) {
		this.clock = clock;
	}

	public ProblemDetail create(HttpStatus status, ApiErrorCode errorCode, String detail, HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(status.getReasonPhrase());
		problemDetail.setType(URI.create("https://gestorgastos.dev/problems/" + errorCode.code().toLowerCase()));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("code", errorCode.code());
		problemDetail.setProperty("traceId", TraceIdAccessor.getTraceId(request));
		problemDetail.setProperty("timestamp", Instant.now(clock));
		return problemDetail;
	}
}
