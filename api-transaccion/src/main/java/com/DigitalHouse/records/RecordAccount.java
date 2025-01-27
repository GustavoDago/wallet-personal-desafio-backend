package com.DigitalHouse.records;

public record RecordAccount(
        String id,
        String alias,
        String cvu,
        double balance,
        String name,
        String userId
) {}
