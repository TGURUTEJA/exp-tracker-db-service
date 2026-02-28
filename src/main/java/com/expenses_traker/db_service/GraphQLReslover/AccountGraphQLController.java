package com.expenses_traker.db_service.GraphQLReslover;

import com.expenses_traker.db_service.Entity.AccountEntity;
import com.expenses_traker.db_service.Service.AccountService;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.pojo.Accounts.AccountDetails;
import com.expenses_traker.db_service.pojo.IDInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AccountGraphQLController {

    private final AccountService accountService;

    //Create Account
    @MutationMapping
    public Mono<APIResponse<AccountDetails>> createAccount(@Argument AccountDetails body){
        return accountService.createAccount(body);
    }

    //Get Account by account id
    @QueryMapping
    public Mono<APIResponse<AccountDetails>> getAccountById(@Argument Long id){
        return accountService.getAccountById(id);
    }

    //Get Accounts by user id
    @QueryMapping
    public Mono<APIResponse<List<AccountDetails>>> getAccountsByUserId(@Argument IDInput input) {
        return accountService.getAccountsByUserId(input.getId());
    }

    //Update Account
    @MutationMapping
    public Mono<APIResponse<AccountDetails>> updateAccount(@Argument Long id, @Argument AccountDetails body) {
        return accountService.updateAccount(id, body);
    }

    //Delete Account
    @MutationMapping
    public Mono<APIResponse<Boolean>> deleteAccount(@Argument Long id) {
        return accountService.deleteAccount(id);
    }

}
