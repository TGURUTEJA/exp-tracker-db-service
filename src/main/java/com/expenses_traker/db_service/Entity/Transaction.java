package com.expenses_traker.db_service.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("transactions")
@Data
public class Transaction {

    @Id
    private Long id;

    @Column("user_id")
    private String userId;

    @Column("account_id")
    private Long accountId;

    @Column("debit_credit")
    private String debitCredit;

    private BigDecimal amount;

    @Column("category_id")
    private Long categoryId;

    private String description;

    private LocalDateTime created;

    @Column("account_balance")
    private BigDecimal accountBalance;

}
