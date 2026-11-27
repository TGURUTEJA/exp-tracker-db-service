package com.expenses_traker.db_service.GraphQLReslover;


import com.expenses_traker.db_service.Service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import com.expenses_traker.db_service.Entity.Transaction;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.pojo.TransactionPojo;
import org.springframework.graphql.data.method.annotation.Argument;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class TransactionGraphQLController {
     private final TransactionService transactionService;

     @QueryMapping
     public Flux<Transaction> getTransactionsByUserId(
             @Argument("userId") String userId,
             @Argument("startDate") String startDate,
             @Argument("endDate") String endDate,
             @Argument("accountId") Double accountId,
             @Argument("categoryId") Integer categoryId,
             @Argument("newestFirst") Boolean newestFirst
     ) {
         if (startDate == null && endDate == null && accountId == null && categoryId == null
                 && !Boolean.TRUE.equals(newestFirst)) {
             return transactionService.getTransactionHistoryByUserId(userId);
         }
         return transactionService.getTransactionHistoryByUserId(
                 userId, startDate, endDate, accountId, categoryId
         );
     }

     @MutationMapping
     public Mono<APIResponse<Transaction>> createTransaction(
             @Argument("userId") String userId,
             @Argument("input") TransactionPojo input
     ) {
            return transactionService.createTransaction(userId, input);
     }

     @MutationMapping
    public Mono<APIResponse<Transaction>> updateTransaction(
            @Argument Long id,
            @Argument("userId") String userId,
            @Argument("input") TransactionPojo input
    ) {
        return transactionService.updateTransaction(id, userId, input);
    }

    @MutationMapping
    public Mono<APIResponse<Boolean>> deleteTransaction(
            @Argument Long id,
            @Argument("userId") String userId
    ) {
        return transactionService.deleteTransaction(id, userId);
    }


}
