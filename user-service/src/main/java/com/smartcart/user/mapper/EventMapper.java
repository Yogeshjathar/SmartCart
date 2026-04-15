package com.smartcart.user.mapper;

import com.smartcart.common.event.AggregateType;
import com.smartcart.common.event.EventType;
import com.smartcart.common.event.UserCreatedEvent;
import com.smartcart.user.entity.User;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.UUID;

public class EventMapper {
    @Value("${spring.application.name}")
    private String serviceName;
    String traceId = MDC.get("traceId");
    String correlationId = MDC.get("correlationId");

    public UserCreatedEvent buildUserCreatedEvent(User user){

        return UserCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EventType.USER_CREATED)
                .aggregateId(user.getId().toString())
                .aggregateType(AggregateType.USER)
                .occurredAt(Instant.now())
                .version("v1")
                .traceId(traceId)
                .correlationId(correlationId)
                .source(serviceName)
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
