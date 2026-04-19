package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.Budget;
import com.expenses_traker.db_service.Repository.BudgetRepo;
import com.expenses_traker.db_service.util.ResponseHealper;
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

    private final ResponseHealper responseHealper = new ResponseHealper();

    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService(budgetRepo, responseHealper);
    }

    @Test
    void createBudget_rejectsDuplicate() {
        Budget b = new Budget();
        b.setUserId("1");
        b.setCategoryId(5L);
        b.setYear(2026);
        b.setMonth(4);
        b.setAmount(BigDecimal.valueOf(1000));

        when(budgetRepo.existsByUserIdAndCategoryIdAndYearAndMonth("1", 5L, 2026, 4)).thenReturn(Mono.just(true));

        StepVerifier.create(budgetService.createBudget(b))
                .assertNext(res -> {
                    assert res.isError();
                    assert res.getMessage().contains("already exists");
                })
                .verifyComplete();
    }

    @Test
    void createBudget_savesWhenValid() {
        Budget b = new Budget();
        b.setUserId("1");
        b.setCategoryId(5L);
        b.setYear(2026);
        b.setMonth(4);
        b.setAmount(BigDecimal.valueOf(1000));

        when(budgetRepo.existsByUserIdAndCategoryIdAndYearAndMonth("1", 5L, 2026, 4)).thenReturn(Mono.just(false));
        when(budgetRepo.save(any(Budget.class))).thenAnswer(inv -> {
            Budget saved = inv.getArgument(0);
            saved.setId(99L);
            return Mono.just(saved);
        });

        StepVerifier.create(budgetService.createBudget(b))
                .assertNext(res -> {
                    assert !res.isError();
                    assert res.getData() != null;
                    assert res.getData().getId() != null;
                })
                .verifyComplete();
    }

    @Test
    void updateBudget_rejectsInvalidMonth() {
        Budget existing = new Budget();
        existing.setId(1L);
        existing.setUserId("1");
        existing.setCategoryId(5L);
        existing.setYear(2026);
        existing.setMonth(4);
        existing.setAmount(BigDecimal.valueOf(1000));

        Budget patch = new Budget();
        patch.setMonth(13);

        when(budgetRepo.findById(1L)).thenReturn(Mono.just(existing));

        StepVerifier.create(budgetService.updateBudget(1L, patch))
                .assertNext(res -> {
                    assert res.isError();
                    assert res.getMessage().contains("month must be between 1 and 12");
                })
                .verifyComplete();
    }
}

