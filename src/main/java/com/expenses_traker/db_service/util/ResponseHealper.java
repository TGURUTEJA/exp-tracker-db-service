package com.expenses_traker.db_service.util;

import java.util.List;

import com.expenses_traker.db_service.pojo.APIResponse;
import org.springframework.stereotype.Component;

@Component
public class ResponseHealper {

    public <T> APIResponse<T> buildErrorResponse(String message) {
        APIResponse<T> response = new APIResponse<>();
        response.setData(null);
        response.setError(true);
        response.setMessage(message);
        return response;
    }
    public <T> APIResponse<T> buildErrorResponse(T data , String message) {
        APIResponse<T> response = new APIResponse<>();
        response.setData(data);
        response.setError(true);
        response.setMessage(message);
        return response;
    }
    public <T> APIResponse<T> buildSuccessResponse( T data, String message) {
        APIResponse<T> response = new APIResponse<>();
        response.setData(data);
        response.setError(false);
        response.setMessage(message);
        return response;
    }

}
