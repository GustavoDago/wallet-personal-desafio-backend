package com.DigitalHouse.app.api_transactions.records;

import java.util.Optional;

public record RecordTransaction(
        String  id,
        String userId,
        double amount,
        Optional<String> name,
        String dated,
        String type,
        Optional<String> origin,
        Optional<String> destination
) {}
