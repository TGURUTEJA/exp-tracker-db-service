package com.expenses_traker.db_service.Exception;

import lombok.Getter;

@Getter
public class ErrorMapping {

    private final ErrorCode code;
    private final String message;

    public ErrorMapping(ErrorCode code, String message) {
        this.code = code;
        this.message = message;
    }

}
