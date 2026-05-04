package com.smartcart.common.aop;

import com.smartcart.common.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;

@Slf4j
@Aspect
public class ExceptionLoggingAspect {

    @AfterThrowing(
            pointcut = "(" +
                    "execution(* com.smartcart..service..*(..)) || " +
                    "execution(* com.smartcart..controller..*(..)) || " +
                    "execution(* com.smartcart..consumer..*(..)) || " +
                    "execution(* com.smartcart..producer..*(..))" +
                    ")",
            throwing = "ex"
    )
    public void logExceptions(Throwable ex) {
        String traceId = TraceUtil.getTraceId();
        String correlationId = TraceUtil.getCorrelationId();

        log.error("[traceId={}, correlationId={}] Exception: {} - {}",
                traceId, correlationId, ex.getClass().getSimpleName(), ex.getMessage(), ex);
    }
}
