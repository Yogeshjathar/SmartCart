package com.smartcart.common.filter;

import com.smartcart.common.util.TraceUtil;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import java.io.IOException;
import java.util.UUID;

public class TraceFilter implements Filter {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private final Tracer tracer;

    public TraceFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // TraceId
        String traceId = httpRequest.getHeader(TraceUtil.TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceUtil.generateTraceId();
        }
        TraceUtil.setTraceId(traceId);

        // CorrelationId
        String correlationId = httpRequest.getHeader(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", correlationId);

        // Create a root OT span for this HTTP request
        Span httpSpan = tracer.nextSpan().name(httpRequest.getMethod() + " " + httpRequest.getRequestURI()).start();
        try (Tracer.SpanInScope ws = tracer.withSpan(httpSpan)) {

            // Add headers to response
            httpResponse.setHeader(TraceUtil.TRACE_HEADER, traceId);
            httpResponse.setHeader(CORRELATION_HEADER, correlationId);

            chain.doFilter(request, response);
        } finally {
            httpSpan.end();
            TraceUtil.clear();
            MDC.remove("correlationId");
        }
    }
}