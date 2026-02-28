package com.expenses_traker.db_service.Controller;

import com.expenses_traker.db_service.Entity.Transaction;
import com.expenses_traker.db_service.Service.TransactionService;
import com.expenses_traker.db_service.pojo.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/DB/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/test")
    public String testTransaction() {
        return "Transaction service is up and running!";
    }
    @GetMapping("/userId")
    public Flux<APIResponse<Transaction>> TransactionUser(@RequestBody String userId) {
        return transactionService.getTransactionsByUserId(userId);

    }
    @GetMapping("/ID")
    public Mono<APIResponse<Transaction>> TransactionID(@RequestBody Long id) {
        return transactionService.getTransactionById(id);
    }
    @GetMapping("/accountId")
    public Flux<APIResponse<Transaction>> TransactionAccount(@RequestBody Long accountId) {
        return transactionService.getTransactionsByAccountId(accountId);
    }

    
}
