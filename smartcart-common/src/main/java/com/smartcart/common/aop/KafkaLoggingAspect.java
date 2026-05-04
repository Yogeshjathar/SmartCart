package com.smartcart.common.aop;

import com.smartcart.common.util.JsonUtil;
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
        Span kafkaSpan = tracer.nextSpan().name("KafkaSend:" + topic).start();

        try (Tracer.SpanInScope ws = tracer.withSpan(kafkaSpan)) {
            TraceUtil.syncFromTracer(tracer);
            String traceId = TraceUtil.currentTraceId(tracer);
            String correlationId = MDC.get(TraceUtil.CORRELATION_ID);

            String payload = JsonUtil.convertToJson(message);

            log.info(
                    "[traceId={}, correlationId={}] Sending message to topic={} payload={}",
                    traceId,
                    correlationId,
                    topic,
                    payload
            );

        } finally {
            kafkaSpan.end();
        }
    }
}
