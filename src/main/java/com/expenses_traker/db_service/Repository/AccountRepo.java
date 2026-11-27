package com.expenses_traker.db_service.Repository;

import com.expenses_traker.db_service.Entity.AccountEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepo extends R2dbcRepository<AccountEntity, Long> {
    Flux<AccountEntity> findByUserId(String userId);

    @Query("SELECT * FROM accounts WHERE id = :id AND user_id = :userId FOR UPDATE")
    Mono<AccountEntity> findByIdAndUserIdForUpdate(@Param("id") Long id, @Param("userId") String userId);
}
