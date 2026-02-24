package com.smartcart.common.exception;

import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode, HttpStatus.NOT_FOUND);
    }
}
