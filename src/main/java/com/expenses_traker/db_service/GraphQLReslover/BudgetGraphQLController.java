package com.expenses_traker.db_service.GraphQLReslover;

import com.expenses_traker.db_service.Entity.Budget;
import com.expenses_traker.db_service.Service.BudgetService;
import com.expenses_traker.db_service.pojo.BudgetPojo;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BudgetGraphQLController {
    private final BudgetService budgetService;

    // ===== QUERIES =====

    @QueryMapping
    public Mono<Budget> getBudgetById(@Argument Long id, @Argument String userId) {
        return budgetService.getBudgetById(id, userId);
    }

    @QueryMapping
    public Mono<List<Budget>> getBudgetsByUserId(@Argument String userId) {
        return budgetService.getBudgetsByUserId(userId);
    }

    @QueryMapping
    public Mono<List<Budget>> getBudgetsByUserIdAndDateRange(
            @Argument String userId,
            @Argument Integer startYear,
            @Argument Integer startMonth,
            @Argument Integer endYear,
            @Argument Integer endMonth) {
        return budgetService.getBudgetsByUserIdAndDateRange(userId, startYear, startMonth, endYear, endMonth);
    }

    // ===== MUTATIONS =====

    @MutationMapping
    public Mono<Budget> createBudget(
            @Argument String userId,
            @Argument Long categoryId,
            @Argument Integer year,
            @Argument Integer month,
            @Argument BigDecimal amount,
            @Argument Boolean isActive,
            @Argument String recurrence) {
        BudgetPojo pojo = new BudgetPojo();
        pojo.setUserId(userId);
        pojo.setCategoryId(categoryId);
        pojo.setYear(year);
        pojo.setMonth(month);
        pojo.setAmount(amount);
        pojo.setIsActive(isActive);
        pojo.setRecurrence(recurrence);
        return budgetService.createBudget(pojo);
    }

    @MutationMapping
    public Mono<Budget> updateBudget(
            @Argument Long id,
            @Argument String userId,
            @Argument Long categoryId,
            @Argument Integer year,
            @Argument Integer month,
            @Argument BigDecimal amount,
            @Argument Boolean isActive,
            @Argument String recurrence) {
        BudgetPojo pojo = new BudgetPojo();
        pojo.setCategoryId(categoryId);
        pojo.setYear(year);
        pojo.setMonth(month);
        pojo.setAmount(amount);
        pojo.setIsActive(isActive);
        pojo.setRecurrence(recurrence);
        return budgetService.updateBudget(id, userId, pojo);
    }

    @MutationMapping
    public Mono<Boolean> deleteBudget(@Argument Long id, @Argument String userId) {
        return budgetService.deleteBudget(id, userId);
    }
}
