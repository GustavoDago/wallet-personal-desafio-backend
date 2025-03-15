package com.DigitalHouse.app.api_transactions.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransaccionServiceImpTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserFeignClient userFeignClient;

    @Mock
    private CuentaFeignClient accountFeignClient;

    @InjectMocks
    private TransaccionServiceImp transaccionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transaccionService.maxAmount = 3000.0; // Initialize maxAmount
    }

    @Test
    void getUserActivities_ShouldReturnListOfTransactions() {
        // Arrange
        String userId = "1";
        String token = "token";
        Optional<Integer> limit = Optional.empty();
        List<Transaction> expectedTransactions = List.of(new Transaction(), new Transaction());
        when(transactionRepository.findAllByUserId(userId)).thenReturn(expectedTransactions);

        // Act
        List<Transaction> actualTransactions = transaccionService.getUserActivities(userId, token, limit);

        // Assert
        assertEquals(expectedTransactions, actualTransactions);
        verify(transactionRepository).findAllByUserId(userId);
    }

    @Test
    void createDeposit_Successful() throws NotApprovedTransaction, ResourceNotFoundException {
        // Arrange
        String userId = "1";
        String token = "token";
        CreateActivityRequest activityRequest = new CreateActivityRequest(100.0, TransactionType.Deposit, "origin", "destination");

        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", "John");
        userData.put("lastName", "Doe");
        ResponseEntity<Map> userResponse = new ResponseEntity<>(userData, HttpStatus.OK);

        String accountId = "account1";
        RecordAccount recordAccount = new RecordAccount(accountId, "alias", "cvu", 1000.0, "John Doe", userId);

        when(userFeignClient.getUserData(userId, token)).thenReturn(userResponse);
        when(accountFeignClient.getAccountIdByUserId(userId, token)).thenReturn(accountId);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountFeignClient.findAccount(accountId)).thenReturn(recordAccount);
        //  doNothing().when(accountFeignClient).updateAccountBalance(anyString(), anyString(), any(RecordAccount.class), anyString());
        when(accountFeignClient.updateAccountBalance(anyString(), anyString(), any(RecordAccount.class), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act
        Transaction createdTransaction = transaccionService.createDeposit(userId, activityRequest, token);


        // Assert
        assertNotNull(createdTransaction);
        assertEquals(userId, createdTransaction.getUserId());
        assertEquals(activityRequest.amount(), createdTransaction.getAmount());
        assertEquals(TransactionType.Deposit, createdTransaction.getType());
        assertEquals(accountId, createdTransaction.getOrigin());
        assertEquals(activityRequest.destination(), createdTransaction.getDestination());
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountFeignClient).updateAccountBalance(eq(userId), eq(accountId), any(RecordAccount.class), eq(token));
    }


    @Test
    void createDeposit_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String userId = "1";
        String token = "token";
        CreateActivityRequest activityRequest = new CreateActivityRequest(100.0, TransactionType.Deposit, "origin", "destination");
        when(userFeignClient.getUserData(userId, token)).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> transaccionService.createDeposit(userId, activityRequest, token));
    }

    @Test
    void createDeposit_AmountExceedsLimit_ThrowsRuntimeException() {
        // Arrange
        String userId = "1";
        String token = "token";
        CreateActivityRequest activityRequest = new CreateActivityRequest(4000.0, TransactionType.Deposit, "origin", "destination");
        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", "John");
        ResponseEntity<Map> userResponse = new ResponseEntity<>(userData, HttpStatus.OK);
        when(userFeignClient.getUserData(userId, token)).thenReturn(userResponse);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> transaccionService.createDeposit(userId, activityRequest, token));
        assertEquals("El monto supera $3000", exception.getMessage());
    }


    @Test
    void createTransfer_Successful() throws ResourceNotFoundException {
        // Arrange
        String userId = "1";
        String token = "token";
        CreateActivityRequest activityRequest = new CreateActivityRequest(100.0, TransactionType.Transfer, "originAccount", "destinationAccount");

        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", "John");
        ResponseEntity<Map> userResponse = new ResponseEntity<>(userData, HttpStatus.OK);
        RecordAccount originAccount = new RecordAccount("originAccount", "alias1", "cvu1", 1000.0, "John Doe", userId);
        RecordAccount destinationAccount = new RecordAccount("destinationAccount", "alias2", "cvu2", 500.0, "Jane Doe", "2");

        when(userFeignClient.getUserData(userId, token)).thenReturn(userResponse);
        when(accountFeignClient.findAccount("originAccount")).thenReturn(originAccount);
        when(accountFeignClient.findAccount("destinationAccount")).thenReturn(destinationAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        //doNothing().when(accountFeignClient).updateAccountBalance(anyString(), anyString(), any(RecordAccount.class), anyString());
        when(accountFeignClient.updateAccountBalance(anyString(), anyString(), any(RecordAccount.class), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act
        Transaction createdTransaction = transaccionService.createTransfer(userId, activityRequest, token);

        // Assert
        assertNotNull(createdTransaction);
        assertEquals(userId, createdTransaction.getUserId());
        assertEquals(-activityRequest.amount(), createdTransaction.getAmount());
        assertEquals(TransactionType.Transfer, createdTransaction.getType());
        assertEquals("originAccount", createdTransaction.getOrigin());
        assertEquals("destinationAccount", createdTransaction.getDestination());
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountFeignClient, times(2)).updateAccountBalance(anyString(), anyString(), any(RecordAccount.class), anyString());

    }

    @Test
    void createTransfer_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String userId = "1";
        String token = "token";
        CreateActivityRequest activityRequest = new CreateActivityRequest(100.0, TransactionType.Transfer, "origin", "destination");

        when(userFeignClient.getUserData(userId, token)).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));


        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> transaccionService.createTransfer(userId, activityRequest, token));

    }

    @Test
    void createActivity_ShouldReturnSavedTransaction() {
        // Arrange
        String userId = "1";
        CreateActivityRequest activityRequest = new CreateActivityRequest(100.0, TransactionType.Deposit, "origin", "destination");
        Transaction expectedTransaction = new Transaction();
        expectedTransaction.setAmount(activityRequest.amount());
        expectedTransaction.setType(activityRequest.type());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(expectedTransaction);

        // Act
        Transaction actualTransaction = transaccionService.createActivity(userId, activityRequest);

        // Assert
        assertEquals(expectedTransaction, actualTransaction);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void getUserActivity_Successful() throws ResourceNotFoundException {
        String userId = "user1";
        String token = "testToken";
        String activityId = "activity1";
        Transaction transaction = new Transaction();
        transaction.setId(activityId);
        transaction.setUserId(userId);
        transaction.setAmount(100.0);
        transaction.setDated(LocalDateTime.now().toString());
        transaction.setType(TransactionType.Deposit);

        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", "John");
        ResponseEntity<Map> userResponse = new ResponseEntity<>(userData, HttpStatus.OK);

        when(transactionRepository.findById(activityId)).thenReturn(Optional.of(transaction));
        when(userFeignClient.getUserData(userId, token)).thenReturn(userResponse);

        RecordTransaction recordTransaction = transaccionService.getUserActivity(userId, token, activityId);

        assertNotNull(recordTransaction);
        assertEquals(activityId, recordTransaction.id());
        assertEquals(userId, recordTransaction.userId());
        assertEquals(100.0, recordTransaction.amount());
        assertEquals("John", recordTransaction.name().orElse(null));  // Check Optional
        assertEquals(TransactionType.Deposit.getType(), recordTransaction.type());


        verify(transactionRepository).findById(activityId);
        verify(userFeignClient).getUserData(userId, token);
    }


    @Test
    void getUserActivity_TransactionNotFound_ThrowsResourceNotFoundException() {
        String userId = "user1";
        String token = "testToken";
        String activityId = "activity1";

        when(transactionRepository.findById(activityId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transaccionService.getUserActivity(userId, token, activityId));

        verify(transactionRepository).findById(activityId);
        verify(userFeignClient, never()).getUserData(anyString(), anyString()); // Ensure getUserData is not called
    }

    @Test
    void getUserActivity_TransactionDoesNotBelongToUser_ThrowsResourceNotFoundException() {
        String userId = "user1";
        String token = "testToken";
        String activityId = "activity1";
        Transaction transaction = new Transaction();
        transaction.setId(activityId);
        transaction.setUserId("anotherUser"); // Different user

        when(transactionRepository.findById(activityId)).thenReturn(Optional.of(transaction));

        assertThrows(ResourceNotFoundException.class, () -> transaccionService.getUserActivity(userId, token, activityId));

        verify(transactionRepository).findById(activityId);
        verify(userFeignClient, never()).getUserData(anyString(), anyString());  // Ensure getUserData is not called
    }

    @Test
    void getUserActivity_UserNotFound_ThrowsResourceNotFoundException() {
        String userId = "user1";
        String token = "testToken";
        String activityId = "activity1";
        Transaction transaction = new Transaction();
        transaction.setId(activityId);
        transaction.setUserId(userId);

        when(transactionRepository.findById(activityId)).thenReturn(Optional.of(transaction));
        when(userFeignClient.getUserData(userId, token)).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThrows(ResourceNotFoundException.class, () -> transaccionService.getUserActivity(userId, token, activityId));
        verify(transactionRepository).findById(activityId);
        verify(userFeignClient).getUserData(userId, token);
    }

    @Test
    void updateAccountBalance_Successful() throws ResourceNotFoundException {
        // Arrange
        String accountId = "account1";
        double monto = 100.0;
        String accessToken = "token";
        RecordAccount initialAccount = new RecordAccount(accountId, "alias", "cvu", 1000.0, "John Doe", "user1");
        when(accountFeignClient.findAccount(accountId)).thenReturn(initialAccount);
        when(accountFeignClient.updateAccountBalance(anyString(),anyString(),any(),anyString())).thenReturn(ResponseEntity.ok().build());
        // Act
        transaccionService.updateAccountBalance(accountId, monto, accessToken);

        // Assert

        // Verify that findAccount was called
        verify(accountFeignClient).findAccount(accountId);

        // Construct the expected updated account
        RecordAccount expectedUpdatedAccount = new RecordAccount(
                initialAccount.id(),
                initialAccount.alias(),
                initialAccount.cvu(),
                initialAccount.balance() + monto, // Correct balance update
                initialAccount.name(),
                initialAccount.userId()
        );

        verify(accountFeignClient).updateAccountBalance(
                eq(initialAccount.userId()),
                eq(accountId),
                argThat(account -> account.balance() == expectedUpdatedAccount.balance()), // Use argThat for custom matching
                eq(accessToken)
        );
    }
}