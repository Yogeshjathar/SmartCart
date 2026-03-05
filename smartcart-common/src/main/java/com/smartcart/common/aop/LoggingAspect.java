package com.smartcart.common.aop;

import com.smartcart.common.util.TraceUtil;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    private static final long SLOW_THRESHOLD = 1000;
    private final Tracer tracer;

    public LoggingAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    @Around("(execution(* com.smartcart..service..*(..)) || execution(* com.smartcart..controller..*(..))) && !bean(*Filter)")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = TraceUtil.getTraceId();
        String correlationId = MDC.get("correlationId");

        // Create child OT span
        Span span = tracer.nextSpan().name(joinPoint.getSignature().toShortString()).start();

        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            long start = System.currentTimeMillis();
            log.info("[traceId={}, correlationId={}] → Entering {}", traceId, correlationId, joinPoint.getSignature());

            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - start;
            if (duration > SLOW_THRESHOLD) {
                log.warn("[traceId={}, correlationId={}] Slow execution: {} took {} ms",
                        traceId, correlationId, joinPoint.getSignature(), duration);
            } else {
                log.info("[traceId={}, correlationId={}] ← Exiting {} | {} ms",
                        traceId, correlationId, joinPoint.getSignature(), duration);
            }
            return result;
        } catch (Throwable ex) {
            log.error("[traceId={}, correlationId={}] Exception in {}: {}",
                    traceId, correlationId, joinPoint.getSignature(), ex.getMessage(), ex);
            throw ex;
        } finally {
            span.end();
        }
    }
}