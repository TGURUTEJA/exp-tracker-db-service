package com.expenses_traker.db_service.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.LocalDateTime;;

@Data
@Table("accounts")
public class AccountEntity {

    @Id
    private Long id;

    @Column("user_id")
    private String userId;

    @Column("type")
    private String type;

    @Column("display_name")
    private String displayName;

    @Column("balance")
    private BigDecimal balance;

    @Column("currency")
    private String currency;

    @Column("status")
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;
}
