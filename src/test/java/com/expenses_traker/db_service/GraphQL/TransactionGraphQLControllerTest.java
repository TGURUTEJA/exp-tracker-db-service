package com.expenses_traker.db_service.GraphQL;

import com.expenses_traker.db_service.Entity.Transaction;
import com.expenses_traker.db_service.Service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TransactionGraphQLControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void getTransactionsByUserId_returnsAccountBalanceSnapshots() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUserId("user123");
        transaction.setAccountId(17L);
        transaction.setAmount(BigDecimal.valueOf(25));
        transaction.setDebitCredit("DEBIT");
        transaction.setCreated(LocalDateTime.of(2026, 11, 1, 10, 30));
        transaction.setAccountBalance(BigDecimal.valueOf(975));
        when(transactionService.getTransactionHistoryByUserId("user123"))
                .thenReturn(Flux.just(transaction));

        String body = """
                {
                  "query": "query{ getTransactionsByUserId(userId:\\"user123\\"){ id accountId amount debitCredit created accountBalance } }"
                }
                """;

        webTestClient.post()
                .uri("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.getTransactionsByUserId[0].accountId").isEqualTo(17.0)
                .jsonPath("$.data.getTransactionsByUserId[0].accountBalance").isEqualTo(975.0);
    }

}
