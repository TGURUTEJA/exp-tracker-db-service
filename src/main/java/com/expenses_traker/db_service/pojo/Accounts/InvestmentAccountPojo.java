package com.expenses_traker.db_service.pojo.Accounts;


import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.expenses_traker.db_service.pojo.Accounts.AccountDetails;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvestmentAccountPojo extends AccountDetails {
    private Long accountId;
    private String investmentType;
    private LocalDate purchaseDate;
    private LocalDate maturityDate;
    private BigDecimal amountInvested;
    private BigDecimal expectedReturn;
    private BigDecimal currentValue;
}
