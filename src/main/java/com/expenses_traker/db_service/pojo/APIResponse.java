package com.expenses_traker.db_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
public class APIResponse<T> {
    private String message;
    private boolean error;
    private T data;
}
