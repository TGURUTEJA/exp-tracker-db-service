package com.expenses_traker.db_service.Service;

import com.expenses_traker.db_service.Entity.AccountEntity;
import com.expenses_traker.db_service.Entity.*;
import com.expenses_traker.db_service.Repository.*;
import com.expenses_traker.db_service.pojo.APIResponse;
import com.expenses_traker.db_service.pojo.Accounts.AccountDetails;
import com.expenses_traker.db_service.pojo.Accounts.AccountExtraData;
import com.expenses_traker.db_service.util.ResponseHealper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepo accountRepo;
    private final CreditCardDetailsRepo creditRepo;
    private final EmiAccountDetailsRepo emiRepo;
    private final InvestmentAccountDetailsRepo investRepo;
    private final FriendAccountDetailsRepo friendRepo;
    private final ResponseHealper responseHealper;
    private final R2dbcEntityTemplate template;

    // 🟢 CREATE account (with subtype)
    public Mono<APIResponse<AccountDetails>> createAccount(AccountDetails input) {
        if (input == null) {
            return Mono.just(responseHealper.buildErrorResponse("Account data cannot be null"));
        }

        AccountEntity entity = mapToEntity(input);

        return accountRepo.save(entity)
                .flatMap(saved -> handleSubtypeCreateOrUpdate(saved.getId(), saved.getType(), input.getAdditionalData(),"CREATE")
                        .then(populateAccountDetails(saved))
                        .map(details -> responseHealper.buildSuccessResponse(details, "Account created successfully"))
                )
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to create account: " + e.getMessage())));
    }

    // 🟡 GET by ID (with subtype)
    public Mono<APIResponse<AccountDetails>> getAccountById(Long id) {
        if (id == null) {
            return Mono.just(responseHealper.buildErrorResponse("Account ID cannot be null"));
        }

        return accountRepo.findById(id)
                .flatMap(this::populateAccountDetails)
                .map(details -> responseHealper.buildSuccessResponse(details, "Account retrieved successfully"))
                .switchIfEmpty(Mono.just(responseHealper.buildErrorResponse("Account not found")))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to retrieve account: " + e.getMessage())));
    }

    // 🟣 GET all accounts for user
    public Mono<APIResponse<List<AccountDetails>>> getAccountsByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return Mono.just(responseHealper.buildErrorResponse("User ID cannot be null or empty"));
        }

        return accountRepo.findByUserId(userId)
                .flatMap(this::populateAccountDetails)
                .collectList()
                .flatMap( list -> list.isEmpty()
                        ? Mono.just(responseHealper.buildErrorResponse(list,"No Accounts found for user"))
                        : Mono.just(responseHealper.buildSuccessResponse(list, "Accounts retrieved successfully")))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to retrieve accounts: " + e.getMessage())));
    }

    // 🟠 UPDATE (base + subtype)
    public Mono<APIResponse<AccountDetails>> updateAccount(Long id, AccountDetails updates) {
        if (id == null) {
            return Mono.just(responseHealper.buildErrorResponse("Account ID cannot be null"));
        }

        return accountRepo.findById(id)
                .flatMap(existing -> {
                    if (updates.getType() != null) existing.setType(updates.getType());
                    if (updates.getDisplayName() != null) existing.setDisplayName(updates.getDisplayName());
                    if (updates.getBalance() != null) existing.setBalance(updates.getBalance());
                    if (updates.getCurrency() != null) existing.setCurrency(updates.getCurrency());
                    if (updates.getStatus() != null) existing.setStatus(updates.getStatus());

                    return accountRepo.save(existing)
                            .flatMap(saved ->
                                    handleSubtypeCreateOrUpdate(saved.getId(), saved.getType(), updates.getAdditionalData(),"UPDATE")
                                            .then(populateAccountDetails(saved))
                                            .map(details -> responseHealper.buildSuccessResponse(details, "Account updated successfully"))
                            );
                })
                .switchIfEmpty(Mono.just(responseHealper.buildErrorResponse("Account not found")))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to update account: " + e.getMessage())));
    }

    // 🔴 DELETE (base + subtype)
    public Mono<APIResponse<Boolean>> deleteAccount(Long id) {
        if (id == null) {
            return Mono.just(responseHealper.buildErrorResponse("Account ID cannot be null"));
        }

        return accountRepo.findById(id)
                .flatMap(existing ->
                        deleteSubtypeDetails(existing)
                                .then(accountRepo.delete(existing))
                                .thenReturn(responseHealper.buildSuccessResponse(true, "Account deleted successfully"))
                )
                .switchIfEmpty(Mono.just(responseHealper.buildErrorResponse("Account not found")))
                .onErrorResume(e -> Mono.just(responseHealper.buildErrorResponse("Failed to delete account: " + e.getMessage())));
    }

    // 🧩 Map DB entity to GraphQL response
    private Mono<AccountDetails> populateAccountDetails(AccountEntity entity) {
        AccountDetails details = mapToDetails(entity);
        AccountExtraData accountExtraData = new AccountExtraData();
        return switch (entity.getType()) {
            case "CREDIT_CARD" -> creditRepo.findById(entity.getId())
                    .map(add -> {accountExtraData.setCreditCard(add); details.setAdditionalData(accountExtraData); return details; })
                    .defaultIfEmpty(details);
            case "LOAN" -> emiRepo.findById(entity.getId())
                    .map(add -> {accountExtraData.setEmi(add); details.setAdditionalData(accountExtraData); return details; })
                    .defaultIfEmpty(details);
            case "INVESTMENT" -> investRepo.findById(entity.getId())
                    .map(add -> {accountExtraData.setInvestment(add); details.setAdditionalData(accountExtraData); return details; })
                    .defaultIfEmpty(details);
            case "FRIEND" -> friendRepo.findById(entity.getId())
                    .map(add -> {accountExtraData.setFriend(add); details.setAdditionalData(accountExtraData); return details; })
                    .defaultIfEmpty(details);
            default -> Mono.just(details);
        };
    }

    // 🧩 Create or update subtype dynamically
    private Mono<Void> handleSubtypeCreateOrUpdate(Long accountId, String type, AccountExtraData additionalData,String flow) {
        if (additionalData == null) return Mono.empty();

        return switch (type) {
            case "CREDIT_CARD" -> {
                CreditCardEntity cc = additionalData.getCreditCard();
                cc.setAccountId(accountId);
                yield flow.equalsIgnoreCase("UPDATE") ? creditRepo.save(cc).then() : template.insert(CreditCardEntity.class).using(cc).then();
            }
            case "LOAN" -> {
                EMIAccountEntity emi = additionalData.getEmi();
                emi.setAccountId(accountId);
                yield flow.equalsIgnoreCase("UPDATE") ? emiRepo.save(emi).then() : template.insert(EMIAccountEntity.class).using(emi).then();
            }
            case "INVESTMENT" -> {
                InvestmentAccountEntity inv = additionalData.getInvestment();
                inv.setAccountId(accountId);
                yield flow.equalsIgnoreCase("UPDATE") ? investRepo.save(inv).then() : template.insert(InvestmentAccountEntity.class).using(inv).then();
            }
            case "FRIEND" -> {
                FriendAccountEntity fr = additionalData.getFriend();
                fr.setAccountId(accountId);
                yield flow.equalsIgnoreCase("UPDATE") ? friendRepo.save(fr).then() : template.insert(FriendAccountEntity.class).using(fr).then();
            }
            default -> Mono.empty();
        };
    }

    // 🧩 Delete subtype
    private Mono<Void> deleteSubtypeDetails(AccountEntity acc) {
        return switch (acc.getType()) {
            case "CREDIT_CARD" -> creditRepo.deleteById(acc.getId());
            case "LOAN" -> emiRepo.deleteById(acc.getId());
            case "INVESTMENT" -> investRepo.deleteById(acc.getId());
            case "FRIEND" -> friendRepo.deleteById(acc.getId());
            default -> Mono.empty();
        };
    }

    // 🧩 Mapper: AccountDetails → AccountEntity
    private AccountEntity mapToEntity(AccountDetails details) {
        AccountEntity entity = new AccountEntity();
        entity.setId(details.getId());
        entity.setUserId(details.getUserId());
        entity.setType(details.getType());
        entity.setDisplayName(details.getDisplayName());
        entity.setBalance(details.getBalance());
        entity.setCurrency(details.getCurrency());
        entity.setStatus(details.getStatus());
        entity.setCreatedAt(details.getCreatedAt());
        return entity;
    }

    // 🧩 Mapper: AccountEntity → AccountDetails
    private AccountDetails mapToDetails(AccountEntity entity) {
        AccountDetails details = new AccountDetails();
        details.setId(entity.getId());
        details.setUserId(entity.getUserId());
        details.setType(entity.getType());
        details.setDisplayName(entity.getDisplayName());
        details.setBalance(entity.getBalance());
        details.setCurrency(entity.getCurrency());
        details.setStatus(entity.getStatus());
        details.setCreatedAt(entity.getCreatedAt());
        return details;
    }
}
