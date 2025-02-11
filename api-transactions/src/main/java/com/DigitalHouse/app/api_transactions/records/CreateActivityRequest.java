package com.DigitalHouse.app.api_transactions.records;



import com.DigitalHouse.app.api_transactions.entity.TransactionType;

import java.util.Optional;

public record CreateActivityRequest(
        double amount,
        TransactionType type,
        String origin,
        String destination
){}
