package com.expenses_traker.db_service.Repository;

import com.expenses_traker.db_service.Entity.AccountEntity;
import com.expenses_traker.db_service.Entity.Budget;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BudgetRepo extends ReactiveCrudRepository<Budget,Long> {
    Flux<Budget> findByUserId(String userId);
}
