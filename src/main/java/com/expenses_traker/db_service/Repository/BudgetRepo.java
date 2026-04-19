package com.expenses_traker.db_service.Repository;

import com.expenses_traker.db_service.Entity.Budget;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BudgetRepo extends R2dbcRepository<Budget,Long> {
    Flux<Budget> findByUserId(String userId);

    Mono<Boolean> existsByUserIdAndCategoryIdAndYearAndMonth(String userId, Long categoryId, Integer year, Integer month);

    Mono<Boolean> existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(String userId, Long categoryId, Integer year, Integer month, Long id);

    @Query("""
            SELECT *
            FROM budgets
            WHERE user_id = :userId
              AND (year * 100 + month) BETWEEN :startPeriod AND :endPeriod
            ORDER BY year, month
            """)
    Flux<Budget> findByUserIdAndPeriodBetween(
            @Param("userId") String userId,
            @Param("startPeriod") Integer startPeriod,
            @Param("endPeriod") Integer endPeriod
    );
}
