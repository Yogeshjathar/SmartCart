package com.smartcart.common.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@SuperBuilder
public abstract class BaseEvent implements Serializable {

    private final String eventId;
    private final String eventType;
    private final String aggregateId;     // userId / orderId
    private final AggregateType aggregateType;
    private final Instant occurredAt;
    private final String traceId;
    private final String version;
    private final String correlationId;    // For saga / workflow tracking
    private final String source;
}
