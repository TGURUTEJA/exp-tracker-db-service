package com.expenses_traker.db_service.pojo.Accounts;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDetails {
    private Long id;
    private String userId;
    private String type;
    private String displayName;
    private BigDecimal balance;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
    private AccountExtraData additionalData;
}
