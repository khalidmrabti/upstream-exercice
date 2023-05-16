package com.exercice.exercice.model;

import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@ToString
public enum PaymentStatus {
    NEW, AUTHORIZED, CAPTURED;
}
