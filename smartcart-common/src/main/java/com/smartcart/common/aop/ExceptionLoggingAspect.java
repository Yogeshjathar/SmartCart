package com.smartcart.common.aop;

import com.smartcart.common.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;

@Slf4j
@Aspect
public class ExceptionLoggingAspect {

    @AfterThrowing(pointcut = "execution(* com.smartcart..*(..))", throwing = "ex")
    public void logExceptions(Throwable ex) {
        String traceId = TraceUtil.getTraceId();
        String correlationId = MDC.get("correlationId");

        log.error("[traceId={}, correlationId={}] Exception: {} - {}",
                traceId, correlationId, ex.getClass().getSimpleName(), ex.getMessage(), ex);
    }
}