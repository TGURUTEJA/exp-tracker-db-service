package com.expenses_traker.db_service.Repository;

import com.expenses_traker.db_service.Entity.Transaction;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepo extends ReactiveCrudRepository<Transaction, Long> {

    // All transactions for a user
    Flux<Transaction> findByUserId(String userId);

    // All transactions for a specific account
    Flux<Transaction> findByAccountId(Long accountId);

    // All credit/debit transactions
    Flux<Transaction> findByUserIdAndDebitCredit(String userId, String debitCredit);

    // get transactions by category
    Flux<Transaction> findByUserIdAndCategoryId(String userId, Long categoryId);

    // Latest transactions first
    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY created DESC LIMIT :limit")
    Flux<Transaction> findLatestTransactions(String userId, int limit);

    // Custom date range
    @Query("""
        SELECT * FROM transactions 
        WHERE user_id = :userId 
        AND created BETWEEN :start AND :end
        ORDER BY created DESC
    """)
    Flux<Transaction> findByUserIdAndDateRange(String userId, String start, String end);
}
