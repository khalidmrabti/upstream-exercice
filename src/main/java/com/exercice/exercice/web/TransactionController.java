package com.exercice.exercice.web;

import com.exercice.exercice.exceptions.TransactionException;
import com.exercice.exercice.model.PaymentType;
import com.exercice.exercice.model.Transaction;
import com.exercice.exercice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

        private final TransactionService transactionService;

        @Autowired
        public TransactionController(TransactionService transactionService) {
            this.transactionService = transactionService;
        }

        // Create a new transaction
        @PostMapping
        public ResponseEntity<Mono<Transaction>> createTransaction(@RequestBody Transaction transaction) {
            Mono<Transaction> createdTransaction = transactionService.createTransaction(transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
        }

        // Update an existing transaction
        @PutMapping("/{transactionId}")
        public ResponseEntity<Mono<Transaction>> updateTransaction(@PathVariable String transactionId, @RequestBody Transaction transaction) {
            Mono<Transaction> updatedTransaction = transactionService.updateTransaction(transactionId, transaction);
            return ResponseEntity.ok(updatedTransaction);
        }

        // Get a transaction by ID
        @GetMapping("/{transactionId}")
        public Mono<ResponseEntity<Transaction>> getTransactionById(@PathVariable String transactionId) {
            return transactionService.getTransactionById(transactionId)
                    .map(ResponseEntity::ok)
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        }

        // Get all transactions
        @GetMapping
        public ResponseEntity<Flux<Transaction>> getAllTransactions() {
            Flux<Transaction> transactions = transactionService.getAllTransactions();
            return ResponseEntity.ok(transactions);
        }

        // Delete a transaction by ID
        @DeleteMapping("/{transactionId}")
        public Mono<ResponseEntity<Void>> deleteTransaction(@PathVariable String transactionId) {
            return transactionService.deleteTransaction(transactionId)
                    .thenReturn(ResponseEntity.noContent().<Void>build())
                    .onErrorResume(error -> {
                        error.printStackTrace();
                        if (error instanceof TransactionException) {
                            return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build());
                        }
                        return Mono.just(ResponseEntity.notFound().build());
                    });
        }
}



