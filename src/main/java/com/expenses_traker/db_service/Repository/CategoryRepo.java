package com.expenses_traker.db_service.Repository;

import com.expenses_traker.db_service.Entity.CategoryEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface CategoryRepo extends R2dbcRepository<CategoryEntity, Long> {
    Flux<CategoryEntity> findByUserId(String userId);
}
