package com.DigitalHouse.Service;


import com.DigitalHouse.Entity.AliasGenerator;
import com.DigitalHouse.Entity.Cuenta;
import com.DigitalHouse.Entity.RecordAccount;
import com.DigitalHouse.Repository.CuentaRepository;
import com.DigitalHouse.components.CvuGenerator;
import com.DigitalHouse.exceptions.ResourceAlreadyExistsException;
import com.DigitalHouse.exceptions.ResourceBadRequestException;
import com.DigitalHouse.exceptions.ResourceNotFoundException;
import com.DigitalHouse.feignClients.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
public class CuentaServiceImp implements CuentaService{

    @Autowired
    private CvuGenerator cvuGenerator;

    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private CuentaRepository cuentaRepository;

    @Override
    public List<Cuenta> listarCuentas() {
        return (List<Cuenta>) cuentaRepository.findAll();
    }

    @Override
    public Cuenta crearCuenta(String userId, String accessToken) throws ResourceAlreadyExistsException {
        if (cuentaRepository.findByUserId(userId).isPresent())
            throw new ResourceAlreadyExistsException("El usuario ya tiene una cuenta");

        Cuenta account = new Cuenta();

        String alias = AliasGenerator.generateAlias();
        String cvu = cvuGenerator.generateCvu();

        account.setAlias(alias);
        account.setCvu(cvu);
        account.setBalance(0);
        account.setUserId(userId);

        // 1. Get User Data from Keycloak (using Feign Client)
        ResponseEntity<String> userName = userFeignClient.obtenerUserName(userId, accessToken);

        if (!userName.getBody().isEmpty()) {
            account.setName(userName.getBody());
        } else {
            throw new RuntimeException("Error al obtener los datos del usuario");
        }

        // Guardar la cuenta en el repositorio
        Cuenta savedAccount = cuentaRepository.save(account);

        if (savedAccount != null) {
            return savedAccount;
        } else {
            throw new RuntimeException("Error al crear la cuenta");
        }
    }

    @Override
    public Optional<Cuenta> obtenerCuentaPorId(String  id) {
        return cuentaRepository.findById(id);
    }

    @Override
    public Cuenta actualizarCuenta(String  id, Cuenta cuenta) {
        return cuentaRepository.findById(id).map(c -> {
            c.setAlias(cuenta.getAlias());
            c.setBalance(cuenta.getBalance());
            return cuentaRepository.save(c);
        }).orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
    }

    @Override
    public void eliminarCuenta(String  id) {
        if (!cuentaRepository.existsById(id)) {
            throw new RuntimeException("Cuenta no encontrada");
        }
        cuentaRepository.deleteById(id);
    }

    @Override
    public void actualizarAlias(String  userId, String newAlias) {
        Optional<Cuenta> cuentaBuscada = cuentaRepository.findByUserId(userId);
        if (cuentaBuscada.isPresent()) {
            Cuenta cuenta = cuentaBuscada.get();
            cuenta.setAlias(newAlias);
            cuentaRepository.save(cuenta);
        } else {
            throw new RuntimeException("Cuenta no encontrada para el usuario con ID: " + userId);
        }
    }

    @Override
    public Cuenta getAccountByUserId(String userId) {
        return cuentaRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("No account found for userId: " + userId));    }

    @Override
    public Cuenta updateAccount(String userId, RecordAccount data) throws ResourceBadRequestException, ResourceNotFoundException {
        if (data.id()==null) throw new ResourceBadRequestException("No existe esa cuenta");

        Cuenta account = cuentaRepository.findById(data.id())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        // Actualizar los campos de la cuenta con los datos recibidos
        if (data.alias() != null) account.setAlias(data.alias());
        if (data.cvu() != null) account.setCvu(data.cvu());
        account.setBalance(data.balance());
        if (data.name() != null) account.setName(data.name());
        if (data.userId() != null) account.setUserId(data.userId());
        return cuentaRepository.save(account);
    }
}
