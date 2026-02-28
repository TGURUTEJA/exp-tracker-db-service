package com.expenses_traker.db_service.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("budgets")
@Data
public class Budget {

    @Id
    private Long id;

    @Column("user_id")
    private String userId;

    @Column("category_id")
    private Long categoryId;

    private Integer year;

    private Integer month;

    private BigDecimal amount;

    @Column("is_active")
    private Boolean isActive;

    private String recurrence;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;


}
