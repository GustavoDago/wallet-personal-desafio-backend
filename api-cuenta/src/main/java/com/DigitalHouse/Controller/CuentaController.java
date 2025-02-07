package com.DigitalHouse.Controller;

import com.DigitalHouse.Entity.Cuenta;
import com.DigitalHouse.Entity.RecordAccount;
import com.DigitalHouse.Service.CuentaService;
import com.DigitalHouse.exceptions.ResourceAlreadyExistsException;
import com.DigitalHouse.exceptions.ResourceBadRequestException;
import com.DigitalHouse.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping()
public class CuentaController {
    @Autowired
    private CuentaService cuentaService;

    @PostMapping("users/{userId}/accounts")
    public ResponseEntity<?> crearCuenta(@PathVariable String userId,
                                              @RequestHeader("Authorization") String accessToken) {
        try {
            Cuenta nuevaCuenta = cuentaService.crearCuenta(userId,accessToken);
            return ResponseEntity.ok(nuevaCuenta);
        } catch (ResourceAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/accounts")
    public List<Cuenta> getAccounts(){
        List<Cuenta> cuentas = cuentaService.listarCuentas();
        return cuentas;
    }

    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<?> getAccount(@PathVariable String userId,
                                        @RequestHeader("Authorization") String accessToken) {
        try {
            Cuenta account = cuentaService.getAccountByUserId(userId);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ocurrió un error: " + e.getMessage());
        }
    }


    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<Cuenta>> obtenerCuentasPorUsuario(@PathVariable String  userId) {
        Optional<Cuenta> cuenta = cuentaService.obtenerCuentaPorId(userId);
        return cuenta.isPresent()
                ? ResponseEntity.ok(List.of(cuenta.get()))
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cuenta> obtenerCuentaPorId(@PathVariable String  id) {
        return cuentaService.obtenerCuentaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cuenta> actualizarCuenta(@PathVariable String  id, @RequestBody Cuenta cuenta) {
        Cuenta cuentaActualizada = cuentaService.actualizarCuenta(id, cuenta);
        return ResponseEntity.ok(cuentaActualizada);
    }

    @PutMapping("/usuario/{userId}/alias")
    public ResponseEntity<Void> actualizarAlias(@PathVariable String  userId, @RequestParam String alias) {
        cuentaService.actualizarAlias(userId, alias);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable String  id) {
        cuentaService.eliminarCuenta(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/accounts/1")
    public ResponseEntity<?> updateAccount(@PathVariable String userId,
                                           @RequestBody RecordAccount data,
                                           @RequestHeader("Authorization") String accessToken) {
        try {
            Cuenta updatedAccount = cuentaService.updateAccount(userId, data);
            return ResponseEntity.ok(updatedAccount);
        } catch (ResourceBadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
