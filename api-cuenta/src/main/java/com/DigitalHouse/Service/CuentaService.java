package com.DigitalHouse.Service;

import com.DigitalHouse.Entity.Cuenta;
import com.DigitalHouse.Entity.RecordAccount;
import com.DigitalHouse.exceptions.ResourceAlreadyExistsException;
import com.DigitalHouse.exceptions.ResourceBadRequestException;
import com.DigitalHouse.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

public interface CuentaService {
    List<Cuenta> listarCuentas();
    Cuenta crearCuenta(String userId, String accessToken) throws ResourceAlreadyExistsException;
    Optional<Cuenta> obtenerCuentaPorId(String  id);
    Cuenta actualizarCuenta(String  id, Cuenta cuenta);
    void eliminarCuenta(String  id);
    void actualizarAlias(String userId, String newAlias);

    Cuenta getAccountByUserId(String userId);

    Cuenta updateAccount(String userId, RecordAccount data) throws ResourceBadRequestException, ResourceNotFoundException;

    Cuenta updateAccountBalance(String accountId, RecordAccount data) throws Exception;

    RecordAccount getAccountByAccountId(String accountId) throws ResourceNotFoundException;
}
