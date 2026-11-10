package com.expenses_traker.db_service.Exception;

import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.pojo.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public Mono<ResponseEntity<APIResponse<Object>>> handleAppException(AppException ex) {

        ErrorResponse error = new ErrorResponse();
        error.setCode(ex.getErrorCode().getCode());
        error.setMessage(ex.getMessage());
        error.setDetails(ex.getDetails());

        APIResponse<Object> response = new APIResponse<>();
        response.setError(true);
        response.setErrorDetails(error);
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<APIResponse<Object>>> handleDataIntegrity(DataIntegrityViolationException ex) {

        String constraint = ExceptionUtil.extractConstraintName(ex);

        ErrorMapping mapping;

        if (constraint != null) {
            mapping = ConstraintErrorMapper.resolve(constraint);
        } else {
            mapping = new ErrorMapping(ErrorCode.DATABASE_ERROR, "Database error");
        }

        ErrorResponse error = new ErrorResponse();
        error.setCode(mapping.getCode().getCode());
        error.setMessage(mapping.getMessage());

        APIResponse<Object> response = new APIResponse<>();
        response.setError(true);
        response.setErrorDetails(error);

        return Mono.just(ResponseEntity.badRequest().body(response));
    }
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<APIResponse<Object>>> handleGeneric(Exception ex) {

        ErrorResponse error = new ErrorResponse();
        error.setCode(ErrorCode.INTERNAL_ERROR.getCode());
        error.setMessage("Something went wrong");

        APIResponse<Object> response = new APIResponse<>();
        response.setError(true);
        response.setErrorDetails(error);

        return Mono.just(ResponseEntity.status(500).body(response));
    }
}
