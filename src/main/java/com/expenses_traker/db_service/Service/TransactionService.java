package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.Transaction;
import com.expenses_traker.db_service.Entity.AccountEntity;
import com.expenses_traker.db_service.Entity.CreditCardEntity;
import com.expenses_traker.db_service.Entity.InvestmentAccountEntity;
import com.expenses_traker.db_service.Repository.AccountRepo;
import com.expenses_traker.db_service.Repository.CategoryRepo;
import com.expenses_traker.db_service.Repository.CreditCardDetailsRepo;
import com.expenses_traker.db_service.Repository.InvestmentAccountDetailsRepo;
import com.expenses_traker.db_service.Repository.TransactionRepo;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.pojo.TransactionPojo;
import com.expenses_traker.db_service.util.ResponseHealper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class TransactionService {
    private final ResponseHealper responseHealper;
    private final TransactionRepo transactionRepo;
    private final R2dbcEntityTemplate template;
    private final AccountRepo accountRepo;
    private final CategoryRepo categoryRepo;
    private final CreditCardDetailsRepo creditCardDetailsRepo;
    private final InvestmentAccountDetailsRepo investmentAccountDetailsRepo;
    private final TransactionalOperator transactionalOperator;

    public Mono<APIResponse<Transaction>> createTransaction(String userId, TransactionPojo input) {
        return validateInput(userId, input)
                .flatMap(validated -> lockAccount(validated.accountId(), userId)
                        .flatMap(account -> validateCategory(validated.categoryId(), userId)
                                .then(getEffectiveBalance(account))
                                .flatMap(currentBalance -> {
                                    BigDecimal updatedBalance = currentBalance.add(validated.delta());
                                    Transaction transaction = toEntity(userId, validated);
                                    return saveAccountBalance(account, updatedBalance)
                                            .then(transactionRepo.save(transaction))
                                            .flatMap(saved -> refreshAccountSnapshots(account.getId(), updatedBalance)
                                                    .then(transactionRepo.findByIdAndUserIdForUpdate(saved.getId(), userId)
                                                            .switchIfEmpty(Mono.error(new IllegalStateException(
                                                                    "Created transaction could not be reloaded"
                                                            ))))
                                                    .map(refreshed -> responseHealper.buildSuccessResponse(
                                                            refreshed, "Transaction created successfully"
                                                    )));
                                })))
                .as(transactionalOperator::transactional)
                .onErrorResume(error -> Mono.just(responseHealper.buildErrorResponse(
                        "Failed to create transaction: " + error.getMessage()
                )));
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

    public Flux<Transaction> getTransactionHistoryByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return Flux.error(new IllegalArgumentException("User ID cannot be null or empty"));
        }
        return transactionRepo.findByUserIdOrderByCreatedAscIdAsc(userId);
    }

    public Flux<Transaction> getTransactionHistoryByUserId(
            String userId,
            String startDate,
            String endDate,
            Double accountId,
            Integer categoryId
    ) {
        if (userId == null || userId.isBlank()) {
            return Flux.error(new IllegalArgumentException("User ID cannot be null or empty"));
        }

        LocalDateTime start;
        LocalDateTime end;
        try {
            start = parseDateTime(startDate, "startDate");
            end = parseDateTime(endDate, "endDate");
        } catch (IllegalArgumentException error) {
            return Flux.error(error);
        }
        if (start != null && end != null && start.isAfter(end)) {
            return Flux.error(new IllegalArgumentException("startDate must not be after endDate"));
        }
        if (accountId != null
                && (!Double.isFinite(accountId) || accountId <= 0 || accountId != Math.rint(accountId))) {
            return Flux.error(new IllegalArgumentException("Account ID must be a positive whole number"));
        }
        if (categoryId != null && categoryId <= 0) {
            return Flux.error(new IllegalArgumentException("Category ID must be positive"));
        }

        Criteria criteria = Criteria.where("userId").is(userId);
        if (start != null) {
            criteria = criteria.and("created").greaterThanOrEquals(start);
        }
        if (end != null) {
            criteria = criteria.and("created").lessThanOrEquals(end);
        }
        if (accountId != null) {
            criteria = criteria.and("accountId").is(accountId.longValue());
        }
        if (categoryId != null) {
            criteria = criteria.and("categoryId").is(categoryId.longValue());
        }

        Query query = Query.query(criteria)
                .sort(Sort.by(Sort.Order.desc("created"), Sort.Order.desc("id")));
        return template.select(query, Transaction.class);
    }

    private LocalDateTime parseDateTime(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException error) {
            throw new IllegalArgumentException(
                    fieldName + " must be an ISO local date-time", error
            );
        }
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


    public Mono<APIResponse<Transaction>> updateTransaction(
            Long id,
            String userId,
            TransactionPojo input
    ) {
        return validateTransactionId(id, userId)
                .then(transactionRepo.findByIdAndUserIdForUpdate(id, userId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Transaction not found for user"))))
                .flatMap(existing -> validateInput(userId, input)
                        .flatMap(validated -> {
                            ValidatedTransaction update = validated.created() == null
                                    ? validated.withCreated(existing.getCreated())
                                    : validated;
                            List<Long> accountIds = Stream.of(existing.getAccountId(), update.accountId())
                                    .distinct()
                                    .sorted()
                                    .toList();
                            return lockAccounts(accountIds, userId)
                                    .flatMap(accounts -> validateCategory(update.categoryId(), userId)
                                            .then(updateTransactionBalances(existing, update, accounts))
                                            .flatMap(balances -> {
                                                applyInput(existing, update);
                                                return transactionRepo.save(existing)
                                                        .flatMap(saved -> refreshAccountSnapshots(
                                                                        existing.getAccountId(),
                                                                        balances.get(existing.getAccountId())
                                                                )
                                                                .then(existing.getAccountId().equals(update.accountId())
                                                                        ? Mono.empty()
                                                                        : refreshAccountSnapshots(
                                                                                update.accountId(),
                                                                                balances.get(update.accountId())
                                                                        ))
                                                                .then(transactionRepo.findByIdAndUserIdForUpdate(saved.getId(), userId)
                                                                        .switchIfEmpty(Mono.error(new IllegalStateException(
                                                                                "Updated transaction could not be reloaded"
                                                                        ))))
                                                                .map(refreshed -> responseHealper.buildSuccessResponse(
                                                                        refreshed, "Transaction updated successfully"
                                                                )));
                                            }));
                        }))
                .as(transactionalOperator::transactional)
                .onErrorResume(error -> Mono.just(responseHealper.buildErrorResponse(
                        "Failed to update transaction: " + error.getMessage()
                )));
    }

    public Mono<APIResponse<Boolean>> deleteTransaction(Long id, String userId) {
        return validateTransactionId(id, userId)
                .then(transactionRepo.findByIdAndUserIdForUpdate(id, userId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Transaction not found for user"))))
                .flatMap(existing -> lockAccount(existing.getAccountId(), userId)
                        .flatMap(account -> getEffectiveBalance(account)
                                .flatMap(currentBalance -> {
                                    BigDecimal updatedBalance = currentBalance.subtract(signedAmount(existing));
                                    return saveAccountBalance(account, updatedBalance)
                                            .then(transactionRepo.delete(existing))
                                            .then(refreshAccountSnapshots(account.getId(), updatedBalance))
                                            .thenReturn(responseHealper.buildSuccessResponse(
                                                    true, "Transaction deleted successfully"
                                            ));
                                })))
                .as(transactionalOperator::transactional)
                .onErrorResume(error -> Mono.just(responseHealper.buildErrorResponse(
                        "Failed to delete transaction: " + error.getMessage()
                )));
    }

    private Mono<ValidatedTransaction> validateInput(String userId, TransactionPojo input) {
        try {
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            if (input == null) {
                throw new IllegalArgumentException("Transaction data cannot be null");
            }
            if (input.getAccountId() == null || !Double.isFinite(input.getAccountId())
                    || input.getAccountId() <= 0
                    || input.getAccountId() != Math.rint(input.getAccountId())
                    || input.getAccountId() > Long.MAX_VALUE) {
                throw new IllegalArgumentException("Account ID must be a positive whole number");
            }
            if (input.getAmount() == null || input.getAmount().signum() <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            BigDecimal amount = input.getAmount().setScale(2, RoundingMode.UNNECESSARY);
            if (amount.precision() > 12) {
                throw new IllegalArgumentException("Amount exceeds the supported precision");
            }
            String debitCredit = input.getDebitCredit() == null
                    ? ""
                    : input.getDebitCredit().trim().toLowerCase(java.util.Locale.ROOT);
            if (!debitCredit.equals("credit") && !debitCredit.equals("debit")) {
                throw new IllegalArgumentException("Debit/credit must be either 'debit' or 'credit'");
            }
            Long categoryId = input.getCategoryId() == null
                    ? null
                    : input.getCategoryId().longValue();
            if (categoryId != null && categoryId <= 0) {
                throw new IllegalArgumentException("Category ID must be positive");
            }
            LocalDateTime created = input.getCreated() == null || input.getCreated().isBlank()
                    ? null
                    : LocalDateTime.parse(input.getCreated());
            return Mono.just(new ValidatedTransaction(
                    input.getAccountId().longValue(),
                    amount,
                    debitCredit,
                    categoryId,
                    input.getDescription(),
                    created
            ));
        } catch (IllegalArgumentException error) {
            return Mono.error(error);
        } catch (ArithmeticException | DateTimeParseException error) {
            return Mono.error(new IllegalArgumentException(
                    "Amount must have at most two decimal places and created must be an ISO local date-time",
                    error
            ));
        }
    }

    private Mono<Void> validateTransactionId(Long id, String userId) {
        if (id == null || id <= 0) {
            return Mono.error(new IllegalArgumentException("Transaction ID must be positive"));
        }
        if (userId == null || userId.isBlank()) {
            return Mono.error(new IllegalArgumentException("User ID cannot be null or empty"));
        }
        return Mono.empty();
    }

    private Mono<AccountEntity> lockAccount(Long accountId, String userId) {
        return lockAccounts(List.of(accountId), userId).map(accounts -> accounts.get(accountId));
    }

    private Mono<Map<Long, AccountEntity>> lockAccounts(List<Long> accountIds, String userId) {
        return Flux.fromIterable(accountIds)
                .concatMap(accountId -> accountRepo.findByIdAndUserIdForUpdate(accountId, userId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                "Account not found for user"
                        ))))
                .collectMap(AccountEntity::getId);
    }

    private Mono<Void> validateCategory(Long categoryId, String userId) {
        if (categoryId == null) {
            return Mono.empty();
        }
        return categoryRepo.findById(categoryId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Category not found for user")))
                .flatMap(category -> userId.equals(category.getUserId())
                        ? Mono.empty()
                        : Mono.error(new IllegalArgumentException("Category not found for user")));
    }

    private Transaction toEntity(String userId, ValidatedTransaction input) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAccountId(input.accountId());
        transaction.setAmount(input.amount());
        transaction.setDebitCredit(input.debitCredit());
        transaction.setCategoryId(input.categoryId());
        transaction.setDescription(input.description());
        transaction.setCreated(input.created() == null ? LocalDateTime.now() : input.created());
        return transaction;
    }

    private void applyInput(Transaction transaction, ValidatedTransaction input) {
        transaction.setAccountId(input.accountId());
        transaction.setAmount(input.amount());
        transaction.setDebitCredit(input.debitCredit());
        transaction.setCategoryId(input.categoryId());
        transaction.setDescription(input.description());
        transaction.setCreated(input.created());
        transaction.setAccountBalance(null);
    }

    private Mono<Map<Long, BigDecimal>> updateTransactionBalances(
            Transaction existing,
            ValidatedTransaction updated,
            Map<Long, AccountEntity> accounts
    ) {
        Long oldAccountId = existing.getAccountId();
        Long newAccountId = updated.accountId();
        AccountEntity oldAccount = accounts.get(oldAccountId);
        AccountEntity newAccount = accounts.get(newAccountId);

        Mono<BigDecimal> oldBalanceMono = getEffectiveBalance(oldAccount);
        Mono<BigDecimal> newBalanceMono = oldAccountId.equals(newAccountId)
                ? oldBalanceMono
                : getEffectiveBalance(newAccount);

        return Mono.zip(oldBalanceMono, newBalanceMono)
                .flatMap(balances -> {
                    BigDecimal oldBalance = balances.getT1();
                    BigDecimal newBalance = balances.getT2();
                    BigDecimal oldDelta = signedAmount(existing);
                    BigDecimal newDelta = updated.delta();
                    Map<Long, BigDecimal> updatedBalances;
                    if (oldAccountId.equals(newAccountId)) {
                        updatedBalances = Map.of(oldAccountId, oldBalance.subtract(oldDelta).add(newDelta));
                    } else {
                        updatedBalances = Map.of(
                                oldAccountId, oldBalance.subtract(oldDelta),
                                newAccountId, newBalance.add(newDelta)
                        );
                    }

                    return Flux.fromIterable(updatedBalances.entrySet())
                            .concatMap(entry -> saveAccountBalance(accounts.get(entry.getKey()), entry.getValue()))
                            .then(Mono.just(updatedBalances));
                });
    }

    private Mono<BigDecimal> getEffectiveBalance(AccountEntity account) {
        BigDecimal baseBalance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
        return switch (account.getType()) {
            case "CREDIT_CARD" -> creditCardDetailsRepo.findById(account.getId())
                    .map(details -> details.getOutstanding() == null
                            ? baseBalance
                            : details.getOutstanding())
                    .defaultIfEmpty(baseBalance);
            case "INVESTMENT" -> investmentAccountDetailsRepo.findById(account.getId())
                    .map(details -> details.getCurrentValue() == null
                            ? baseBalance
                            : details.getCurrentValue())
                    .defaultIfEmpty(baseBalance);
            default -> Mono.just(baseBalance);
        };
    }

    private Mono<Void> saveAccountBalance(AccountEntity account, BigDecimal balance) {
        account.setBalance(balance);
        Mono<Void> subtypeUpdate = switch (account.getType()) {
            case "CREDIT_CARD" -> creditCardDetailsRepo.findById(account.getId())
                    .flatMap(details -> {
                        details.setOutstanding(balance);
                        return creditCardDetailsRepo.save(details);
                    })
                    .then();
            case "INVESTMENT" -> investmentAccountDetailsRepo.findById(account.getId())
                    .flatMap(details -> {
                        details.setCurrentValue(balance);
                        return investmentAccountDetailsRepo.save(details);
                    })
                    .then();
            default -> Mono.empty();
        };
        return accountRepo.save(account).then(subtypeUpdate);
    }

    private Mono<Void> refreshAccountSnapshots(Long accountId, BigDecimal currentBalance) {
        return transactionRepo.findByAccountIdOrderByCreatedAscIdAsc(accountId)
                .collectList()
                .flatMap(transactions -> {
                    if (transactions.isEmpty()) {
                        return Mono.empty();
                    }
                    recalculateSnapshots(transactions, currentBalance);
                    return transactionRepo.saveAll(transactions).then();
                });
    }

    static void recalculateSnapshots(List<Transaction> transactions, BigDecimal currentBalance) {
        BigDecimal totalDelta = transactions.stream()
                .map(TransactionService::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal runningBalance = currentBalance.subtract(totalDelta);
        for (Transaction transaction : transactions) {
            runningBalance = runningBalance.add(signedAmount(transaction));
            transaction.setAccountBalance(runningBalance);
        }
    }

    private static BigDecimal signedAmount(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        if (amount == null) {
            throw new IllegalStateException("Transaction amount is missing");
        }
        String direction = transaction.getDebitCredit();
        if ("credit".equalsIgnoreCase(direction)) {
            return amount;
        }
        if ("debit".equalsIgnoreCase(direction)) {
            return amount.negate();
        }
        throw new IllegalStateException("Transaction direction is invalid");
    }

    private record ValidatedTransaction(
            Long accountId,
            BigDecimal amount,
            String debitCredit,
            Long categoryId,
            String description,
            LocalDateTime created
    ) {
        private BigDecimal delta() {
            return debitCredit.equals("credit") ? amount : amount.negate();
        }

        private ValidatedTransaction withCreated(LocalDateTime value) {
            return new ValidatedTransaction(
                    accountId, amount, debitCredit, categoryId, description, value
            );
        }
    }
}
