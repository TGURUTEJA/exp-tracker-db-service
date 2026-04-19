package com.expenses_traker.db_service.GraphQLReslover;

import com.expenses_traker.db_service.Entity.Budget;
import com.expenses_traker.db_service.Service.BudgetService;
import com.expenses_traker.db_service.pojo.APIResponse;
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

    @MutationMapping
    public Mono<APIResponse<Budget>> createBudget(@Argument("input") BudgetInput input) {
        return budgetService.createBudget(toEntity(input, false));
    }

    @MutationMapping
    public Mono<APIResponse<Budget>> updateBudget(@Argument Long id, @Argument("input") BudgetInput input) {
        return budgetService.updateBudget(id, toEntity(input, true));
    }

    @MutationMapping
    public Mono<APIResponse<Boolean>> deleteBudget(@Argument Long id) {
        return budgetService.deleteBudget(id);
    }

    @QueryMapping
    public Mono<APIResponse<Budget>> getBudgetById(@Argument Long id) {
        return budgetService.getBudgetById(id);
    }

    @QueryMapping
    public Mono<APIResponse<List<Budget>>> getBudgetsByUserId(@Argument String userId) {
        return budgetService.getBudgetsByUserId(userId);
    }

    @QueryMapping
    public Mono<APIResponse<List<Budget>>> getBudgetsByUserIdAndDateRange(@Argument("input") BudgetDateRangeInput input) {
        return budgetService.getBudgetsByUserIdAndDateRange(
                input.getUserId(),
                input.getStartYear(),
                input.getStartMonth(),
                input.getEndYear(),
                input.getEndMonth()
        );
    }

    private static Budget toEntity(BudgetInput input, boolean forUpdate) {
        Budget b = new Budget();
        if (input == null) {
            return b;
        }
        if (!forUpdate || input.getUserId() != null) b.setUserId(input.getUserId());
        if (!forUpdate || input.getCategoryId() != null) b.setCategoryId(input.getCategoryId() == null ? null : input.getCategoryId().longValue());
        if (!forUpdate || input.getYear() != null) b.setYear(input.getYear());
        if (!forUpdate || input.getMonth() != null) b.setMonth(input.getMonth());
        if (!forUpdate || input.getAmount() != null) b.setAmount(input.getAmount());
        if (!forUpdate || input.getIsActive() != null) b.setIsActive(input.getIsActive());
        if (!forUpdate || input.getRecurrence() != null) b.setRecurrence(input.getRecurrence());
        return b;
    }

    public static class BudgetInput {
        private String userId;
        private Integer categoryId;
        private Integer year;
        private Integer month;
        private BigDecimal amount;
        private Boolean isActive;
        private String recurrence;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean active) { isActive = active; }
        public String getRecurrence() { return recurrence; }
        public void setRecurrence(String recurrence) { this.recurrence = recurrence; }
    }

    public static class BudgetDateRangeInput {
        private String userId;
        private Integer startYear;
        private Integer startMonth;
        private Integer endYear;
        private Integer endMonth;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public Integer getStartYear() { return startYear; }
        public void setStartYear(Integer startYear) { this.startYear = startYear; }
        public Integer getStartMonth() { return startMonth; }
        public void setStartMonth(Integer startMonth) { this.startMonth = startMonth; }
        public Integer getEndYear() { return endYear; }
        public void setEndYear(Integer endYear) { this.endYear = endYear; }
        public Integer getEndMonth() { return endMonth; }
        public void setEndMonth(Integer endMonth) { this.endMonth = endMonth; }
    }
}

