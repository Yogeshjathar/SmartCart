package com.smartcart.common.config;

import com.smartcart.common.util.TraceUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;

public class FeignTraceConfig {

    @Bean
    public RequestInterceptor feignTraceInterceptor(Tracer tracer) {

        return new RequestInterceptor() {

            @Override
            public void apply(RequestTemplate template) {
                TraceUtil.syncFromTracer(tracer);
                String correlationId = MDC.get(TraceUtil.CORRELATION_ID);

                if (correlationId != null) {
                    template.header(TraceUtil.CORRELATION_HEADER, correlationId);
                }
                template.header(TraceUtil.TRACE_HEADER, TraceUtil.currentTraceId(tracer));
                template.header(TraceUtil.SPAN_HEADER, TraceUtil.currentSpanId(tracer));
            }
        };
    }
}
