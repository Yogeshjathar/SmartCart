package com.smartcart.common.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UserCreatedEvent extends BaseEvent {

    private final Long userId;
    private final String email;
}
