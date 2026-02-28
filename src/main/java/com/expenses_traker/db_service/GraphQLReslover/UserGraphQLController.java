package com.expenses_traker.db_service.GraphQLReslover;

import com.expenses_traker.db_service.Entity.UserDetailsEntity;
import com.expenses_traker.db_service.Service.UserDetailsService;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.pojo.UserInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class UserGraphQLController {

    private final UserDetailsService userService;

    @QueryMapping
    public Mono<APIResponse<List<UserDetailsEntity>>> getAllUsers() {
        return userService.getAllUsers();
    }

    @QueryMapping
    public  Mono<APIResponse<UserDetailsEntity>> getUserById(@Argument Map<String,String> input) {
        System.out.println("THis is controller "+input.get("id"));
        return userService.getUserById(input.get("id"));
    }

    @MutationMapping
    public  Mono<APIResponse<UserDetailsEntity>> createUser(@Argument UserInput input) {
        UserDetailsEntity user = new UserDetailsEntity();
        user.setId(input.getId());
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setDob(input.getDob());
        System.out.println("THis is controller "+Thread.currentThread().getName());
        return userService.createUser(user);
    }

    @MutationMapping
    public  Mono<APIResponse<UserDetailsEntity>> updateUser(@Argument String id, @Argument UserInput input) {
        return userService.updateUser(id, input);
    }

    @MutationMapping
    public  Mono<APIResponse<Boolean>> deleteUser(@Argument String id) {
        return userService.deleteUser(id);
    }
}
