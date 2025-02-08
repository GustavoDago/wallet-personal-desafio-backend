package com.DigitalHouse.app.api_transactions.service;


import com.DigitalHouse.Entity.Cuenta;
import com.DigitalHouse.app.api_transactions.entity.Transaction;
import com.DigitalHouse.app.api_transactions.entity.TransactionType;
import com.DigitalHouse.app.api_transactions.exceptions.NotApprovedTransaction;
import com.DigitalHouse.app.api_transactions.exceptions.ResourceNotFoundException;
import com.DigitalHouse.app.api_transactions.feign.CuentaFeignClient;
import com.DigitalHouse.app.api_transactions.feign.UserFeignClient;
import com.DigitalHouse.app.api_transactions.records.CreateActivityRequest;
import com.DigitalHouse.app.api_transactions.records.RecordAccount;
import com.DigitalHouse.app.api_transactions.records.RecordTransaction;
import com.DigitalHouse.app.api_transactions.repository.TransactionRepository;
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
    private CuentaFeignClient accountFeignClient;

    double maxAmount = 3000d;

    @Override
    public List<Transaction> getUserActivities(String userId, String token, Optional<Integer> limit) {
        return transactionRepository.findAllByUserId(userId);
    }

    @Override
    public Transaction createDeposit(String userId, CreateActivityRequest activityRequest, String accessToken) throws NotApprovedTransaction, ResourceNotFoundException {
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
    public Transaction createTransfer(String userId, CreateActivityRequest activityRequest, String accessToken) throws ResourceNotFoundException {
        // comprobar que exista el usuario
        ResponseEntity<Map> userResponse = userFeignClient.getUserData(userId, accessToken);
        if (userResponse.getStatusCode().isError()){
            throw new ResourceNotFoundException("Error - No se encuentran datos del usuario");
        }
        // buscar cuenta del usuario
        RecordAccount accountOrigin = accountFeignClient.findAccount(activityRequest.origin());
        ResponseEntity<Cuenta> accountDestination = accountFeignClient.getAccount(userId,accessToken);
        if (accountOrigin.id() == null || accountDestination == null){
            throw new ResourceNotFoundException("Error - No se encuentran datos de cuenta correctos");
        }
        // Lógica para crear transferencia
        // Llena los detalles de la transferencia según `activityRequest`
        String fecha = LocalDateTime.now().toString();
        double monto = activityRequest.amount() * (-1);

        Transaction transfer = new Transaction();
        transfer.setUserId(userId);
        transfer.setAmount(monto);
        transfer.setDated(fecha);
        transfer.setType(TransactionType.Transfer);
        transfer.setOrigin(accountOrigin.id());
        transfer.setDestination(accountDestination.getBody().getId());
        Transaction transactionSave = transactionRepository.save(transfer);

        updateAccountBalance(userId, monto, accessToken);

        return transactionSave;
    }

    @Override
    public Transaction createActivity(String userId, CreateActivityRequest activityRequest) {
        Transaction transaction = new Transaction();
        double monto = (activityRequest.type() == TransactionType.Transfer)
                ? activityRequest.amount() * -1 : activityRequest.amount();
        transaction.setAmount(monto);
        transaction.setType(activityRequest.type());
        transaction.setDated(LocalDateTime.now().toString());
        transaction.setOrigin(transaction.getOrigin());
        transaction.setDestination(transaction.getDestination());

        Transaction transactionGuardada = transactionRepository.save(transaction);
        return transactionGuardada;
    }

    @Override
    public void updateAccountBalance(String userId, double monto, String accessToken) throws ResourceNotFoundException {
        // buscar cuenta del usuario
        ResponseEntity<?> accountResponse = accountFeignClient.getAccount(userId,accessToken);
        if (accountResponse.getStatusCode().isError()){
            throw new ResourceNotFoundException("Error - No se encuentra esa cuenta de usuario");
        }
        Object responseBody = accountResponse.getBody();
        if (responseBody instanceof Cuenta) {
            Cuenta account = (Cuenta) responseBody;
            // Ahora puedo acceder a los datos del objeto account
            String id = account.getId();
            String alias = account.getAlias();
            String cvu = account.getCvu();
            // en caso de una transferencia, el monto viene negativo
            double balance = account.getBalance() + monto;
            String name = account.getName();
            RecordAccount recordAccount = new RecordAccount(
                    id,
                    alias,
                    cvu,
                    balance,
                    name,
                    userId
            );
            accountFeignClient.updateAccountBalance(userId,id,recordAccount,accessToken);
            System.out.println("ID: " + id);
            System.out.println("Alias: " + alias);
            System.out.println("CVU: " + cvu);
            System.out.println("Balance: " + balance);
            System.out.println("Name: " + name);
            System.out.println("User ID: " + userId);
        } else {
            throw new IllegalArgumentException("Error - El cuerpo de la respuesta no es una instancia de Account");}
    }

    @Override
    public RecordTransaction getUserActivity(String userId, String token, String activityId) throws ResourceNotFoundException {
        Optional<Transaction> transaction = transactionRepository.findById(activityId);
        if (transaction.isPresent()){
            Transaction t = transaction.get();
            if (t.getUserId().equals(userId)){
                ResponseEntity<Map> userResponse = userFeignClient.getUserData(userId,token);
                if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                    String userName = (String) userResponse.getBody().get("firstName");
                    return new RecordTransaction(
                            activityId,
                            userId,
                            t.getAmount(),
                            Optional.ofNullable(userName), // Usar Optional para manejar la posibilidad de null
                            t.getDated(),
                            t.getType().getType(),
                            Optional.ofNullable(t.getOrigin()),
                            Optional.ofNullable(t.getDestination())
                    );
                } else {
                    throw new ResourceNotFoundException("Error al obtener datos del usuario.");
                }
            } else {
                // Manejar el caso donde el userId no coincide con el de la transacción.
                // Puedo lanzar una excepción o retornar null.
                throw new ResourceNotFoundException("La transacción no pertenece al usuario."); // O return null;

            }

        } else {
            throw new ResourceNotFoundException("Transacción no encontrada.");}
    }
}
