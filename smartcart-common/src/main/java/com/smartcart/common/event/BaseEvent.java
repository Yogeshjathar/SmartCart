package com.smartcart.common.event;

import com.smartcart.common.util.DateTimeUtil;
import com.smartcart.common.util.IdGeneratorUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

@Getter
@SuperBuilder
public abstract class BaseEvent implements Serializable {

    @Builder.Default
    private final String eventId = IdGeneratorUtil.generateUUID();
    private final EventType eventType;
    private final String aggregateId;     // userId / orderId
    private final AggregateType aggregateType;

    @Builder.Default
    private final Instant occurredAt = DateTimeUtil.nowUTC();
    private final String traceId;
    private final String version;
    private final String correlationId;    // For saga / workflow tracking
    private final String source;

    public abstract String getTopic();
}
