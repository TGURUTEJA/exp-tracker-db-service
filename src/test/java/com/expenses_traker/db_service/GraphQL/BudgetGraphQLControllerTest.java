package com.expenses_traker.db_service.GraphQL;

import com.expenses_traker.db_service.Entity.Budget;
import com.expenses_traker.db_service.Service.BudgetService;
import com.expenses_traker.db_service.pojo.APIResponse;
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
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class BudgetGraphQLControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BudgetService budgetService;

    @Test
    void createBudget_mutationReturnsApiResponse() {
        Budget saved = new Budget();
        saved.setId(1L);
        saved.setUserId("1");
        saved.setCategoryId(10L);
        saved.setYear(2026);
        saved.setMonth(4);
        saved.setAmount(BigDecimal.valueOf(500));

        APIResponse<Budget> api = new APIResponse<>();
        api.setError(false);
        api.setMessage("ok");
        api.setData(saved);

        when(budgetService.createBudget(any(Budget.class))).thenReturn(Mono.just(api));

        String body = """
                {
                  "query": "mutation($input: BudgetInput!){ createBudget(input:$input){ error message data { id userId year month } } }",
                  "variables": { "input": { "userId":"1", "categoryId":10, "year":2026, "month":4, "amount":500 } }
                }
                """;

        webTestClient.post()
                .uri("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.createBudget.error").isEqualTo(false)
                .jsonPath("$.data.createBudget.data.userId").isEqualTo("1");
    }
}

