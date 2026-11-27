package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.CategoryEntity;
import com.expenses_traker.db_service.Repository.CategoryRepo;
import com.expenses_traker.db_service.pojo.CategoryPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryServiceOwnershipTest {

    private CategoryRepo categoryRepo;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryRepo = mock(CategoryRepo.class);
        categoryService = new CategoryService(categoryRepo);
    }

    @Test
    void updateDoesNotFindCategoryOutsideUserScope() {
        when(categoryRepo.findByIdAndUserId(8L, "user-a")).thenReturn(Mono.empty());

        StepVerifier.create(categoryService.updateCategory(8L, updateInput("user-a")))
                .expectError()
                .verify();

        verify(categoryRepo, never()).save(org.mockito.ArgumentMatchers.any(CategoryEntity.class));
    }

    @Test
    void deleteDoesNotDeleteCategoryOutsideUserScope() {
        when(categoryRepo.findByIdAndUserId(8L, "user-a")).thenReturn(Mono.empty());

        StepVerifier.create(categoryService.deleteCategory(8L, "user-a"))
                .expectError()
                .verify();

        verify(categoryRepo, never()).delete(org.mockito.ArgumentMatchers.any(CategoryEntity.class));
    }

    @Test
    void updatePreservesAndReturnsIconAndColorForOwnedCategory() {
        CategoryEntity existing = new CategoryEntity();
        existing.setId(8L);
        existing.setUserId("user-a");
        existing.setName("Dining");
        existing.setType("NEED");
        existing.setIcon("food");
        existing.setColor("green");
        when(categoryRepo.findByIdAndUserId(8L, "user-a")).thenReturn(Mono.just(existing));
        when(categoryRepo.save(existing)).thenReturn(Mono.just(existing));
        CategoryPojo input = updateInput("user-a");
        input.setIcon("restaurant");
        input.setColor("amber");

        StepVerifier.create(categoryService.updateCategory(8L, input))
                .assertNext(updated -> {
                    org.junit.jupiter.api.Assertions.assertEquals("restaurant", updated.getIcon());
                    org.junit.jupiter.api.Assertions.assertEquals("amber", updated.getColor());
                })
                .verifyComplete();

        verify(categoryRepo).findByIdAndUserId(8L, "user-a");
    }

    private CategoryPojo updateInput(String userId) {
        CategoryPojo input = new CategoryPojo();
        input.setUserId(userId);
        input.setName("Dining");
        input.setType("NEED");
        return input;
    }
}
