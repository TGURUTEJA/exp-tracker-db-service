package com.expenses_traker.db_service.pojo.Accounts;


import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreditCardPojo{
    private BigDecimal outstanding;
    private BigDecimal creditLimit;
    private LocalDate dueDate;
    private LocalDate billDate;
}
