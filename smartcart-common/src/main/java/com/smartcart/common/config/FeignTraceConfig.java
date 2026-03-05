package com.smartcart.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;

public class FeignTraceConfig {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Bean
    public RequestInterceptor feignTraceInterceptor() {

        return new RequestInterceptor() {

            @Override
            public void apply(RequestTemplate template) {

                String correlationId = MDC.get("correlationId");

                if (correlationId != null) {
                    template.header(CORRELATION_ID_HEADER, correlationId);
                }

            }
        };
    }
}