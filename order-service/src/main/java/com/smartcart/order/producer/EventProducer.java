package com.smartcart.order.producer;

import com.smartcart.common.event.BaseEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    public void publish(BaseEvent event) {

        String topic = event.getTopic();
        String key = event.getAggregateId();

        long startTime = System.currentTimeMillis();

        ProducerRecord<String, Object> record =
                new ProducerRecord<>(topic, key, event);

        addHeaders(record, event);

        log.info("Publishing event | eventId={} | type={} | topic={}",
                event.getEventId(),
                event.getEventType(),
                topic);

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(record);

        future.whenComplete((result, ex) ->
                handleResult(event, topic, startTime, result, ex));
    }

    private void addHeaders(ProducerRecord<String, Object> record, BaseEvent event) {

        record.headers().add("eventId", event.getEventId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("eventType", event.getEventType().name().getBytes(StandardCharsets.UTF_8));
        record.headers().add("version", event.getVersion().getBytes(StandardCharsets.UTF_8));
        record.headers().add("traceId", event.getTraceId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("correlationId", event.getCorrelationId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("source", event.getSource().getBytes(StandardCharsets.UTF_8));
    }

    private void handleResult(BaseEvent event, String topic,
                              long startTime,
                              SendResult<String, Object> result,
                              Throwable ex) {

        long duration = System.currentTimeMillis() - startTime;

        if (ex == null) {

            Counter.builder("smartcart.kafka.publish.success")
                    .tag("topic", topic)
                    .tag("eventType", event.getEventType().name())
                    .register(meterRegistry)
                    .increment();

            Timer.builder("smartcart.kafka.publish.latency")
                    .tag("topic", topic)
                    .register(meterRegistry)
                    .record(Duration.ofMillis(duration));

            log.info("Event published | eventId={} | partition={} | offset={}",
                    event.getEventId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } else {

            Counter.builder("smartcart.kafka.publish.failure")
                    .tag("topic", topic)
                    .tag("eventType", event.getEventType().name())
                    .register(meterRegistry)
                    .increment();

            log.error("Event publish failed | eventId={} | error={}",
                    event.getEventId(), ex.getMessage(), ex);
        }
    }
}