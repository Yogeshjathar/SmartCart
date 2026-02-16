package com.smartcart.common.util;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceUtil {

    private static final String TRACE_ID = "traceId";

    private TraceUtil() {}

    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID);
        if (traceId == null) {
            traceId = generateTraceId();
            MDC.put(TRACE_ID, traceId);
        }
        return traceId;
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
