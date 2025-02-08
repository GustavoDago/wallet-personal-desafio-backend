package com.DigitalHouse.app.api_transactions.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

@Data
@Table(name = "transactions")
@Entity
public class Transaction {
    @Id
    @GeneratedValue
    @UuidGenerator
    private String  id;
    private double amount;
    private String userId;
    private String dated;
    private TransactionType type;
    private String origin;
    private String destination;
}

