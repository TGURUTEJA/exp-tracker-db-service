package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.Budget;
import com.expenses_traker.db_service.Exception.AppException;
import com.expenses_traker.db_service.Exception.ErrorCode;
import com.expenses_traker.db_service.Repository.BudgetRepo;
import com.expenses_traker.db_service.pojo.BudgetPojo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepo budgetRepo;

    // Create
    public Mono<Budget> createBudget(BudgetPojo budgetPojo) {
        if (budgetPojo == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Budget data cannot be null");
        }

        String userId = safeTrim(budgetPojo.getUserId());
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "User ID cannot be null or empty");
        }
        if (budgetPojo.getCategoryId() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Category ID cannot be null");
        }
        if (budgetPojo.getYear() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Year cannot be null");
        }
        if (budgetPojo.getMonth() == null || budgetPojo.getMonth() < 1 || budgetPojo.getMonth() > 12) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Month must be between 1 and 12");
        }
        if (budgetPojo.getAmount() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Amount cannot be null");
        }

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategoryId(budgetPojo.getCategoryId());
        budget.setYear(budgetPojo.getYear());
        budget.setMonth(budgetPojo.getMonth());
        budget.setAmount(budgetPojo.getAmount());
        budget.setIsActive(budgetPojo.getIsActive() != null ? budgetPojo.getIsActive() : true);
        budget.setRecurrence(budgetPojo.getRecurrence() != null ? budgetPojo.getRecurrence() : "monthly");

        LocalDateTime now = LocalDateTime.now();
        budget.setCreatedAt(now);
        budget.setUpdatedAt(now);

        return budgetRepo.existsByUserIdAndCategoryIdAndYearAndMonth(userId, budgetPojo.getCategoryId(), budgetPojo.getYear(), budgetPojo.getMonth())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Budget already exists for this user/category/period"));
                    }
                    return budgetRepo.save(budget);
                });
    }

    // Get by ID with user ID validation
    public Mono<Budget> getBudgetById(Long id, String userId) {
        if (id == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Budget ID cannot be null");
        }
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "User ID cannot be null");
        }

        return budgetRepo.findById(id)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.INVALID_REFERENCE, "Budget not found")))
                .flatMap(budget -> {
                    if (!budget.getUserId().equals(userId)) {
                        return Mono.error(new AppException(ErrorCode.INVALID_REFERENCE, "Budget does not belong to this user"));
                    }
                    return Mono.just(budget);
                });
    }

    // Get all budgets by user ID
    public Mono<List<Budget>> getBudgetsByUserId(String userId) {
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "User ID cannot be null");
        }

        return budgetRepo.findByUserId(userId).collectList();
    }

    // Get budgets by user ID and date range
    public Mono<List<Budget>> getBudgetsByUserIdAndDateRange(String userId, Integer startYear, Integer startMonth, Integer endYear, Integer endMonth) {
        String uid = safeTrim(userId);
        if (uid == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "User ID cannot be null or empty");
        }
        if (startYear == null || endYear == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Start year and end year are required");
        }
        if (startMonth == null || endMonth == null || startMonth < 1 || startMonth > 12 || endMonth < 1 || endMonth > 12) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Start month and end month must be between 1 and 12");
        }

        int startPeriod = startYear * 100 + startMonth;
        int endPeriod = endYear * 100 + endMonth;
        if (startPeriod > endPeriod) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Start period must be before or equal to end period");
        }

        return budgetRepo.findByUserIdAndPeriodBetween(uid, startPeriod, endPeriod).collectList();
    }

    // Update
    public Mono<Budget> updateBudget(Long id, String userId, BudgetPojo input) {
        if (id == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Budget ID cannot be null");
        }
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "User ID cannot be null");
        }

        return budgetRepo.findById(id)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.INVALID_REFERENCE, "Budget not found")))
                .flatMap(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        return Mono.error(new AppException(ErrorCode.INVALID_REFERENCE, "Budget does not belong to this user"));
                    }

                    Long newCategoryId = input.getCategoryId() != null ? input.getCategoryId() : existing.getCategoryId();
                    Integer newYear = input.getYear() != null ? input.getYear() : existing.getYear();
                    Integer newMonth = input.getMonth() != null ? input.getMonth() : existing.getMonth();

                    if (input.getMonth() != null && (newMonth < 1 || newMonth > 12)) {
                        return Mono.error(new AppException(ErrorCode.INVALID_INPUT, "Month must be between 1 and 12"));
                    }

                    Mono<Void> duplicateCheck = Mono.empty();
                    if (!newCategoryId.equals(existing.getCategoryId())
                            || !newYear.equals(existing.getYear())
                            || !newMonth.equals(existing.getMonth())) {
                        duplicateCheck = budgetRepo.existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
                                        userId, newCategoryId, newYear, newMonth, id
                                )
                                .flatMap(exists -> exists
                                        ? Mono.error(new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Budget already exists for this user/category/period"))
                                        : Mono.empty());
                    }

                    return duplicateCheck.then(Mono.defer(() -> {
                        if (input.getAmount() != null) existing.setAmount(input.getAmount());
                        if (input.getCategoryId() != null) existing.setCategoryId(input.getCategoryId());
                        if (input.getYear() != null) existing.setYear(input.getYear());
                        if (input.getMonth() != null) existing.setMonth(input.getMonth());
                        if (input.getIsActive() != null) existing.setIsActive(input.getIsActive());
                        if (input.getRecurrence() != null) existing.setRecurrence(input.getRecurrence());
                        existing.setUpdatedAt(LocalDateTime.now());
                        return budgetRepo.save(existing);
                    }));
                });
    }

    // Delete
    public Mono<Boolean> deleteBudget(Long id, String userId) {
        if (id == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Budget ID cannot be null");
        }
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "User ID cannot be null");
        }

        return budgetRepo.findById(id)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.INVALID_REFERENCE, "Budget not found")))
                .flatMap(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        return Mono.error(new AppException(ErrorCode.INVALID_REFERENCE, "Budget does not belong to this user"));
                    }
                    return budgetRepo.delete(existing).thenReturn(true);
                });
    }

    private static String safeTrim(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
