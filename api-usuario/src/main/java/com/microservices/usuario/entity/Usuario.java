package com.microservices.usuario.entity;


import lombok.Data;



@Data
public class Usuario {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String dni;
}
