package com.expenses_traker.db_service.pojo;

import lombok.Data;

@Data
public class APIResponse<T> {
    private String message;
    private boolean error;
    private T data;
    private ErrorResponse errorDetails;
}
