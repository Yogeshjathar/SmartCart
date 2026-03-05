package com.smartcart.common.config;

import com.smartcart.common.aop.*;
import com.smartcart.common.filter.TraceFilter;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(Tracer.class)
public class ObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnBean(Tracer.class)
    public LoggingAspect loggingAspect(Tracer tracer) { return new LoggingAspect(tracer); }

    @Bean
    public ExceptionLoggingAspect exceptionLoggingAspect() { return new ExceptionLoggingAspect(); }

    @Bean
    @ConditionalOnBean(Tracer.class)
    public KafkaLoggingAspect kafkaLoggingAspect(Tracer tracer) { return new KafkaLoggingAspect(tracer); }

    @Bean
    public SecurityAuditAspect securityAuditAspect() { return new SecurityAuditAspect(); }

    @Bean
    @ConditionalOnBean(Tracer.class)
    public Filter traceFilter(Tracer tracer) { return new TraceFilter(tracer); }

    @Bean
    @ConditionalOnClass(name = "feign.RequestInterceptor")
    public FeignTraceConfig feignTraceConfig() {
        return new FeignTraceConfig();
    }
}