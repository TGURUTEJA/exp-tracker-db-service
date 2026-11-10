package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.Budget;
import com.expenses_traker.db_service.Repository.BudgetRepo;
import com.expenses_traker.db_service.pojo.BudgetPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepo budgetRepo;

    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService(budgetRepo);
    }

    @Test
    void createBudget_rejectsDuplicate() {
        BudgetPojo pojo = new BudgetPojo();
        pojo.setUserId("user123");
        pojo.setCategoryId(5L);
        pojo.setYear(2026);
        pojo.setMonth(4);
        pojo.setAmount(BigDecimal.valueOf(1000));

        when(budgetRepo.existsByUserIdAndCategoryIdAndYearAndMonth("user123", 5L, 2026, 4))
                .thenReturn(Mono.just(true));

        StepVerifier.create(budgetService.createBudget(pojo))
                .expectErrorMessage("Budget already exists for this user/category/period")
                .verify();
    }

    @Test
    void createBudget_savesWhenValid() {
        BudgetPojo pojo = new BudgetPojo();
        pojo.setUserId("user123");
        pojo.setCategoryId(5L);
        pojo.setYear(2026);
        pojo.setMonth(4);
        pojo.setAmount(BigDecimal.valueOf(1000));

        Budget savedBudget = new Budget();
        savedBudget.setId(1L);
        savedBudget.setUserId("user123");
        savedBudget.setCategoryId(5L);
        savedBudget.setYear(2026);
        savedBudget.setMonth(4);
        savedBudget.setAmount(BigDecimal.valueOf(1000));

        when(budgetRepo.existsByUserIdAndCategoryIdAndYearAndMonth("user123", 5L, 2026, 4))
                .thenReturn(Mono.just(false));
        when(budgetRepo.save(any(Budget.class))).thenReturn(Mono.just(savedBudget));

        StepVerifier.create(budgetService.createBudget(pojo))
                .assertNext(budget -> {
                    assert budget.getId().equals(1L);
                    assert budget.getUserId().equals("user123");
                })
                .verifyComplete();
    }

    @Test
    void updateBudget_rejectsInvalidMonth() {
        BudgetPojo pojo = new BudgetPojo();
        pojo.setMonth(13);

        Budget existing = new Budget();
        existing.setId(1L);
        existing.setUserId("user123");
        existing.setCategoryId(5L);
        existing.setYear(2026);
        existing.setMonth(4);
        existing.setAmount(BigDecimal.valueOf(1000));

        when(budgetRepo.findById(1L)).thenReturn(Mono.just(existing));

        StepVerifier.create(budgetService.updateBudget(1L, "user123", pojo))
                .expectErrorMessage("Month must be between 1 and 12")
                .verify();
    }
}
