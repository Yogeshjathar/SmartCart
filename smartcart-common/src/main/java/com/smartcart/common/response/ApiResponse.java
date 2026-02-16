package com.smartcart.common.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartcart.common.util.DateTimeUtil;
import com.smartcart.common.util.TraceUtil;
import java.io.Serializable;
import java.time.Instant;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiResponse<T> implements Serializable {

    private final boolean success;
    private final String message;
    private final T data;
    private final Instant timestamp;
    private final String traceId;
    private final String errorCode;

    @Builder
    private ApiResponse(boolean success,
                        String message,
                        T data,
                        Instant timestamp,
                        String traceId,
                        String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.traceId = traceId;
        this.errorCode = errorCode;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(DateTimeUtil.nowUTC())
                .traceId(TraceUtil.getTraceId())
                .build();
    }

    public static <T> ApiResponse<T> failure(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(DateTimeUtil.nowUTC())
                .traceId(TraceUtil.getTraceId())
                .errorCode(errorCode)
                .build();
    }
}
