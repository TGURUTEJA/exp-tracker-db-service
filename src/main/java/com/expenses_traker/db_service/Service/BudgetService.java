package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.Budget;
import com.expenses_traker.db_service.Repository.BudgetRepo;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.util.ResponseHealper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepo budgetRepo;
    private final ResponseHealper responseHealper;

    // Create
    public Mono<APIResponse<Budget>> createBudget(Budget budget){
        return budgetRepo.save(budget)
                .map(savedBudget -> responseHealper.buildSuccessResponse(savedBudget, "Budget created successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to create budget: " + e.getMessage())));
    }
    // get by ID
    public Mono<APIResponse<Budget>> getBudgetById(Long id){
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
        return budgetRepo.findByUserId(userId)
                .collectList()
                .map(budgets -> budgets.isEmpty() ?
                        responseHealper.buildErrorResponse(budgets,"No budgets found for user ID: " + userId)
                        : responseHealper.buildSuccessResponse(budgets, "Budgets retrieved successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to retrieve budgets: " + e.getMessage())));
    }

    // Update by user ID
    public Mono<APIResponse<Budget>> updateBudget(Long id, Budget budget){
        return budgetRepo.findById(id)
                .flatMap(existingBudget -> {
                    existingBudget.setAmount(budget.getAmount());
                    existingBudget.setCategoryId(budget.getCategoryId());
                    existingBudget.setYear(budget.getYear());
                    existingBudget.setMonth(budget.getMonth());
                    existingBudget.setIsActive(budget.getIsActive());
                    existingBudget.setRecurrence(budget.getRecurrence());
                    existingBudget.setUpdatedAt(budget.getUpdatedAt());
                    return budgetRepo.save(existingBudget);
                })
                .map(updatedBudget -> responseHealper.buildSuccessResponse(updatedBudget, "Budget updated successfully"))
                .defaultIfEmpty(responseHealper.buildErrorResponse("Budget not found"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to update budget: " + e.getMessage())));
    }

    //Delete by ID
    public Mono<APIResponse<Boolean>> deleteBudget(Long id){
        return budgetRepo.findById(id)
                .flatMap(existingBudget ->
                        budgetRepo.delete(existingBudget)
                                .then(Mono.just(responseHealper.buildSuccessResponse(true, "Budget deleted successfully")))
                )
                .defaultIfEmpty(responseHealper.buildErrorResponse("Budget not found"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to delete budget: " + e.getMessage())));
    }
}
