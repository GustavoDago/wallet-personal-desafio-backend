package com.DigitalHouse.controller;

import com.DigitalHouse.entity.Transaccion;
import com.DigitalHouse.service.TransaccionServiceImp;
import jakarta.transaction.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transaccion")
public class TransaccionController {
    @Autowired
    private TransaccionServiceImp transaccionService;

    public TransaccionController(TransaccionServiceImp transaccionService) {
        this.transaccionService = transaccionService;
    }

    @PostMapping
    public ResponseEntity<Transaccion> createTransaction(@RequestParam Long accountId, @RequestParam String type, @RequestParam BigDecimal amount) {
        Transaction transaction = (Transaction) transaccionService.crearTransaccion(accountId, type, amount);
        return ResponseEntity.ok((Transaccion) transaction);
    }

    @GetMapping("/{cuentaId}")
    public ResponseEntity<List<Transaccion>> getTransactions(@PathVariable Long accountId, @RequestParam String type) {
        return ResponseEntity.ok(transaccionService.obtenerTransaccionPorTipo(accountId, type));
    }
}
