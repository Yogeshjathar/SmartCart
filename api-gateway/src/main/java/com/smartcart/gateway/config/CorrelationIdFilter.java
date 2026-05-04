package com.smartcart.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String finalCorrelationId = correlationId;

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder.header(CORRELATION_HEADER, finalCorrelationId))
                .build();

        return chain.filter(mutatedExchange)
                .then(Mono.fromRunnable(() ->
                        mutatedExchange.getResponse().getHeaders().set(CORRELATION_HEADER, finalCorrelationId)
                ));
    }
}
