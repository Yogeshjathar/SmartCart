package com.smartcart.common.exception;

import com.smartcart.common.response.ApiResponse;
import com.smartcart.common.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException ex) {

        log.error("Business error occurred",
                "errorCode", ex.getErrorCode(),
                "traceId", MDC.get("traceId"),
                ex);

        ApiResponse<Object> response =
                ApiResponse.failure(ex.getMessage(), ex.getErrorCode().name());

        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {

        log.error("Unhandled exception", ex);

        ApiResponse<Object> response =
                ApiResponse.failure("Internal Server Error",
                        ErrorCode.INTERNAL_SERVER_ERROR.name());

        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflict(ConflictException ex) {
        System.out.println("Conflict handler triggered");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(ex.getMessage(), "DUPLICATE_RESOURCE"));
    }
}
