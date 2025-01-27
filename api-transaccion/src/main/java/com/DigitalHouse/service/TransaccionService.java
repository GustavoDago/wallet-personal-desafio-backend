package com.DigitalHouse.service;

import com.DigitalHouse.entity.Transaction;
import com.DigitalHouse.exceptions.NotApprovedTransaction;
import com.DigitalHouse.exceptions.ResourceNotFoundException;
import com.DigitalHouse.records.CreateActivityRequest;
import com.DigitalHouse.records.RecordTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransaccionService {
//    Transaccion crearTransaccion(Long cuentaId, String tipo, BigDecimal monto);
//    List<Transaccion> obtenerTransaccionPorTipo(Long cuentaId, String tipo);
    List<Transaction> getUserActivities(String userId, String token, Optional<Integer> limit);
    Transaction createDeposit(String userId, CreateActivityRequest activityRequest, String accessToken) throws ResourceNotFoundException, NotApprovedTransaction;
    Transaction createTransfer(String userId, CreateActivityRequest activityRequest, String accessToken);
    Transaction createActivity(String userId, CreateActivityRequest activityRequest);
    void updateAccountBalance(String userId, double monto, String accessToken);
    RecordTransaction getUserActivity(String userId, String token, String activityId);
}
