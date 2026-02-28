package com.expenses_traker.db_service.Repository;

import com.expenses_traker.db_service.Entity.AccountEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface AccountRepo extends R2dbcRepository<AccountEntity, Long> {
    Flux<AccountEntity> findByUserId(String userId);
}
