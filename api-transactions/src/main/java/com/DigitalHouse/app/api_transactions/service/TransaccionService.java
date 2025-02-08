package com.DigitalHouse.app.api_transactions.service;



import com.DigitalHouse.app.api_transactions.entity.Transaction;
import com.DigitalHouse.app.api_transactions.exceptions.NotApprovedTransaction;
import com.DigitalHouse.app.api_transactions.exceptions.ResourceNotFoundException;
import com.DigitalHouse.app.api_transactions.records.CreateActivityRequest;
import com.DigitalHouse.app.api_transactions.records.RecordTransaction;

import java.util.List;
import java.util.Optional;

public interface TransaccionService {
//    Transaccion crearTransaccion(Long cuentaId, String tipo, BigDecimal monto);
//    List<Transaccion> obtenerTransaccionPorTipo(Long cuentaId, String tipo);
    List<Transaction> getUserActivities(String userId, String token, Optional<Integer> limit);
    Transaction createDeposit(String userId, CreateActivityRequest activityRequest, String accessToken) throws ResourceNotFoundException, NotApprovedTransaction, ResourceNotFoundException;
    Transaction createTransfer(String userId, CreateActivityRequest activityRequest, String accessToken) throws ResourceNotFoundException;
    Transaction createActivity(String userId, CreateActivityRequest activityRequest);
    void updateAccountBalance(String userId, double monto, String accessToken) throws ResourceNotFoundException;
    RecordTransaction getUserActivity(String userId, String token, String activityId) throws ResourceNotFoundException;
}
