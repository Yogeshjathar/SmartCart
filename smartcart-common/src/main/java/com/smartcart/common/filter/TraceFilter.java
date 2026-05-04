package com.smartcart.common.filter;

import com.smartcart.common.util.TraceUtil;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TraceFilter extends OncePerRequestFilter {
    private final Tracer tracer;

    public TraceFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain)
            throws IOException, ServletException {
        String correlationId = TraceUtil.resolveCorrelationId(
                httpRequest.getHeader(TraceUtil.CORRELATION_HEADER)
        );

        TraceUtil.setCorrelationId(correlationId);
        TraceUtil.setTraceId(httpRequest.getHeader(TraceUtil.TRACE_HEADER));
        TraceUtil.setSpanId(httpRequest.getHeader(TraceUtil.SPAN_HEADER));

        try {
            chain.doFilter(httpRequest, httpResponse);
        } finally {
            TraceUtil.syncFromTracer(tracer);
            httpResponse.setHeader(TraceUtil.CORRELATION_HEADER, correlationId);
            httpResponse.setHeader(TraceUtil.TRACE_HEADER, TraceUtil.currentTraceId(tracer));
            httpResponse.setHeader(TraceUtil.SPAN_HEADER, TraceUtil.currentSpanId(tracer));
            TraceUtil.clear();
        }
    }
}
