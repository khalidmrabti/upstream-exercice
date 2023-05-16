package com.exercice.exercice;

import com.exercice.exercice.dao.ITransactionRepository;
import com.exercice.exercice.exceptions.TransactionException;
import com.exercice.exercice.model.OrderLine;
import com.exercice.exercice.model.PaymentStatus;
import com.exercice.exercice.model.PaymentType;
import com.exercice.exercice.model.Transaction;
import com.exercice.exercice.service.TransactionService;
import com.exercice.exercice.web.TransactionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
@SpringBootTest
public class TransactionServiceTest {

    @Mock
    private ITransactionRepository transactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionService(transactionRepository);
    }


    @Test
    public void createTransaction_ShouldSaveTransactionWithNewStatus() {
        Transaction transaction = new Transaction();
        transaction.setAmount(54.80);
        transaction.setPaymentType(PaymentType.CREDIT_CARD);

        when(transactionRepository.save(transaction)).thenReturn(Mono.just(transaction));

        Mono<Transaction> result = transactionService.createTransaction(transaction);

        StepVerifier.create(result)
                .expectNextMatches(savedTransaction -> savedTransaction.getStatus() == PaymentStatus.NEW)
                .verifyComplete();

        verify(transactionRepository, times(1)).save(transaction);
        verifyNoMoreInteractions(transactionRepository);
    }


    @Test
    public void updateTransaction_ShouldUpdateTransactionFields() {
        String transactionId = "123";
        Transaction existingTransaction = new Transaction();
        existingTransaction.setId(transactionId);
        existingTransaction.setAmount(50.00);
        existingTransaction.setPaymentType(PaymentType.CREDIT_CARD);
        existingTransaction.setStatus(PaymentStatus.AUTHORIZED);

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setAmount(60.00);
        updatedTransaction.setPaymentType(PaymentType.PAYPAL);

        when(transactionRepository.findById(transactionId)).thenReturn(Mono.just(existingTransaction));
        when(transactionRepository.save(existingTransaction)).thenReturn(Mono.just(existingTransaction));

        Mono<Transaction> result = transactionService.updateTransaction(transactionId, updatedTransaction);

        StepVerifier.create(result)
                .expectNextMatches(savedTransaction ->
                        savedTransaction.getAmount() == updatedTransaction.getAmount() &&
                                savedTransaction.getPaymentType() == updatedTransaction.getPaymentType())
                .verifyComplete();

        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, times(1)).save(existingTransaction);
        verifyNoMoreInteractions(transactionRepository);
    }


    @Test
    public void getTransactionById_ShouldReturnTransaction() {
        String transactionId = "123";
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);

        when(transactionRepository.findById(transactionId)).thenReturn(Mono.just(transaction));

        Mono<Transaction> result = transactionService.getTransactionById(transactionId);

        StepVerifier.create(result)
                .expectNext(transaction)
                .verifyComplete();

        verify(transactionRepository, times(1)).findById(transactionId);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void getAllTransactions_ShouldReturnAllTransactions() {
        Transaction transaction1 = new Transaction();
        Transaction transaction2 = new Transaction();
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        when(transactionRepository.findAll()).thenReturn(Flux.fromIterable(transactions));

        Flux<Transaction> result = transactionService.getAllTransactions();

        StepVerifier.create(result)
                .expectNext(transaction1, transaction2)
                .verifyComplete();

        verify(transactionRepository, times(1)).findAll();
        verifyNoMoreInteractions(transactionRepository);
    }
    @Test
    public void deleteTransaction_ShouldDeleteTransactionById() {
        String transactionId = "123";
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setStatus(PaymentStatus.NEW);

        when(transactionRepository.findById(transactionId)).thenReturn(Mono.just(transaction));
        when(transactionRepository.deleteById(transactionId)).thenReturn(Mono.empty());

        Mono<Void> result = transactionService.deleteTransaction(transactionId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, times(1)).deleteById(transactionId);
        verifyNoMoreInteractions(transactionRepository);
    }



    //création d'une transaction d'un montant de 54,80 EUR avec une carte bancaire et une commande contenant
    // 4 paires de gants de ski à 10 EUR pièce, et 1 bonnet en laine à 14,80EUR
    @Test
    public void createTransaction_ShouldCreateTransactionWithOrderLines() {
        // Prepare test data
        Transaction transaction = new Transaction();
        transaction.setAmount(54.80);
        transaction.setPaymentType(PaymentType.CREDIT_CARD);

        // Create order lines
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine("Gloves", 4, 10.0f));
        orderLines.add(new OrderLine("Wool Hat", 1, 14.8f));
        transaction.setOrderLines(orderLines);

        // Define the expected created transaction
        Transaction createdTransaction = new Transaction();
        createdTransaction.setId("1");
        createdTransaction.setAmount(54.80);
        createdTransaction.setPaymentType(PaymentType.CREDIT_CARD);
        createdTransaction.setStatus(PaymentStatus.NEW);
        createdTransaction.setOrderLines(orderLines);

        // Set up the mock behavior
        when(transactionRepository.save(transaction)).thenReturn(Mono.just(createdTransaction));

        // Create the transaction and verify the result
        Mono<Transaction> result = transactionService.createTransaction(transaction);

        StepVerifier.create(result)
                .expectNext(createdTransaction)
                .verifyComplete();

        verify(transactionRepository, times(1)).save(transaction);
        verifyNoMoreInteractions(transactionRepository);
    }

    //modification de la transaction en passant le statut à autorisé
    @Test
    public void updateTransaction_StatusAuthorized_ShouldUpdateTransactionStatus() {
        // Prepare test data
        String transactionId = "1";
        Transaction existingTransaction = new Transaction();
        existingTransaction.setId(transactionId);
        existingTransaction.setAmount(54.80);
        existingTransaction.setPaymentType(PaymentType.CREDIT_CARD);
        existingTransaction.setStatus(PaymentStatus.NEW);

        // Define the updated transaction
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setStatus(PaymentStatus.AUTHORIZED);

        // Define the expected updated transaction
        Transaction expectedUpdatedTransaction = new Transaction();
        expectedUpdatedTransaction.setId(transactionId);
        expectedUpdatedTransaction.setAmount(54.80);
        expectedUpdatedTransaction.setPaymentType(PaymentType.CREDIT_CARD);
        expectedUpdatedTransaction.setStatus(PaymentStatus.AUTHORIZED);

        // Set up the mock behavior
        when(transactionRepository.findById(transactionId)).thenReturn(Mono.just(existingTransaction));
        when(transactionRepository.save(existingTransaction)).thenReturn(Mono.just(expectedUpdatedTransaction));

        // Update the transaction and verify the result
        Mono<Transaction> result = transactionService.updateTransaction(transactionId, updatedTransaction);

        StepVerifier.create(result)
                .expectNext(expectedUpdatedTransaction)
                .verifyComplete();

        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, times(1)).save(existingTransaction); // Updated to use the updatedTransaction object
        verifyNoMoreInteractions(transactionRepository);
    }


    //modification de la transaction en passant le statut à capturé
    @Test
    public void updateTransaction_StatusCaptured_ShouldUpdateTransactionStatus() {
        // Prepare test data
        String transactionId = "1";
        Transaction existingTransaction = new Transaction();
        existingTransaction.setId(transactionId);
        existingTransaction.setAmount(54.80);
        existingTransaction.setPaymentType(PaymentType.CREDIT_CARD);
        existingTransaction.setStatus(PaymentStatus.AUTHORIZED);

        // Define the updated transaction
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setStatus(PaymentStatus.CAPTURED);

        // Define the expected updated transaction
        Transaction expectedUpdatedTransaction = new Transaction();
        expectedUpdatedTransaction.setId(transactionId);
        expectedUpdatedTransaction.setAmount(54.80);
        expectedUpdatedTransaction.setPaymentType(PaymentType.CREDIT_CARD);
        expectedUpdatedTransaction.setStatus(PaymentStatus.CAPTURED);

        // Set up the mock behavior
        when(transactionRepository.findById(transactionId)).thenReturn(Mono.just(existingTransaction));
        when(transactionRepository.save(existingTransaction)).thenReturn(Mono.just(expectedUpdatedTransaction));

        // Update the transaction and verify the result
        Mono<Transaction> result = transactionService.updateTransaction(transactionId, updatedTransaction);

        StepVerifier.create(result)
                .expectNext(expectedUpdatedTransaction)
                .verifyComplete();

        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, times(1)).save(existingTransaction);
        verifyNoMoreInteractions(transactionRepository);
    }

    //création d'une transaction d'un montant de 208 EUR avec PayPal et une commande contenant
    //1 vélo à 208 EUR
    @Test
    public void createTransactionWithPayPal_ShouldCreateTransactionWithOrderLines() {
        // Prepare test data
        Transaction transaction = new Transaction();
        transaction.setAmount(208.00);
        transaction.setPaymentType(PaymentType.PAYPAL);

        // Create order lines
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine("Bike", 1, 208.0f));
        transaction.setOrderLines(orderLines);

        // Define the expected created transaction
        Transaction createdTransaction = new Transaction();
        createdTransaction.setId("1");
        createdTransaction.setAmount(208.00);
        createdTransaction.setPaymentType(PaymentType.PAYPAL);
        createdTransaction.setStatus(PaymentStatus.NEW);
        createdTransaction.setOrderLines(orderLines);

        // Set up the mock behavior
        when(transactionRepository.save(transaction)).thenReturn(Mono.just(createdTransaction));

        // Create the transaction and verify the result
        Mono<Transaction> result = transactionService.createTransaction(transaction);

        StepVerifier.create(result)
                .expectNext(createdTransaction)
                .verifyComplete();

        verify(transactionRepository, times(1)).save(transaction);
        verifyNoMoreInteractions(transactionRepository);
    }



}

