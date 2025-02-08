package com.DigitalHouse.app.api_transactions.records;

public record RecordAccount(
        String id,
        String alias,
        String cvu,
        double balance,
        String name,
        String userId
) {}
