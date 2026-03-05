package com.smartcart.common.util;

import org.slf4j.MDC;
import java.util.UUID;

public final class TraceUtil {

    public static final String TRACE_HEADER = "X-Trace-Id";
    private static final String TRACE_ID = "traceId";

    private TraceUtil() {}

    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID);
        return traceId != null ? traceId : "N/A";
    }

    public static void setTraceId(String traceId) {
        if(traceId != null) MDC.put(TRACE_ID, traceId);
    }

    public static void clear() {
        MDC.remove(TRACE_ID);
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}