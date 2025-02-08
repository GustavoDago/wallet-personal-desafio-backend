package com.DigitalHouse.app.api_transactions.entity;

public enum TransactionType {
    Transfer("Transfer"),
    Deposit("Deposit");

    private final String type;

    TransactionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
