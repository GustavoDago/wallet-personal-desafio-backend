package com.microservices.usuario.records;

public record NewUserRecord(String email,
                            String password,
                            String firstName,
                            String lastName,
                            String phone,
                            String dni) {
}
