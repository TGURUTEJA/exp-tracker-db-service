package com.expenses_traker.db_service.pojo.Accounts;



import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.expenses_traker.db_service.pojo.Accounts.AccountDetails;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FriendAccountPojo extends AccountDetails {
    private Long accountId;
    private String friendName;
    private String contactInfo;
    private BigDecimal outstandingAmount;
    private LocalDate loanStartDate;
    private LocalDate loanDueDate;
}
