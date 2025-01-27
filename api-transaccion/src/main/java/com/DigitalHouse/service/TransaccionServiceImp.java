package com.DigitalHouse.service;


import com.DigitalHouse.Entity.Cuenta;
import com.DigitalHouse.entity.Transaction;
import com.DigitalHouse.entity.TransactionType;
import com.DigitalHouse.exceptions.NotApprovedTransaction;
import com.DigitalHouse.exceptions.ResourceNotFoundException;
import com.DigitalHouse.feign.AccountFeignClient;
import com.DigitalHouse.feign.UserFeignClient;
import com.DigitalHouse.records.CreateActivityRequest;
import com.DigitalHouse.records.RecordTransaction;
import com.DigitalHouse.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TransaccionServiceImp implements TransaccionService{
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private AccountFeignClient accountFeignClient;

    double maxAmount = 3000d;

    @Override
    public List<Transaction> getUserActivities(String userId, String token, Optional<Integer> limit) {
        return transactionRepository.findAllByUserId(userId);
    }

    @Override
    public Transaction createDeposit(String userId, CreateActivityRequest activityRequest, String accessToken) throws ResourceNotFoundException, NotApprovedTransaction {
        // Lógica para crear depósito
        // comprobar que exista el usuario
        ResponseEntity<Map> userResponse = userFeignClient.getUserData(userId, accessToken);
        if (userResponse.getStatusCode().isError()) {
            throw new ResourceNotFoundException("Error - No se encuentran datos del usuario");
        }
        // buscar cuenta del usuario
        ResponseEntity<Cuenta> accountResponse = accountFeignClient.getAccount(userId, accessToken);
        if (accountResponse.getStatusCode().isError()) {
            throw new ResourceNotFoundException("Error - No se encuentra esa cuenta de usuario");
        }

        // comprobar que el monto no supere los 3000
        double monto = activityRequest.amount();
        if (monto > maxAmount) {
            throw new RuntimeException("El monto supera $3000");
        }

        // Llena los detalles del depósito según `activityRequest`
        String fecha = LocalDateTime.now().toString();
        Transaction deposit = new Transaction();
        deposit.setUserId(userId);
        deposit.setAmount(monto);
        deposit.setDated(fecha);
        deposit.setType(TransactionType.Deposit);
        deposit.setOrigin(accountResponse.getBody().getId());
        deposit.setDestination(activityRequest.destination());

        try {
            Transaction transactionSave = transactionRepository.save(deposit);
            updateAccountBalance(userId, monto, accessToken);
            return transactionSave;
        } catch (Exception e) {
            throw new NotApprovedTransaction("Hay un error en el procesamiento de la transacción");
        }
    }

    @Override
    public Transaction createTransfer(String userId, CreateActivityRequest activityRequest, String accessToken) {
        return null;
    }

    @Override
    public Transaction createActivity(String userId, CreateActivityRequest activityRequest) {
        return null;
    }

    @Override
    public void updateAccountBalance(String userId, double monto, String accessToken) {

    }

    @Override
    public RecordTransaction getUserActivity(String userId, String token, String activityId) {
        return null;
    }
}
