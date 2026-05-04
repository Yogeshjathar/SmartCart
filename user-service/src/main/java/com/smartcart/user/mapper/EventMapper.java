package com.smartcart.user.mapper;

import com.smartcart.common.event.AggregateType;
import com.smartcart.common.event.EventType;
import com.smartcart.common.event.UserCreatedEvent;
import com.smartcart.common.util.TraceUtil;
import com.smartcart.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EventMapper {
    @Value("${spring.application.name}")
    private String serviceName;

    public UserCreatedEvent buildUserCreatedEvent(User user){

        return UserCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EventType.USER_CREATED)
                .aggregateId(user.getId().toString())
                .aggregateType(AggregateType.USER)
                .occurredAt(Instant.now())
                .version("v1")
                .traceId(TraceUtil.getTraceId())
                .spanId(TraceUtil.getSpanId())
                .correlationId(TraceUtil.resolveCorrelationId(null))
                .source(serviceName)
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
