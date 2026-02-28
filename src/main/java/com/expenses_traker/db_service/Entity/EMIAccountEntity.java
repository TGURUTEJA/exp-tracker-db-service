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
@Table("emi_account_details")
public class EMIAccountEntity {

    @Id
    @Column("account_id")
    private Long accountId;

    @Column("principal_amount")
    private BigDecimal principalAmount;

    @Column("interest_rate")
    private BigDecimal interestRate;

    @Column("total_installments")
    private Integer totalInstallments;

    @Column("paid_installments")
    private Integer paidInstallments;

    @Column("emi_amount")
    private BigDecimal emiAmount;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;

    @Column("next_due_date")
    private LocalDate nextDueDate;
}