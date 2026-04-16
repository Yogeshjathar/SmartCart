package com.smartcart.common.event;

import com.smartcart.common.kafka.KafkaTopics;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UserCreatedEvent extends BaseEvent {

    private final String userId;
    private final String email;
    private final String role;
    private final String status;

    @Override
    public String getTopic() {
        return KafkaTopics.USER_CREATED;
    }
}
