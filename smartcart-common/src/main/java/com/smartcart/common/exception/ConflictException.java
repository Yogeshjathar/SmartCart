package com.smartcart.common.exception;

public class ConflictException extends BaseException {

    public ConflictException(String message) {
        super(message, ErrorCode.DUPLICATE_RESOURCE, ErrorCode.DUPLICATE_RESOURCE.getHttpStatus());
    }
}
