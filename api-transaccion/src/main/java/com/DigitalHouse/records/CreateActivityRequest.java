package com.DigitalHouse.records;

import com.DigitalHouse.entity.TransactionType;

import java.util.Optional;

public record CreateActivityRequest(
        double amount,
        TransactionType type,
        String description,
        Optional<String> date,
        String origin,
        String destination,
        String name
){}
