package com.expenses_traker.db_service.Repository;

import com.expenses_traker.db_service.Entity.CategoryEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface CategoryRepo extends R2dbcRepository<CategoryEntity, Long> {
    Flux<CategoryEntity> findByUserId(String userId);
    Mono<CategoryEntity> findByIdAndUserId(Long id, String userId);

    Mono<Boolean> existsByUserIdAndName(String userId, String name);

    Mono<Boolean> existsByUserIdAndNameAndIdNot(String userId, String name, Long id);
}
