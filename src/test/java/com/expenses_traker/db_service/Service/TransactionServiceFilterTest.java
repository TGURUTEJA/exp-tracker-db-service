package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.AccountEntity;
import com.expenses_traker.db_service.Entity.CategoryEntity;
import com.expenses_traker.db_service.Entity.Transaction;
import com.expenses_traker.db_service.Repository.AccountRepo;
import com.expenses_traker.db_service.Repository.CategoryRepo;
import com.expenses_traker.db_service.Repository.CreditCardDetailsRepo;
import com.expenses_traker.db_service.Repository.InvestmentAccountDetailsRepo;
import com.expenses_traker.db_service.Repository.TransactionRepo;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.pojo.TransactionPojo;
import com.expenses_traker.db_service.util.ResponseHealper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransactionServiceFilterTest {

    private TransactionRepo transactionRepo;
    private R2dbcEntityTemplate template;
    private AccountRepo accountRepo;
    private CategoryRepo categoryRepo;
    private TransactionalOperator transactionalOperator;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionRepo = mock(TransactionRepo.class);
        template = mock(R2dbcEntityTemplate.class);
        accountRepo = mock(AccountRepo.class);
        categoryRepo = mock(CategoryRepo.class);
        transactionalOperator = mock(TransactionalOperator.class);
        doAnswer(invocation -> invocation.getArgument(0))
                .when(transactionalOperator).transactional(any(Mono.class));
        transactionService = new TransactionService(
                new ResponseHealper(),
                transactionRepo,
                template,
                accountRepo,
                categoryRepo,
                mock(CreditCardDetailsRepo.class),
                mock(InvestmentAccountDetailsRepo.class),
                transactionalOperator
        );
    }

    @Test
    void getTransactionHistoryByUserIdAppliesOptionalFilters() {
        Transaction transaction = new Transaction();
        when(template.select(any(Query.class), eq(Transaction.class)))
                .thenReturn(Flux.just(transaction));

        StepVerifier.create(transactionService.getTransactionHistoryByUserId(
                        "user123",
                        "2026-11-01T00:00:00",
                        "2026-11-30T23:59:59",
                        17.0,
                        3
                ))
                .expectNext(transaction)
                .verifyComplete();

        verify(template).select(any(Query.class), eq(Transaction.class));
        verifyNoInteractions(transactionRepo);
    }

    @Test
    void getTransactionHistoryByUserIdRejectsInvalidDateRange() {
        StepVerifier.create(transactionService.getTransactionHistoryByUserId(
                        "user123",
                        "2026-12-01T00:00:00",
                        "2026-11-30T23:59:59",
                        null,
                        null
                ))
                .expectErrorMessage("startDate must not be after endDate")
                .verify();

        verifyNoInteractions(template, transactionRepo);
    }

    @Test
    void recalculateSnapshotsReplaysOrderedBalanceDeltas() {
        Transaction first = transaction("credit", 100);
        Transaction backdated = transaction("credit", 10);
        Transaction laterDebit = transaction("debit", 20);
        Transaction latest = transaction("credit", 50);

        TransactionService.recalculateSnapshots(
                List.of(first, backdated, laterDebit, latest),
                BigDecimal.valueOf(140)
        );

        assertEquals(BigDecimal.valueOf(100), first.getAccountBalance());
        assertEquals(BigDecimal.valueOf(110), backdated.getAccountBalance());
        assertEquals(BigDecimal.valueOf(90), laterDebit.getAccountBalance());
        assertEquals(BigDecimal.valueOf(140), latest.getAccountBalance());
    }

    @Test
    void createTransactionRejectsAccountOutsideUserScope() {
        when(accountRepo.findByIdAndUserIdForUpdate(17L, "user123")).thenReturn(Mono.empty());

        StepVerifier.create(transactionService.createTransaction("user123", validInput()))
                .assertNext(response -> {
                    assertTrue(response.isError());
                    assertTrue(response.getMessage().contains("Account not found for user"));
                })
                .verifyComplete();

        verify(transactionRepo, never()).save(any(Transaction.class));
    }

    @Test
    void createTransactionRejectsCategoryOwnedByAnotherUser() {
        AccountEntity account = new AccountEntity();
        account.setId(17L);
        account.setUserId("user123");
        account.setType("BANK");
        account.setBalance(BigDecimal.valueOf(100));
        when(accountRepo.findByIdAndUserIdForUpdate(17L, "user123")).thenReturn(Mono.just(account));
        CategoryEntity category = new CategoryEntity();
        category.setId(3L);
        category.setUserId("other-user");
        when(categoryRepo.findById(3L)).thenReturn(Mono.just(category));

        TransactionPojo input = validInput();
        input.setCategoryId(3);

        StepVerifier.create(transactionService.createTransaction("user123", input))
                .assertNext(response -> {
                    assertTrue(response.isError());
                    assertTrue(response.getMessage().contains("Category not found for user"));
                })
                .verifyComplete();

        verify(transactionRepo, never()).save(any(Transaction.class));
    }

    @Test
    void createDebitReducesAccountBalance() {
        AccountEntity account = account("user123", BigDecimal.valueOf(100));
        AtomicReference<Transaction> persistedTransaction = new AtomicReference<>();
        when(accountRepo.findByIdAndUserIdForUpdate(17L, "user123")).thenReturn(Mono.just(account));
        when(accountRepo.save(account)).thenReturn(Mono.just(account));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId(41L);
            persistedTransaction.set(saved);
            return Mono.just(saved);
        });
        when(transactionRepo.findByAccountIdOrderByCreatedAscIdAsc(17L))
                .thenAnswer(invocation -> Flux.defer(() -> Mono.justOrEmpty(persistedTransaction.get())));
        when(transactionRepo.findByIdAndUserIdForUpdate(41L, "user123"))
                .thenAnswer(invocation -> Mono.just(persistedTransaction.get()));
        stubSaveAll();

        StepVerifier.create(transactionService.createTransaction("user123", validInput()))
                .assertNext(response -> {
                    assertTrue(!response.isError());
                    assertEquals(0, response.getData().getAccountBalance().compareTo(BigDecimal.valueOf(90)));
                })
                .verifyComplete();

        assertEquals(0, account.getBalance().compareTo(BigDecimal.valueOf(90)));
        verify(transactionRepo).saveAll(org.mockito.ArgumentMatchers.<Iterable<Transaction>>any());
        verify(accountRepo).save(account);
    }

    @Test
    void updateReversesOldDeltaBeforeApplyingNewDirection() {
        AccountEntity account = account("user123", BigDecimal.valueOf(90));
        Transaction existing = transaction("debit", 10);
        existing.setId(41L);
        existing.setUserId("user123");
        existing.setAccountId(17L);
        existing.setCreated(LocalDateTime.of(2026, 11, 1, 10, 0));
        when(transactionRepo.findByIdAndUserIdForUpdate(41L, "user123"))
                .thenReturn(Mono.just(existing), Mono.just(existing));
        when(accountRepo.findByIdAndUserIdForUpdate(17L, "user123")).thenReturn(Mono.just(account));
        when(accountRepo.save(account)).thenReturn(Mono.just(account));
        when(transactionRepo.save(existing)).thenReturn(Mono.just(existing));
        when(transactionRepo.findByAccountIdOrderByCreatedAscIdAsc(17L)).thenReturn(Flux.just(existing));
        stubSaveAll();

        TransactionPojo input = validInput();
        input.setDebitCredit("credit");
        input.setAmount(BigDecimal.valueOf(20));

        StepVerifier.create(transactionService.updateTransaction(41L, "user123", input))
                .assertNext(response -> assertTrue(!response.isError()))
                .verifyComplete();

        assertEquals(0, account.getBalance().compareTo(BigDecimal.valueOf(120)));
        assertEquals("credit", existing.getDebitCredit());
        assertEquals(0, existing.getAccountBalance().compareTo(BigDecimal.valueOf(120)));
    }

    @Test
    void deleteReversesTransactionDeltaFromAccountBalance() {
        AccountEntity account = account("user123", BigDecimal.valueOf(90));
        Transaction existing = transaction("debit", 10);
        existing.setId(41L);
        existing.setUserId("user123");
        existing.setAccountId(17L);
        when(transactionRepo.findByIdAndUserIdForUpdate(41L, "user123")).thenReturn(Mono.just(existing));
        when(accountRepo.findByIdAndUserIdForUpdate(17L, "user123")).thenReturn(Mono.just(account));
        when(accountRepo.save(account)).thenReturn(Mono.just(account));
        when(transactionRepo.delete(existing)).thenReturn(Mono.empty());
        when(transactionRepo.findByAccountIdOrderByCreatedAscIdAsc(17L)).thenReturn(Flux.empty());

        StepVerifier.create(transactionService.deleteTransaction(41L, "user123"))
                .assertNext(response -> {
                    assertTrue(!response.isError());
                    assertEquals(Boolean.TRUE, response.getData());
                })
                .verifyComplete();

        assertEquals(0, account.getBalance().compareTo(BigDecimal.valueOf(100)));
    }

    @Test
    void deleteTransactionLooksUpTransactionWithinUserScope() {
        when(transactionRepo.findByIdAndUserIdForUpdate(9L, "user123")).thenReturn(Mono.empty());

        StepVerifier.create(transactionService.deleteTransaction(9L, "user123"))
                .assertNext(APIResponse::isError)
                .verifyComplete();

        verify(transactionRepo).findByIdAndUserIdForUpdate(9L, "user123");
    }

    private TransactionPojo validInput() {
        TransactionPojo input = new TransactionPojo();
        input.setAccountId(17.0);
        input.setAmount(BigDecimal.TEN);
        input.setDebitCredit("debit");
        return input;
    }

    private AccountEntity account(String userId, BigDecimal balance) {
        AccountEntity account = new AccountEntity();
        account.setId(17L);
        account.setUserId(userId);
        account.setType("BANK");
        account.setBalance(balance);
        return account;
    }

    private void stubSaveAll() {
        when(transactionRepo.saveAll(org.mockito.ArgumentMatchers.<Iterable<Transaction>>any()))
                .thenAnswer(invocation -> Flux.fromIterable(invocation.getArgument(0)));
    }

    private Transaction transaction(String direction, long amount) {
        Transaction transaction = new Transaction();
        transaction.setDebitCredit(direction);
        transaction.setAmount(BigDecimal.valueOf(amount));
        return transaction;
    }
}
