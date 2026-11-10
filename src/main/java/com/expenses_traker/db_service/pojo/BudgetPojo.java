package com.expenses_traker.db_service.pojo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetPojo {
    private Long id;
    private String userId;
    private Long categoryId;
    private Integer year;
    private Integer month;
    private BigDecimal amount;
    private Boolean isActive;
    private String recurrence;
}

