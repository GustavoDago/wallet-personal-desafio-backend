package com.DigitalHouse.Service;

import com.DigitalHouse.Entity.Cuenta;
import com.DigitalHouse.Entity.RecordAccount;
import com.DigitalHouse.Repository.CuentaRepository;
import com.DigitalHouse.components.CvuGenerator;
import com.DigitalHouse.exceptions.ResourceAlreadyExistsException;
import com.DigitalHouse.exceptions.ResourceBadRequestException;
import com.DigitalHouse.exceptions.ResourceNotFoundException;
import com.DigitalHouse.feignClients.UserFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CuentaServiceImpTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private CvuGenerator cvuGenerator;

    @Mock
    private UserFeignClient userFeignClient;

    @InjectMocks
    private CuentaServiceImp cuentaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listarCuentas_ShouldReturnListOfAccounts() {
        // Arrange
        List<Cuenta> expectedAccounts = new ArrayList<>();
        expectedAccounts.add(new Cuenta());
        expectedAccounts.add(new Cuenta());
        when(cuentaRepository.findAll()).thenReturn(expectedAccounts);

        // Act
        List<Cuenta> actualAccounts = cuentaService.listarCuentas();

        // Assert
        assertEquals(expectedAccounts.size(), actualAccounts.size());
        verify(cuentaRepository, times(1)).findAll();
    }

    @Test
    void crearCuenta_ShouldCreateAccountSuccessfully() throws ResourceAlreadyExistsException {
        // Arrange
        String userId = "123";
        String accessToken = "token";
        String userName = "Test User";
        String generatedCvu = "1234567890123456789012";
        Cuenta savedAccount = new Cuenta(); // Create a populated Cuenta object

        when(cuentaRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cvuGenerator.generateCvu()).thenReturn(generatedCvu);
        when(userFeignClient.obtenerUserName(userId, accessToken)).thenReturn(new ResponseEntity<>(userName, HttpStatus.OK));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(savedAccount);

        // Act
        Cuenta result = cuentaService.crearCuenta(userId, accessToken);

        // Assert
        assertNotNull(result);
        verify(cuentaRepository).save(any(Cuenta.class));
    }

    @Test
    void crearCuenta_ShouldThrowResourceAlreadyExistsException() throws ResourceAlreadyExistsException {
        // Arrange
        String userId = "123";
        String accessToken = "token";
        when(cuentaRepository.findByUserId(userId)).thenReturn(Optional.of(new Cuenta()));

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> cuentaService.crearCuenta(userId, accessToken));
    }


    @Test
    void obtenerCuentaPorId_ShouldReturnAccount() {
        // Arrange
        String id = "1";
        Cuenta expectedAccount = new Cuenta();
        expectedAccount.setId(id);
        when(cuentaRepository.findById(id)).thenReturn(Optional.of(expectedAccount));

        // Act
        Optional<Cuenta> actualAccount = cuentaService.obtenerCuentaPorId(id);

        // Assert
        assertTrue(actualAccount.isPresent());
        assertEquals(expectedAccount.getId(), actualAccount.get().getId());
        verify(cuentaRepository, times(1)).findById(id);
    }

    @Test
    void obtenerCuentaPorId_ShouldReturnEmptyOptionalWhenNotFound() {
        // Arrange
        String id = "1";
        when(cuentaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Cuenta> actualAccount = cuentaService.obtenerCuentaPorId(id);

        // Assert
        assertFalse(actualAccount.isPresent());
        verify(cuentaRepository, times(1)).findById(id);
    }

    @Test
    void actualizarCuenta_ShouldUpdateAccount() {
        // Arrange
        String id = "1";
        Cuenta existingAccount = new Cuenta();
        existingAccount.setId(id);
        existingAccount.setAlias("oldAlias");
        Cuenta updatedAccountData = new Cuenta();
        updatedAccountData.setAlias("newAlias");
        updatedAccountData.setBalance(100.0);

        Cuenta savedAccount = new Cuenta(); // Create a populated Cuenta object
        savedAccount.setId(id);
        savedAccount.setAlias("newAlias");
        savedAccount.setBalance(100.0);


        when(cuentaRepository.findById(id)).thenReturn(Optional.of(existingAccount));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(savedAccount);

        // Act
        Cuenta updatedAccount = cuentaService.actualizarCuenta(id, updatedAccountData);

        // Assert
        assertEquals("newAlias", updatedAccount.getAlias());
        assertEquals(100.0, updatedAccount.getBalance());
        verify(cuentaRepository, times(1)).findById(id);
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
    }


    @Test
    void eliminarCuenta_ShouldDeleteAccount() {
        // Arrange
        String id = "1";
        when(cuentaRepository.existsById(id)).thenReturn(true);

        // Act
        cuentaService.eliminarCuenta(id);

        // Assert
        verify(cuentaRepository, times(1)).deleteById(id);
    }

    @Test
    void eliminarCuenta_ShouldThrowExceptionWhenAccountNotFound() {
        // Arrange
        String id = "1";
        when(cuentaRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cuentaService.eliminarCuenta(id));
        verify(cuentaRepository, never()).deleteById(id);
    }

    @Test
    void actualizarAlias_ShouldUpdateAlias() {
        // Arrange
        String userId = "123";
        String newAlias = "new.alias";
        Cuenta existingAccount = new Cuenta();
        existingAccount.setUserId(userId);
        existingAccount.setAlias("old.alias");

        when(cuentaRepository.findByUserId(userId)).thenReturn(Optional.of(existingAccount));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(existingAccount); // Return the account for simplicity

        // Act
        cuentaService.actualizarAlias(userId, newAlias);

        // Assert
        assertEquals(newAlias, existingAccount.getAlias());
        verify(cuentaRepository, times(1)).findByUserId(userId);
        verify(cuentaRepository, times(1)).save(existingAccount);
    }

    @Test
    void actualizarAlias_ShouldThrowExceptionWhenAccountNotFound() {
        // Arrange
        String userId = "123";
        String newAlias = "new.alias";
        when(cuentaRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cuentaService.actualizarAlias(userId, newAlias));
        verify(cuentaRepository, times(1)).findByUserId(userId);
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @Test
    void getAccountByUserId_ShouldReturnAccount() throws ResourceNotFoundException {
        // Arrange
        String userId = "123";
        Cuenta expectedAccount = new Cuenta();
        expectedAccount.setUserId(userId);
        when(cuentaRepository.findByUserId(userId)).thenReturn(Optional.of(expectedAccount));

        // Act
        Cuenta actualAccount = cuentaService.getAccountByUserId(userId);

        // Assert
        assertEquals(expectedAccount.getUserId(), actualAccount.getUserId());
        verify(cuentaRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getAccountByUserId_ShouldThrowResourceNotFoundException() {
        // Arrange
        String userId = "123";
        when(cuentaRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cuentaService.getAccountByUserId(userId));
        verify(cuentaRepository, times(1)).findByUserId(userId);
    }

    @Test
    void updateAccount_ShouldUpdateAccountSuccessfully() throws ResourceBadRequestException, ResourceNotFoundException {
        // Arrange
        String userId = "123";
        String accountId = "456";
        RecordAccount data = new RecordAccount(accountId, "new.alias", "newCvu", 150.0, "New Name", "newUserId");
        Cuenta existingAccount = new Cuenta();
        existingAccount.setId(accountId);
        existingAccount.setAlias("old.alias");
        existingAccount.setCvu("oldCvu");
        existingAccount.setBalance(100.0);
        existingAccount.setName("Old Name");
        existingAccount.setUserId(userId);

        Cuenta savedAccount = new Cuenta();
        savedAccount.setId(accountId);
        savedAccount.setAlias("new.alias");
        savedAccount.setCvu("newCvu");
        savedAccount.setBalance(150.0);
        savedAccount.setName("New Name");
        savedAccount.setUserId("newUserId");

        when(cuentaRepository.findById(data.id())).thenReturn(Optional.of(existingAccount));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(savedAccount);  // Return the updated account

        // Act
        Cuenta updatedAccount = cuentaService.updateAccount(userId, data);

        // Assert
        assertEquals(data.alias(), updatedAccount.getAlias());
        assertEquals(data.cvu(), updatedAccount.getCvu());
        assertEquals(data.balance(), updatedAccount.getBalance());
        assertEquals(data.name(), updatedAccount.getName());
        assertEquals(data.userId(), updatedAccount.getUserId());

        verify(cuentaRepository, times(1)).findById(data.id());
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
    }

    @Test
    void updateAccount_ShouldThrowResourceBadRequestExceptionWhenIdIsNull() {
        // Arrange
        String userId = "123";
        RecordAccount data = new RecordAccount(null, "new.alias", null, 0.0, null, null);

        // Act & Assert
        assertThrows(ResourceBadRequestException.class, () -> cuentaService.updateAccount(userId, data));
    }

    @Test
    void updateAccount_ShouldThrowResourceNotFoundExceptionWhenAccountNotFound() {
        // Arrange
        String userId = "123";
        RecordAccount data = new RecordAccount("456", "new.alias", null, 0.0, null, null);
        when(cuentaRepository.findById(data.id())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cuentaService.updateAccount(userId, data));
    }

    @Test
    void getAccountByAccountId_ShouldReturnRecordAccount() throws ResourceNotFoundException {
        // Arrange
        String accountId = "123";
        Cuenta account = new Cuenta();
        account.setId(accountId);
        account.setAlias("test.alias");
        account.setCvu("testCvu");
        account.setBalance(100.0);
        account.setName("Test Name");
        account.setUserId("testUserId");

        when(cuentaRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        RecordAccount recordAccount = cuentaService.getAccountByAccountId(accountId);

        // Assert
        assertNotNull(recordAccount);
        assertEquals(account.getId(), recordAccount.id());
        assertEquals(account.getAlias(), recordAccount.alias());
        assertEquals(account.getCvu(), recordAccount.cvu());
        assertEquals(account.getBalance(), recordAccount.balance());
        assertEquals(account.getName(), recordAccount.name());
        assertEquals(account.getUserId(), recordAccount.userId());

        verify(cuentaRepository).findById(accountId);
    }

    @Test
    void getAccountByAccountId_ShouldThrowResourceNotFoundException() {
        // Arrange
        String accountId = "123";
        when(cuentaRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cuentaService.getAccountByAccountId(accountId));
        verify(cuentaRepository).findById(accountId);
    }


    @Test
    void updateAccountBalance_ShouldUpdateBalanceSuccessfully() throws Exception {
        // Arrange
        String userId = "user123";
        String accountId = "account456";
        double newBalance = 200.0;
        RecordAccount data = new RecordAccount(accountId, "new.alias", "newCvu", newBalance, "New Name", userId);

        Cuenta existingAccount = new Cuenta();
        existingAccount.setId(accountId);
        existingAccount.setUserId(userId);
        existingAccount.setBalance(100.0); // Initial balance

        Cuenta updatedAccount = new Cuenta(); // Create a populated Cuenta object for the result
        updatedAccount.setId(accountId);
        updatedAccount.setUserId(userId);
        updatedAccount.setBalance(newBalance); //Updated balance

        when(cuentaRepository.findByUserId(userId)).thenReturn(Optional.of(existingAccount));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(updatedAccount); // Return modified

        // Act
        Cuenta result = cuentaService.updateAccountBalance(accountId, data);

        // Assert
        assertEquals(newBalance, result.getBalance());  // Check new balance
        verify(cuentaRepository, times(1)).findByUserId(userId);
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
    }


    @Test
    void updateAccountBalance_ShouldThrowResourceNotFoundException_WhenAccountNotFound() {
        // Arrange
        String nonExistentUserId = "nonExistentUser";
        String accountId = "123";
        RecordAccount data = new RecordAccount(accountId, "new.alias", null, 200.0, null, nonExistentUserId);

        when(cuentaRepository.findByUserId(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cuentaService.updateAccountBalance(accountId, data));

        verify(cuentaRepository, times(1)).findByUserId(nonExistentUserId);
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }
}