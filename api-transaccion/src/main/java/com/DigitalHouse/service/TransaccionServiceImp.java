package com.DigitalHouse.service;

import com.DigitalHouse.Entity.Cuenta;
import com.DigitalHouse.entity.Transaccion;
import com.DigitalHouse.feign.CuentaClient;
import com.DigitalHouse.repository.TransaccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class TransaccionServiceImp implements TransaccionService{
    @Autowired
    private TransaccionRepository transaccionRepository;


    @Override
    public Transaccion crearTransaccion(Long cuentaId, String tipo, BigDecimal monto) {
        return null;
    }

    @Override
    public List<Transaccion> obtenerTransaccionPorTipo(Long cuentaId, String tipo) {
        return List.of();
    }


    @Autowired
    private CuentaClient cuentaClient;

    public Transaccion registrarTransaccion(Long cuentaId, String tipo, BigDecimal monto) {
        // Obtener la cuenta usando Feign
        Cuenta cuenta = cuentaClient.obtenerCuenta(cuentaId);

        // Validar saldo para egresos
        if ("Egreso".equalsIgnoreCase(tipo) && cuenta.getBalance().compareTo(monto) < 0) {
            throw new RuntimeException("Fondos insuficientes en la cuenta");
        }

        // Actualizar saldo en la cuenta
        BigDecimal nuevoBalance = "Egreso".equalsIgnoreCase(tipo)
                ? cuenta.getBalance().subtract(monto)
                : cuenta.getBalance().add(monto);
        cuenta.setBalance(nuevoBalance);

        // Actualizar la cuenta mediante Feign
        cuentaClient.actualizarCuenta(cuentaId, cuenta);

        // Registrar la transacción
        Transaccion transaccion = Transaccion.builder()
                .id(cuentaId)
                .tipo(tipo)
                .amount(monto)
                .fecha(LocalDateTime.now())
                .build();

        return transaccionRepository.save(transaccion);
    }
}
