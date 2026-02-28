package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.UserDetailsEntity;
import com.expenses_traker.db_service.Repository.UserDetailsRepo;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.pojo.UserInput;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsService.class);

    private final UserDetailsRepo userDetailsRepo;
    private final R2dbcEntityTemplate template;

    public Mono<APIResponse<List<UserDetailsEntity>>> getAllUsers() {
        return userDetailsRepo.findAll()
                .collectList()
                .map(users -> buildResponse(users, false, "Users retrieved successfully"));
    }

    public Mono<APIResponse<UserDetailsEntity>> getUserById(String id) {
        return userDetailsRepo.findById(id)
                .doOnNext(u -> log.debug("Retrieved user: {} on thread: {}", u, Thread.currentThread().getName()))
                .map(user -> buildResponse(user, false, "User found"))
                .defaultIfEmpty(buildResponse(null, true, "User not found"));
    }

    public Mono<APIResponse<UserDetailsEntity>> createUser(UserDetailsEntity user) {
        log.debug("Creating user: {}", user);
        return userDetailsRepo.existsById(user.getId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(buildResponse(null, true, "User already exists with ID: " + user.getId()));
                    }
                    return template.insert(UserDetailsEntity.class)
                            .using(user)
                            .map(inserted -> buildResponse(inserted, false, "User created successfully"));
                });
    }

    public Mono<APIResponse<Boolean>> deleteUser(String id) {
        return userDetailsRepo.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.just(buildResponse(false, true, "User not found"));
                    }
                    return userDetailsRepo.deleteById(id)
                            .then(Mono.fromSupplier(() -> buildResponse(true, false, "User deleted successfully")));
                });
    }

    public Mono<APIResponse<UserDetailsEntity>> updateUser(String id, UserInput input) {
        return getUserById(id)
                .flatMap(data -> {
                    if (data.isError() || data.getData() == null) {
                        return Mono.just(buildResponse(null, true, data.getMessage()));
                    }
                    UserDetailsEntity user = new UserDetailsEntity();
                    user.setId(id);
                    user.setFirstName(input.getFirstName()!=null ? input.getFirstName() : data.getData().getFirstName());
                    user.setLastName(input.getLastName()!=null ? input.getLastName() : data.getData().getLastName());
                    user.setDob(input.getDob()!=null ? input.getDob() : data.getData().getDob());
                    return userDetailsRepo.save(user)
                            .flatMap(res -> Mono.just(buildResponse(res, false, "User updated successfully"))).defaultIfEmpty(buildResponse(null, true, "Failed to update user"));
                });
    }

    private <T> APIResponse<T> buildResponse(T data, boolean error, String message) {
        APIResponse<T> response = new APIResponse<>();
        response.setData(data);
        response.setError(error);
        response.setMessage(message);
        return response;
    }
}