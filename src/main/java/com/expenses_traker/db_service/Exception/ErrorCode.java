package com.expenses_traker.db_service.Exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    INVALID_INPUT("INVALID_INPUT"),
    INVALID_REFERENCE("INVALID_REFERENCE"),
    RESOURCE_ALREADY_EXISTS("RESOURCE_ALREADY_EXISTS"),
    DATABASE_ERROR("DATABASE_ERROR"),
    INTERNAL_ERROR("INTERNAL_ERROR");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }
}
