package com.expenses_traker.db_service.Exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

}