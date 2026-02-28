package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.Transaction;
import com.expenses_traker.db_service.Repository.TransactionRepo;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.util.ResponseHealper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class TransactionService {
    private final ResponseHealper responseHealper;
    private final TransactionRepo transactionRepo;

    // Create a transaction
    public Mono<APIResponse<Transaction>> createTransaction(Transaction transaction) {
        return transactionRepo.save(transaction)
                .map(savedTransaction -> responseHealper.buildSuccessResponse(savedTransaction, "Transaction created successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to create transaction: " + e.getMessage())));
    }

    //Get transaction by id
    public Mono<APIResponse<Transaction>> getTransactionById(Long id) {
        return transactionRepo.findById(id)
                .map(transaction -> responseHealper.buildSuccessResponse(transaction, "Transaction fetched successfully"))
                .defaultIfEmpty(responseHealper.buildErrorResponse("Transaction not found"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to fetch transaction: " + e.getMessage())));
    }

    // Get by user id
    public Flux<APIResponse<Transaction>> getTransactionsByUserId(String userId) {
        return transactionRepo.findByUserId(userId)
                .map(transactions -> responseHealper.buildSuccessResponse(transactions, "Transactions fetched successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to fetch transactions: " + e.getMessage())));
    }

    //Get by Account id
    public Flux<APIResponse<Transaction>> getTransactionsByAccountId(Long accountId) {
        return transactionRepo.findByAccountId(accountId)
                .map(transactions -> responseHealper.buildSuccessResponse(transactions, "Transactions fetched successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to fetch transactions: " + e.getMessage())));
    }

    //get by credit/debit
    public Flux<APIResponse<Transaction>> getTransactionsByDebitCredit(String userId, String debitCredit) {
        return transactionRepo.findByUserIdAndDebitCredit(userId, debitCredit)
                .map(transactions -> responseHealper.buildSuccessResponse(transactions, "Transactions fetched successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to fetch transactions: " + e.getMessage())));
    }

    // get transactions by category
    public Flux<APIResponse<Transaction>> getTransactionsByCategory(String userId, Long categoryId) {
        return transactionRepo.findByUserIdAndCategoryId(userId, categoryId)
                .map(transactions -> responseHealper.buildSuccessResponse(transactions, "Transactions fetched successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to fetch transactions: " + e.getMessage())));
    }

    // get latest transactions
    public Flux<APIResponse<Transaction>> getLatestTransactions(String userId, int limit) {
        return transactionRepo.findLatestTransactions(userId,limit)
                .map(transactions -> responseHealper.buildSuccessResponse(transactions, "Transactions fetched successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to fetch transactions: " + e.getMessage())));
    }

    // get by date range
    public Flux<APIResponse<Transaction>> getTransactionsByDateRange(String userId, String start, String end) {
        return transactionRepo.findByUserIdAndDateRange(userId, start, end)
                .map(transactions -> responseHealper.buildSuccessResponse(transactions, "Transactions fetched successfully"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to fetch transactions: " + e.getMessage())));
    }


    //Update transaction by id
    public Mono<APIResponse<Transaction>> updateTransaction(Long id, Transaction updatedTransaction) {
        return transactionRepo.findById(id)
                .flatMap(existingTransaction -> {
                    existingTransaction.setAccountId(updatedTransaction.getAccountId());
                    existingTransaction.setDebitCredit(updatedTransaction.getDebitCredit());
                    existingTransaction.setAmount(updatedTransaction.getAmount());
                    existingTransaction.setCategoryId(updatedTransaction.getCategoryId());
                    existingTransaction.setDescription(updatedTransaction.getDescription());
                    existingTransaction.setCreated(updatedTransaction.getCreated());
                    existingTransaction.setAccountBalance(updatedTransaction.getAccountBalance());
                    return transactionRepo.save(existingTransaction);
                })
                .map(savedTransaction -> responseHealper.buildSuccessResponse(savedTransaction, "Transaction updated successfully"))
                .defaultIfEmpty(responseHealper.buildErrorResponse("Transaction not found"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to update transaction: " + e.getMessage())));
    }

    // Delete transaction by id
    public Mono<APIResponse<Boolean>> deleteTransaction(Long id) {
        return transactionRepo.findById(id)
                .flatMap(existingTransaction ->
                        transactionRepo.delete(existingTransaction)
                                .then(Mono.just(responseHealper.buildSuccessResponse(true, "Transaction deleted successfully")))
                )
                .defaultIfEmpty(responseHealper.buildErrorResponse("Transaction not found"))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to delete transaction: " + e.getMessage())));
    }



}
