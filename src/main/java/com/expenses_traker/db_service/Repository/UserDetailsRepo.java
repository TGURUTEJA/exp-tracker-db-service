package com.expenses_traker.db_service.Repository;


import com.expenses_traker.db_service.Entity.UserDetailsEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface UserDetailsRepo extends R2dbcRepository<UserDetailsEntity,String> {
}
