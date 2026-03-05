package com.smartcart.common.aop;

import com.smartcart.common.util.TraceUtil;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class KafkaLoggingAspect {

    private final Tracer tracer;

    public KafkaLoggingAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    // Intercept all KafkaTemplate.send() calls
    @Before("execution(* org.springframework.kafka.core.KafkaTemplate.send(..)) && args(topic, message)")
    public void logKafkaSend(String topic, Object message) {
        String traceId = TraceUtil.getTraceId();
        String correlationId = MDC.get("correlationId");

        // Create OT span for Kafka produce
        Span kafkaSpan = tracer.nextSpan().name("KafkaSend:" + topic).start();
        try (Tracer.SpanInScope ws = tracer.withSpan(kafkaSpan)) {
            log.info("[traceId={}, correlationId={}] Sending message to Kafka topic: {}", traceId, correlationId, topic);
        } finally {
            kafkaSpan.end();
        }
    }
}