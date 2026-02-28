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
@Table("friend_account_details")
public class FriendAccountEntity {

    @Id
    @Column("account_id")
    private Long accountId;

    @Column("friend_name")
    private String friendName;

    @Column("contact_info")
    private String contactInfo;

    @Column("outstanding_amount")
    private BigDecimal outstandingAmount;

    @Column("loan_start_date")
    private LocalDate loanStartDate;

    @Column("loan_due_date")
    private LocalDate loanDueDate;
}
