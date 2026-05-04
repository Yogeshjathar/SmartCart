package com.smartcart.order.config;

import com.smartcart.common.event.InventoryReservationFailedEvent;
import com.smartcart.common.event.InventoryReservedEvent;
import com.smartcart.common.event.PaymentFailedEvent;
import com.smartcart.common.event.PaymentSuccessEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, InventoryReservedEvent> inventoryReservedConsumerFactory() {
        JsonDeserializer<InventoryReservedEvent> deserializer =
                new JsonDeserializer<>(InventoryReservedEvent.class);
        deserializer.addTrustedPackages("com.smartcart.common.event");
        return new DefaultKafkaConsumerFactory<>(baseProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConsumerFactory<String, InventoryReservationFailedEvent> inventoryReservationFailedConsumerFactory() {
        JsonDeserializer<InventoryReservationFailedEvent> deserializer =
                new JsonDeserializer<>(InventoryReservationFailedEvent.class);
        deserializer.addTrustedPackages("com.smartcart.common.event");
        return new DefaultKafkaConsumerFactory<>(baseProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConsumerFactory<String, PaymentSuccessEvent> paymentSuccessConsumerFactory() {
        JsonDeserializer<PaymentSuccessEvent> deserializer =
                new JsonDeserializer<>(PaymentSuccessEvent.class);
        deserializer.addTrustedPackages("com.smartcart.common.event");
        return new DefaultKafkaConsumerFactory<>(baseProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConsumerFactory<String, PaymentFailedEvent> paymentFailedConsumerFactory() {
        JsonDeserializer<PaymentFailedEvent> deserializer =
                new JsonDeserializer<>(PaymentFailedEvent.class);
        deserializer.addTrustedPackages("com.smartcart.common.event");
        return new DefaultKafkaConsumerFactory<>(baseProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservedEvent> inventoryReservedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, InventoryReservedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(inventoryReservedConsumerFactory());
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservationFailedEvent> inventoryReservationFailedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, InventoryReservationFailedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(inventoryReservationFailedConsumerFactory());
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent> paymentSuccessKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentSuccessConsumerFactory());
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> paymentFailedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentFailedConsumerFactory());
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

    private Map<String, Object> baseProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}
