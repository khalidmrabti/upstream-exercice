package com.exercice.exercice.service;

import com.exercice.exercice.dao.ITransactionRepository;
import com.exercice.exercice.exceptions.TransactionException;
import com.exercice.exercice.model.PaymentStatus;
import com.exercice.exercice.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class TransactionService {


    private final ITransactionRepository transactionRepository;


    public Mono<Transaction> createTransaction(Transaction transaction) {
        // Apply business logic and validation if needed
        transaction.setStatus(PaymentStatus.NEW);
        return transactionRepository.save(transaction);
    }


    private Mono<Void> paymentStatusGuard(Transaction updatedTransaction, Transaction existingTransaction) {
        if (existingTransaction.getStatus() == PaymentStatus.CAPTURED) {
            return Mono.error(TransactionException.cannotUpdateCaptured());
        }

        if (updatedTransaction.getStatus() == PaymentStatus.CAPTURED && existingTransaction.getStatus() != PaymentStatus.AUTHORIZED) {
            return Mono.error(TransactionException.cannotCaptureUnAuthorized());
        }

        return Mono.empty();
    }

    public Mono<Transaction> updateTransaction(String transactionId, Transaction updatedTransaction) {
        Mono<Transaction> existingTransaction = getTransactionById(transactionId)
                .switchIfEmpty(Mono.error(TransactionException.cannotFind(transactionId)));

        return existingTransaction
                .flatMap(transaction -> paymentStatusGuard(updatedTransaction, transaction))
                .then(existingTransaction)
                .doOnSuccess(transaction -> {
                    // Update the relevant fields
                    transaction.setAmount(updatedTransaction.getAmount());
                    transaction.setPaymentType(updatedTransaction.getPaymentType());
                    transaction.setStatus(updatedTransaction.getStatus());
                })
                .flatMap(transactionRepository::save);
    }


    public Mono<Transaction> getTransactionById(String transactionId) {
        return transactionRepository.findById(transactionId);
    }

    public Flux<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Mono<Void> deleteTransaction(String transactionId) {
        return getTransactionById(transactionId)
                .switchIfEmpty(Mono.error(TransactionException.cannotFind(transactionId)))
                .flatMap(existingTransaction -> {
                    if (existingTransaction.getStatus() == PaymentStatus.CAPTURED) {
                        return Mono.error(TransactionException.cannotDeleteCaptured());
                    }
                    return transactionRepository.deleteById(transactionId);
                });
    }




}
