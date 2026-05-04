package com.smartcart.common.util;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;

import java.util.UUID;

public final class TraceUtil {

    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String SPAN_HEADER = "X-Span-Id";
    public static final String CORRELATION_HEADER = "X-Correlation-Id";
    public static final String AGGREGATE_ID_HEADER = "X-Aggregate-Id";
    public static final String AGGREGATE_TYPE_HEADER = "X-Aggregate-Type";
    public static final String EVENT_ID_HEADER = "X-Event-Id";
    public static final String EVENT_TYPE_HEADER = "X-Event-Type";
    public static final String SOURCE_HEADER = "X-Source-Service";

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String CORRELATION_ID = "correlationId";
    public static final String AGGREGATE_ID = "aggregateId";
    public static final String AGGREGATE_TYPE = "aggregateType";
    public static final String EVENT_ID = "eventId";
    public static final String EVENT_TYPE = "eventType";
    public static final String SOURCE = "source";

    private TraceUtil() {}

    public static String getTraceId() {
        return getOrDefault(TRACE_ID);
    }

    public static String getSpanId() {
        return getOrDefault(SPAN_ID);
    }

    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID);
    }

    public static String getAggregateId() {
        return MDC.get(AGGREGATE_ID);
    }

    public static String getEventId() {
        return MDC.get(EVENT_ID);
    }

    public static void setTraceId(String traceId) {
        putIfPresent(TRACE_ID, traceId);
    }

    public static void setSpanId(String spanId) {
        putIfPresent(SPAN_ID, spanId);
    }

    public static void setCorrelationId(String correlationId) {
        putIfPresent(CORRELATION_ID, correlationId);
    }

    public static void setAggregateContext(String aggregateId, String aggregateType) {
        putIfPresent(AGGREGATE_ID, aggregateId);
        putIfPresent(AGGREGATE_TYPE, aggregateType);
    }

    public static void setEventContext(String eventId, String eventType, String source) {
        putIfPresent(EVENT_ID, eventId);
        putIfPresent(EVENT_TYPE, eventType);
        putIfPresent(SOURCE, source);
    }

    public static void syncFromTracer(Tracer tracer) {
        if (tracer == null) {
            return;
        }

        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return;
        }

        setTraceId(currentSpan.context().traceId());
        setSpanId(currentSpan.context().spanId());
    }

    public static String currentTraceId(Tracer tracer) {
        Span currentSpan = tracer != null ? tracer.currentSpan() : null;
        if (currentSpan != null && currentSpan.context() != null) {
            return currentSpan.context().traceId();
        }
        return MDC.get(TRACE_ID);
    }

    public static String currentSpanId(Tracer tracer) {
        Span currentSpan = tracer != null ? tracer.currentSpan() : null;
        if (currentSpan != null && currentSpan.context() != null) {
            return currentSpan.context().spanId();
        }
        return MDC.get(SPAN_ID);
    }

    public static String resolveCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            return correlationId;
        }

        String fromMdc = MDC.get(CORRELATION_ID);
        if (fromMdc != null && !fromMdc.isBlank()) {
            return fromMdc;
        }

        return UUID.randomUUID().toString();
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    public static void clear() {
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
        MDC.remove(CORRELATION_ID);
        MDC.remove(AGGREGATE_ID);
        MDC.remove(AGGREGATE_TYPE);
        MDC.remove(EVENT_ID);
        MDC.remove(EVENT_TYPE);
        MDC.remove(SOURCE);
    }

    private static String getOrDefault(String key) {
        String value = MDC.get(key);
        return value != null ? value : "N/A";
    }

    private static void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }
}
