package com.expenses_traker.db_service.Repository;

import com.expenses_traker.db_service.Entity.EMIAccountEntity;
import com.expenses_traker.db_service.pojo.Accounts.EMIAccountPojo;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmiAccountDetailsRepo extends ReactiveCrudRepository<EMIAccountEntity, Long> {

}

