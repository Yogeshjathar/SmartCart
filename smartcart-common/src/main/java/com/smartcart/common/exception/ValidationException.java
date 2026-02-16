package com.smartcart.common.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {

    public ValidationException(String message) {
        super(message, ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }
}
