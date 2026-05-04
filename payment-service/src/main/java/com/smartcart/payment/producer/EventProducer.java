package com.smartcart.payment.producer;

import com.smartcart.common.event.BaseEvent;
import com.smartcart.common.util.KafkaTraceUtil;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Tracer tracer;

    public void publish(BaseEvent event) {
        ProducerRecord<String, Object> record =
                new ProducerRecord<>(event.getTopic(), event.getAggregateId(), event);

        KafkaTraceUtil.addTraceHeaders(record, event, tracer);

        log.info(
                "Publishing payment workflow event | eventId={} | type={} | topic={}",
                event.getEventId(),
                event.getEventType(),
                event.getTopic()
        );

        kafkaTemplate.send(record);
    }
}
