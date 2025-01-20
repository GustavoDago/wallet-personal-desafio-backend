package com.DigitalHouse.feign;

import com.DigitalHouse.Entity.Cuenta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "api-cuenta")
public interface CuentaClient {
    @GetMapping("/api-cuenta/{id}")
    Cuenta obtenerCuenta(@PathVariable Long id);

    @PutMapping("/api-cuenta/{id}")
    void actualizarCuenta(@PathVariable Long id, @RequestBody Cuenta cuenta);
}
