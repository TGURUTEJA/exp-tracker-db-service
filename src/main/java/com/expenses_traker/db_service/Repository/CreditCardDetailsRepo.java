package com.expenses_traker.db_service.Repository;

import com.expenses_traker.db_service.Entity.CreditCardEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardDetailsRepo extends ReactiveCrudRepository<CreditCardEntity, Long> {
}
