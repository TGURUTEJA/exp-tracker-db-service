package com.expenses_traker.db_service.GraphQLReslover;


import com.expenses_traker.db_service.Service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import com.expenses_traker.db_service.Entity.Transaction;
import com.expenses_traker.db_service.pojo.APIResponse;
import org.springframework.graphql.data.method.annotation.Argument;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class TransactionGraphQLController {
     private final TransactionService transactionService;

     @MutationMapping
     public Mono<APIResponse<Transaction>> createTransaction(@Argument("input") Transaction input) {
            return transactionService.createTransaction(input);
     }

     @MutationMapping
    public Mono<APIResponse<Transaction>> updateTransaction(@Argument Long id, @Argument("input") Transaction input) {
        return transactionService.updateTransaction(id, input);
    }

    @MutationMapping
    public Mono<APIResponse<Boolean>> deleteTransaction(@Argument Long id) {
        return transactionService.deleteTransaction(id);
    }


}
