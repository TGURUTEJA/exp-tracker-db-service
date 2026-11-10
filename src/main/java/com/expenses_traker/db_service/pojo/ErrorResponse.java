package com.expenses_traker.db_service.pojo;

import lombok.Data;

import java.util.Map;
@Data
public class ErrorResponse {
    private String code;
    private String message;
    private Map<String, Object> details;
}
