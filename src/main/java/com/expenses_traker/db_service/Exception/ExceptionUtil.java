package com.expenses_traker.db_service.Exception;

public class ExceptionUtil {

    public static String extractConstraintName(Throwable ex) {
        String message = ex.getMessage();
        if (message == null) return null;

        int index = message.indexOf("constraint");
        if (index == -1) return null;

        int start = message.indexOf("\"", index);
        int end = message.indexOf("\"", start + 1);

        if (start != -1 && end != -1) {
            return message.substring(start + 1, end);
        }

        return null;
    }
}