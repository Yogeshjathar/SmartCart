package com.smartcart.common.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UserCreatedEvent extends BaseEvent {

    private final String userId;
    private final String email;
    private final String role;
    private final boolean active;
}
