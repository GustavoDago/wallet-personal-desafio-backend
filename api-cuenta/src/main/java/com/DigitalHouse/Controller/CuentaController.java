package com.DigitalHouse.Controller;

import com.DigitalHouse.Entity.Cuenta;
import com.DigitalHouse.Service.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api-cuenta")
public class CuentaController {
    @Autowired
    private CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<Cuenta> crearCuenta(@RequestBody Cuenta cuenta) {
        Cuenta nuevaCuenta = cuentaService.crearCuenta(cuenta);
        return ResponseEntity.ok(nuevaCuenta);
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<Cuenta>> obtenerCuentasPorUsuario(@PathVariable Long userId) {
        Optional<Cuenta> cuenta = cuentaService.obtenerCuentaPorId(userId);
        return cuenta.isPresent()
                ? ResponseEntity.ok(List.of(cuenta.get()))
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cuenta> obtenerCuentaPorId(@PathVariable Long id) {
        return cuentaService.obtenerCuentaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cuenta> actualizarCuenta(@PathVariable Long id, @RequestBody Cuenta cuenta) {
        Cuenta cuentaActualizada = cuentaService.actualizarCuenta(id, cuenta);
        return ResponseEntity.ok(cuentaActualizada);
    }

    @PutMapping("/usuario/{userId}/alias")
    public ResponseEntity<Void> actualizarAlias(@PathVariable Long userId, @RequestParam String alias) {
        cuentaService.actualizarAlias(userId, alias);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long id) {
        cuentaService.eliminarCuenta(id);
        return ResponseEntity.noContent().build();
    }
}
