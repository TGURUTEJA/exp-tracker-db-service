package com.expenses_traker.db_service.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionPojo {
    private Double accountId;
    private BigDecimal amount;
    private String debitCredit;
    private Integer categoryId;
    private String description;
    private String created;
}
