package com.expenses_traker.db_service.pojo.Accounts;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.expenses_traker.db_service.pojo.Accounts.AccountDetails;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EMIAccountPojo extends AccountDetails{
    private Long accountId;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer totalInstallments;
    private Integer paidInstallments;
    private BigDecimal emiAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextDueDate;
}
