package com.DigitalHouse.entity;

import com.DigitalHouse.Entity.Cuenta;
import jakarta.persistence.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Builder
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Cuenta cuenta;

    private String tipo; // Ejemplo: "Ingreso", "Egreso"

    @Column(nullable = false)
    private BigDecimal amount;

    private LocalDateTime fecha = LocalDateTime.now();
}
