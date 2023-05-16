package com.exercice.exercice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document("Transaction")
public class Transaction {
    @JsonProperty("id")
    @Id
    private String id;
    private double amount;
    private PaymentType paymentType;
    @Builder.Default
    private PaymentStatus status =PaymentStatus.NEW ;
    private List<OrderLine> orderLines;

}
