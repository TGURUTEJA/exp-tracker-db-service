package com.expenses_traker.db_service.Repository;

import com.expenses_traker.db_service.Entity.InvestmentAccountEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestmentAccountDetailsRepo extends ReactiveCrudRepository<InvestmentAccountEntity,Long> {
}
