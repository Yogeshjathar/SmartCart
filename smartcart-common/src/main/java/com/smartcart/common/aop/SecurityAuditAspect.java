package com.smartcart.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Aspect
public class SecurityAuditAspect {

    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    public void auditApiAccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth != null) ? auth.getName() : "anonymous";

        String traceId = MDC.get("traceId");
        String correlationId = MDC.get("correlationId");

        log.info("[traceId={}, correlationId={}] API accessed by user: {}", traceId, correlationId, user);
    }
}