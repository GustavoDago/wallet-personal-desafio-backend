package com.DigitalHouse.app.api_transactions.exceptions;

public class TooManyRequestsException extends Exception{

    public TooManyRequestsException(String message) {
        super(message);
    }


}
