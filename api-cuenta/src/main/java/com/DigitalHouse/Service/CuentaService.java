package com.DigitalHouse.Service;

import com.DigitalHouse.Entity.Cuenta;

import java.util.List;
import java.util.Optional;

public interface CuentaService {
    List<Cuenta> listarCuentas();
    Cuenta crearCuenta(Cuenta cuenta);
    Optional<Cuenta> obtenerCuentaPorId(Long id);
    Cuenta actualizarCuenta(Long id, Cuenta cuenta);
    void eliminarCuenta(Long id);
    void actualizarAlias(Long userId, String newAlias);
}
