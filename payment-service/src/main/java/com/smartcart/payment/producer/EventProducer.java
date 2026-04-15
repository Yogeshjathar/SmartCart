package com.smartcart.payment.producer;

import com.smartcart.common.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(BaseEvent event) {
        ProducerRecord<String, Object> record =
                new ProducerRecord<>(event.getTopic(), event.getAggregateId(), event);

        addHeader(record, "eventId", event.getEventId());
        addHeader(record, "eventType", event.getEventType().name());
        addHeader(record, "version", event.getVersion());
        addHeader(record, "traceId", event.getTraceId());
        addHeader(record, "correlationId", event.getCorrelationId());
        addHeader(record, "source", event.getSource());

        log.info(
                "Publishing payment workflow event | eventId={} | type={} | topic={}",
                event.getEventId(),
                event.getEventType(),
                event.getTopic()
        );

        kafkaTemplate.send(record);
    }

    private void addHeader(ProducerRecord<String, Object> record, String key, String value) {
        if (value != null) {
            record.headers().add(key, value.getBytes(StandardCharsets.UTF_8));
        }
    }
}
