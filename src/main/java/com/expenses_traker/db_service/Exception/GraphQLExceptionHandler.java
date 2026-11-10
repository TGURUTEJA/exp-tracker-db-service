package com.expenses_traker.db_service.Exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        if (ex instanceof AppException appEx) {
            return GraphqlErrorBuilder.newError(env)
                    .message(appEx.getMessage())
                    .extensions(Map.of(
                            "code", appEx.getErrorCode().getCode(),
                            "details", appEx.getDetails()
                    ))
                    .build();
        }

        if (ex instanceof DataIntegrityViolationException dbEx) {

            String constraint = ExceptionUtil.extractConstraintName(dbEx);
            ErrorMapping mapping = constraint != null
                    ? ConstraintErrorMapper.resolve(constraint)
                    : new ErrorMapping(ErrorCode.DATABASE_ERROR, "Database error");

            return GraphqlErrorBuilder.newError(env)
                    .message(mapping.getMessage())
                    .extensions(Map.of(
                            "code", mapping.getCode().getCode()
                    ))
                    .build();
        }

        return GraphqlErrorBuilder.newError(env)
                .message("Something went wrong")
                .extensions(Map.of("code", ErrorCode.INTERNAL_ERROR.getCode()))
                .build();
    }
}
