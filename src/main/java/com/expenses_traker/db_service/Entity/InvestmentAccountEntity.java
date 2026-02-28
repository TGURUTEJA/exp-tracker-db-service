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
@Table("investment_account_details")
public class InvestmentAccountEntity {

        @Id
        @Column("account_id")
        private Long accountId;

        @Column("investment_type")
        private String investmentType;

        @Column("purchase_date")
        private LocalDate purchaseDate;

        @Column("maturity_date")
        private LocalDate maturityDate;

        @Column("amount_invested")
        private BigDecimal amountInvested;

        @Column("expected_return")
        private BigDecimal expectedReturn;

        @Column("current_value")
        private BigDecimal currentValue;
    }

