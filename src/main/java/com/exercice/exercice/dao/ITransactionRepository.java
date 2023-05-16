package com.exercice.exercice.dao;


import com.exercice.exercice.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface ITransactionRepository extends ReactiveMongoRepository<Transaction, String> {
}
