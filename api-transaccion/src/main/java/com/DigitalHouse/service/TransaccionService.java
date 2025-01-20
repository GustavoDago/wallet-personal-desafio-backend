package com.DigitalHouse.service;

import com.DigitalHouse.entity.Transaccion;

import java.math.BigDecimal;
import java.util.List;

public interface TransaccionService {
    Transaccion crearTransaccion(Long cuentaId, String tipo, BigDecimal monto);
    List<Transaccion> obtenerTransaccionPorTipo(Long cuentaId, String tipo);

}
