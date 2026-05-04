package com.smartcart.common.util;

import com.smartcart.common.event.BaseEvent;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;

import java.nio.charset.StandardCharsets;

public final class KafkaTraceUtil {

    private KafkaTraceUtil() {}

    public static <K, V> ProducerRecord<K, V> addTraceHeaders(
            ProducerRecord<K, V> record,
            BaseEvent event,
            Tracer tracer
    ) {
        TraceUtil.syncFromTracer(tracer);

        addHeader(record, TraceUtil.TRACE_HEADER, valueOrFallback(event.getTraceId(), TraceUtil.currentTraceId(tracer)));
        addHeader(record, TraceUtil.SPAN_HEADER, valueOrFallback(event.getSpanId(), TraceUtil.currentSpanId(tracer)));
        addHeader(record, TraceUtil.CORRELATION_HEADER, valueOrFallback(event.getCorrelationId(), TraceUtil.getCorrelationId()));
        addHeader(record, TraceUtil.AGGREGATE_ID_HEADER, event.getAggregateId());
        addHeader(record, TraceUtil.AGGREGATE_TYPE_HEADER, event.getAggregateType() != null ? event.getAggregateType().name() : null);
        addHeader(record, TraceUtil.EVENT_ID_HEADER, event.getEventId());
        addHeader(record, TraceUtil.EVENT_TYPE_HEADER, event.getEventType() != null ? event.getEventType().name() : null);
        addHeader(record, TraceUtil.SOURCE_HEADER, event.getSource());

        return record;
    }

    public static <K, V extends BaseEvent> void runWithConsumerSpan(
            Tracer tracer,
            ConsumerRecord<K, V> record,
            String spanName,
            Runnable action
    ) {
        Span span = tracer.nextSpan().name(spanName).start();

        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            applyRecordContext(tracer, record);
            action.run();
        } finally {
            span.end();
            TraceUtil.clear();
        }
    }

    public static <K, V extends BaseEvent> void applyRecordContext(Tracer tracer, ConsumerRecord<K, V> record) {
        TraceUtil.syncFromTracer(tracer);

        V event = record.value();
        if (event == null) {
            return;
        }

        TraceUtil.setTraceId(valueOrFallback(readHeader(record, TraceUtil.TRACE_HEADER), event.getTraceId()));
        TraceUtil.setSpanId(TraceUtil.currentSpanId(tracer));
        TraceUtil.setCorrelationId(valueOrFallback(readHeader(record, TraceUtil.CORRELATION_HEADER), event.getCorrelationId()));
        TraceUtil.setAggregateContext(
                valueOrFallback(readHeader(record, TraceUtil.AGGREGATE_ID_HEADER), event.getAggregateId()),
                valueOrFallback(
                        readHeader(record, TraceUtil.AGGREGATE_TYPE_HEADER),
                        event.getAggregateType() != null ? event.getAggregateType().name() : null
                )
        );
        TraceUtil.setEventContext(
                valueOrFallback(readHeader(record, TraceUtil.EVENT_ID_HEADER), event.getEventId()),
                valueOrFallback(
                        readHeader(record, TraceUtil.EVENT_TYPE_HEADER),
                        event.getEventType() != null ? event.getEventType().name() : null
                ),
                valueOrFallback(readHeader(record, TraceUtil.SOURCE_HEADER), event.getSource())
        );
    }

    private static <K, V> void addHeader(ProducerRecord<K, V> record, String key, String value) {
        if (value != null && !value.isBlank()) {
            record.headers().remove(key);
            record.headers().add(key, value.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static <K, V> String readHeader(ConsumerRecord<K, V> record, String key) {
        Header header = record.headers().lastHeader(key);
        if (header == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }

    private static String valueOrFallback(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }
}
