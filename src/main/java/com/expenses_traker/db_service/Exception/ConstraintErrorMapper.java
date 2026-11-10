package com.expenses_traker.db_service.Exception;

import java.util.Map;

public class ConstraintErrorMapper {

    private static final Map<String, ErrorMapping> CONSTRAINT_MAP = Map.ofEntries(

            Map.entry("fk_category_user",
                    new ErrorMapping(ErrorCode.INVALID_REFERENCE, "User not found")),

            Map.entry("fk_account_user",
                    new ErrorMapping(ErrorCode.INVALID_REFERENCE, "User not found")),

            Map.entry("fk_transaction_user",
                    new ErrorMapping(ErrorCode.INVALID_REFERENCE, "User not found")),

            Map.entry("fk_transaction_account",
                    new ErrorMapping(ErrorCode.INVALID_REFERENCE, "Account not found")),

            Map.entry("fk_transaction_category",
                    new ErrorMapping(ErrorCode.INVALID_REFERENCE, "Category not found")),

            Map.entry("uq_budget",
                    new ErrorMapping(ErrorCode.RESOURCE_ALREADY_EXISTS, "Budget already exists for this month")),

            Map.entry("chk_accounts_not_same",
                    new ErrorMapping(ErrorCode.INVALID_INPUT, "From and To accounts cannot be the same")),

            Map.entry("uq_category_user_name",
                    new ErrorMapping(ErrorCode.INVALID_INPUT, "Category already exists for this user"))
    );

    public static ErrorMapping resolve(String constraintName) {
        return CONSTRAINT_MAP.getOrDefault(
                constraintName,
                new ErrorMapping(ErrorCode.DATABASE_ERROR, "Database constraint violation")
        );
    }
}
