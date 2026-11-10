package com.expenses_traker.db_service.GraphQL;

import com.expenses_traker.db_service.Entity.Budget;
import com.expenses_traker.db_service.Service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class BudgetGraphQLControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BudgetService budgetService;

    @Test
    void createBudget_mutationRetursBudget() {
        Budget saved = new Budget();
        saved.setId(1L);
        saved.setUserId("user123");
        saved.setCategoryId(10L);
        saved.setYear(2026);
        saved.setMonth(4);
        saved.setAmount(BigDecimal.valueOf(500));

        when(budgetService.createBudget(any())).thenReturn(Mono.just(saved));

        String body = """
                {
                  "query": "mutation{ createBudget(userId:\\"user123\\", categoryId:10, year:2026, month:4, amount:500){ id userId year month amount } }"
                }
                """;

        webTestClient.post()
                .uri("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.createBudget.userId").isEqualTo("user123");
    }

    @Test
    void getBudgetById_queryReturnsBudget() {
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setUserId("user123");
        budget.setCategoryId(10L);
        budget.setYear(2026);
        budget.setMonth(4);
        budget.setAmount(BigDecimal.valueOf(500));

        when(budgetService.getBudgetById(anyLong(), anyString())).thenReturn(Mono.just(budget));

        String body = """
                {
                  "query": "query{ getBudgetById(id:1, userId:\\"user123\\"){ id userId year month } }"
                }
                """;

        webTestClient.post()
                .uri("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.getBudgetById.userId").isEqualTo("user123");
    }
}
