package com.microservices.usuario.exception;

public class ResourceBadRequestException extends Exception{
    public ResourceBadRequestException(String message) {
        super(message);
    }
}
