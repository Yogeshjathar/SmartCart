package com.smartcart.common.kafka;

public class KafkaTopics {

/*  public static final String ORDER_EVENTS = "order-events";
    public static final String INVENTORY_EVENTS = "inventory-events";
    public static final String PAYMENT_EVENTS = "payment-events";
    public static final String USER_EVENTS = "user-events"; */

    public static final String ORDER_CREATED = "order-created";
    public static final String ORDER_CANCELLED = "order-cancelled";

    public static final String INVENTORY_UPDATED = "inventory-updated";

    public static final String PAYMENT_SUCCESS = "payment-success";
    public static final String PAYMENT_FAILED = "payment-failed";

    public static final String USER_CREATED = "user-created";
}
