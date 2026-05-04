package com.smartcart.user.producer;

import com.smartcart.common.event.UserCreatedEvent;
import com.smartcart.common.kafka.KafkaTopics;
import com.smartcart.common.util.KafkaTraceUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;

    private String topic = KafkaTopics.USER_CREATED;

    public void publish(UserCreatedEvent event) {

        String key = event.getAggregateId();

        long startTime = System.currentTimeMillis();

        ProducerRecord<String, UserCreatedEvent> record =
                new ProducerRecord<>(topic, key, event);

        KafkaTraceUtil.addTraceHeaders(record, event, tracer);

        log.info(
                "Publishing event | eventId={} | aggregateId={} | correlationId={}",
                event.getEventId(),
                event.getAggregateId(),
                event.getCorrelationId()
        );

        CompletableFuture<SendResult<String, UserCreatedEvent>> future =
                kafkaTemplate.send(record);

        future.whenComplete((result, ex) -> {

            long duration = System.currentTimeMillis() - startTime;

            if (ex == null) {

                // ✅ Success Metric
                Counter.builder("smartcart.kafka.publish.success")
                        .tag("topic", topic)
                        .tag("eventType", event.getEventType().name())
                        .register(meterRegistry)
                        .increment();

                // ✅ Publish latency
                Timer.builder("smartcart.kafka.publish.latency")
                        .tag("topic", topic)
                        .register(meterRegistry)
                        .record(Duration.ofMillis(duration));

                log.info(
                        "Event published | eventId={} | partition={} | offset={} | durationMs={}",
                        event.getEventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        duration
                );

            } else {

                // ❌ Failure Metric
                Counter.builder("smartcart.kafka.publish.failure")
                        .tag("topic", topic)
                        .tag("eventType", event.getEventType().name())
                        .register(meterRegistry)
                        .increment();

                log.error(
                        "Event publish failed | eventId={} | aggregateId={} | error={}",
                        event.getEventId(),
                        event.getAggregateId(),
                        ex.getMessage(),
                        ex
                );
            }
        });
    }
}
