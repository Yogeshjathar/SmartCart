package com.smartcart.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND),
    ORDER_CANNOT_BE_CANCELLED(HttpStatus.CONFLICT),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST),
    MALFORMED_JSON(HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT),
    INSUFFICIENT_RESERVED_STOCK(HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
