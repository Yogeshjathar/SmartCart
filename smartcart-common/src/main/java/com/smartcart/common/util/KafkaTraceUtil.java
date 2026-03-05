package com.smartcart.common.util;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import java.nio.charset.StandardCharsets;

public final class KafkaTraceUtil {

    public static <K,V> ProducerRecord<K,V> addTraceHeaders(ProducerRecord<K,V> record) {
        String traceId = TraceUtil.getTraceId();
        String correlationId = MDC.get("correlationId");
        String spanId = MDC.get("spanId");

        if(traceId != null) {
            record.headers().add(TraceUtil.TRACE_HEADER, traceId.getBytes(StandardCharsets.UTF_8));
        }
        if(correlationId != null) {
            record.headers().add("X-Correlation-Id", correlationId.getBytes(StandardCharsets.UTF_8));
        }
        if(spanId != null) {
            record.headers().add("X-Span-Id", spanId.getBytes(StandardCharsets.UTF_8));
        }
        return record;
    }
}