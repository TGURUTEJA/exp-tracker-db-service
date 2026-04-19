package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.Budget;
import com.expenses_traker.db_service.Repository.BudgetRepo;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.util.ResponseHealper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepo budgetRepo;
    private final ResponseHealper responseHealper;

    // Create
    public Mono<APIResponse<Budget>> createBudget(Budget budget){
        if (budget == null) {
            return Mono.just(responseHealper.buildErrorResponse("Budget data cannot be null"));
        }
        String userId = safeTrim(budget.getUserId());
        if (userId == null) {
            return Mono.just(responseHealper.buildErrorResponse("userId cannot be null or empty"));
        }
        if (budget.getCategoryId() == null) {
            return Mono.just(responseHealper.buildErrorResponse("categoryId cannot be null"));
        }
        if (budget.getYear() == null) {
            return Mono.just(responseHealper.buildErrorResponse("year cannot be null"));
        }
        if (budget.getMonth() == null || budget.getMonth() < 1 || budget.getMonth() > 12) {
            return Mono.just(responseHealper.buildErrorResponse("month must be between 1 and 12"));
        }
        if (budget.getAmount() == null) {
            return Mono.just(responseHealper.buildErrorResponse("amount cannot be null"));
        }

        budget.setUserId(userId);

        if (budget.getIsActive() == null) {
            budget.setIsActive(true);
        }
        if (budget.getRecurrence() == null) {
            budget.setRecurrence("monthly");
        }

        LocalDateTime now = LocalDateTime.now();
        if (budget.getCreatedAt() == null) budget.setCreatedAt(now);
        budget.setUpdatedAt(now);

        return budgetRepo.existsByUserIdAndCategoryIdAndYearAndMonth(userId, budget.getCategoryId(), budget.getYear(), budget.getMonth())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(responseHealper.buildErrorResponse("Budget already exists for this user/category/period"));
                    }
                    return budgetRepo.save(budget)
                            .map(savedBudget -> responseHealper.buildSuccessResponse(savedBudget, "Budget created successfully"))
                            .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to create budget: " + e.getMessage())));
                });
    }
    // get by ID
    public Mono<APIResponse<Budget>> getBudgetById(Long id){
        if (id == null) {
            return Mono.just(responseHealper.buildErrorResponse("Budget id cannot be null"));
        }
        return budgetRepo.findById(id)
                .map(budget -> responseHealper.buildSuccessResponse(budget, "Budget retrieved successfully"))
                .defaultIfEmpty(responseHealper.buildErrorResponse("Budget not found"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to retrieve budget: " + e.getMessage())));
    }
    // get all budgets
    public Mono<APIResponse<List<Budget>>> getAllBudgets(){
        return budgetRepo.findAll()
                .collectList()
                .map(budgets -> responseHealper.buildSuccessResponse(budgets, "Budgets retrieved successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to retrieve budgets: " + e.getMessage())));
    }
    // get by user ID
    public Mono<APIResponse<List<Budget>>> getBudgetsByUserId(String userId) {
        if (safeTrim(userId) == null) {
            return Mono.just(responseHealper.buildErrorResponse("userId cannot be null or empty"));
        }
        return budgetRepo.findByUserId(userId)
                .collectList()
                .map(budgets -> budgets.isEmpty() ?
                        responseHealper.buildErrorResponse(budgets,"No budgets found for user ID: " + userId)
                        : responseHealper.buildSuccessResponse(budgets, "Budgets retrieved successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to retrieve budgets: " + e.getMessage())));
    }

    public Mono<APIResponse<List<Budget>>> getBudgetsByUserIdAndDateRange(String userId, Integer startYear, Integer startMonth, Integer endYear, Integer endMonth) {
        String uid = safeTrim(userId);
        if (uid == null) {
            return Mono.just(responseHealper.buildErrorResponse("userId cannot be null or empty"));
        }
        if (startYear == null || endYear == null) {
            return Mono.just(responseHealper.buildErrorResponse("startYear and endYear are required"));
        }
        if (startMonth == null || endMonth == null || startMonth < 1 || startMonth > 12 || endMonth < 1 || endMonth > 12) {
            return Mono.just(responseHealper.buildErrorResponse("startMonth and endMonth must be between 1 and 12"));
        }

        int startPeriod = startYear * 100 + startMonth;
        int endPeriod = endYear * 100 + endMonth;
        if (startPeriod > endPeriod) {
            return Mono.just(responseHealper.buildErrorResponse("start period must be before or equal to end period"));
        }

        return budgetRepo.findByUserIdAndPeriodBetween(uid, startPeriod, endPeriod)
                .collectList()
                .map(list -> list.isEmpty()
                        ? responseHealper.buildErrorResponse(list, "No budgets found for user in given range")
                        : responseHealper.buildSuccessResponse(list, "Budgets retrieved successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to retrieve budgets: " + e.getMessage())));
    }

    // Update by user ID
    public Mono<APIResponse<Budget>> updateBudget(Long id, Budget budget){
        if (id == null) {
            return Mono.just(responseHealper.buildErrorResponse("Budget id cannot be null"));
        }
        if (budget == null) {
            return Mono.just(responseHealper.buildErrorResponse("Budget data cannot be null"));
        }
        return budgetRepo.findById(id)
                .flatMap(existingBudget -> {
                    Long newCategoryId = budget.getCategoryId() != null ? budget.getCategoryId() : existingBudget.getCategoryId();
                    Integer newYear = budget.getYear() != null ? budget.getYear() : existingBudget.getYear();
                    Integer newMonth = budget.getMonth() != null ? budget.getMonth() : existingBudget.getMonth();

                    if (budget.getMonth() != null && (newMonth < 1 || newMonth > 12)) {
                        return Mono.error(new IllegalArgumentException("month must be between 1 and 12"));
                    }

                    Mono<Void> duplicateCheck = Mono.empty();
                    if (!newCategoryId.equals(existingBudget.getCategoryId())
                            || !newYear.equals(existingBudget.getYear())
                            || !newMonth.equals(existingBudget.getMonth())) {
                        duplicateCheck = budgetRepo.existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
                                        existingBudget.getUserId(), newCategoryId, newYear, newMonth, id
                                )
                                .flatMap(exists -> exists
                                        ? Mono.error(new IllegalArgumentException("Budget already exists for this user/category/period"))
                                        : Mono.empty());
                    }

                    return duplicateCheck.then(Mono.defer(() -> {
                        if (budget.getAmount() != null) existingBudget.setAmount(budget.getAmount());
                        if (budget.getCategoryId() != null) existingBudget.setCategoryId(budget.getCategoryId());
                        if (budget.getYear() != null) existingBudget.setYear(budget.getYear());
                        if (budget.getMonth() != null) existingBudget.setMonth(budget.getMonth());
                        if (budget.getIsActive() != null) existingBudget.setIsActive(budget.getIsActive());
                        if (budget.getRecurrence() != null) existingBudget.setRecurrence(budget.getRecurrence());
                        existingBudget.setUpdatedAt(LocalDateTime.now());
                        return budgetRepo.save(existingBudget);
                    }));
                })
                .map(updatedBudget -> responseHealper.buildSuccessResponse(updatedBudget, "Budget updated successfully"))
                .defaultIfEmpty(responseHealper.buildErrorResponse("Budget not found"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse(
                        (e instanceof IllegalArgumentException) ? e.getMessage() : "Failed to update budget: " + e.getMessage()
                )));
    }

    //Delete by ID
    public Mono<APIResponse<Boolean>> deleteBudget(Long id){
        if (id == null) {
            return Mono.just(responseHealper.buildErrorResponse("Budget id cannot be null"));
        }
        return budgetRepo.findById(id)
                .flatMap(existingBudget ->
                        budgetRepo.delete(existingBudget)
                                .then(Mono.just(responseHealper.buildSuccessResponse(true, "Budget deleted successfully")))
                )
                .defaultIfEmpty(responseHealper.buildErrorResponse("Budget not found"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to delete budget: " + e.getMessage())));
    }

    private static String safeTrim(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
