package com.expenses_traker.db_service.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("credit_card_details")
public class CreditCardEntity {

    @Id
    @Column("account_id")
    private Long accountId;

    @Column("outstanding")
    private BigDecimal outstanding;

    @Column("credit_limit")
    private BigDecimal creditLimit;

    @Column("due_date")
    private LocalDate dueDate;

    @Column("bill_date")
    private LocalDate billDate;
}