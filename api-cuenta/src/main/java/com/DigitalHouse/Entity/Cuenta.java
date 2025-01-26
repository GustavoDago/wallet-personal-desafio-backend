package com.DigitalHouse.Entity;

import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;

@Data

@Table(name = "cuentas")
@Entity
public class Cuenta {
    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;
    private String alias;
    private String cvu;
    private double balance ;
    private String name;
    private String userId;
}
