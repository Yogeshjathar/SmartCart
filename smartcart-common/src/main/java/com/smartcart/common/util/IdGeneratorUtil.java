package com.smartcart.common.util;

import java.util.UUID;

public final class IdGeneratorUtil {

    private IdGeneratorUtil() {}

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
