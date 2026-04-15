package com.smartcart.user.producer;

import com.smartcart.common.event.UserCreatedEvent;
import com.smartcart.common.kafka.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Duration;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
    private final MeterRegistry meterRegistry;

//    @Value("${smartcart.kafka.topic.user-created}")
    private String topic = KafkaTopics.USER_CREATED;

    public void publish(UserCreatedEvent event) {

        String key = event.getAggregateId();

        long startTime = System.currentTimeMillis();

        ProducerRecord<String, UserCreatedEvent> record =
                new ProducerRecord<>(topic, key, event);

        // ✅ Add Standard Headers
        record.headers().add("eventId",
                event.getEventId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("eventType",
                event.getEventType().name().getBytes(StandardCharsets.UTF_8));
        record.headers().add("version",
                event.getVersion().getBytes(StandardCharsets.UTF_8));
        record.headers().add("traceId",
                event.getTraceId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("correlationId",
                event.getCorrelationId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("source",
                event.getSource().getBytes(StandardCharsets.UTF_8));

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