package com.DigitalHouse.Service;

import com.DigitalHouse.Entity.AliasGenerator;
import com.DigitalHouse.Entity.Cuenta;
import com.DigitalHouse.Repository.CuentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class CuentaServiceImp implements CuentaService{

    @Autowired
    private CuentaRepository cuentaRepository;

    @Override
    public List<Cuenta> listarCuentas() {
        return (List<Cuenta>) cuentaRepository.findAll();
    }

    @Override
    public Cuenta crearCuenta(Cuenta cuenta) {
        if (cuenta.getAlias() == null || cuenta.getAlias().isEmpty()) {
            cuenta.setAlias(AliasGenerator.generateAlias());
        }
        return cuentaRepository.save(cuenta);
    }

    @Override
    public Optional<Cuenta> obtenerCuentaPorId(Long id) {
        return cuentaRepository.findById(id);
    }

    @Override
    public Cuenta actualizarCuenta(Long id, Cuenta cuenta) {
        return cuentaRepository.findById(id).map(c -> {
            c.setAlias(cuenta.getAlias());
            c.setBalance(cuenta.getBalance());
            return cuentaRepository.save(c);
        }).orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
    }

    @Override
    public void eliminarCuenta(Long id) {
        if (!cuentaRepository.existsById(id)) {
            throw new RuntimeException("Cuenta no encontrada");
        }
        cuentaRepository.deleteById(id);
    }

    @Override
    public void actualizarAlias(Long userId, String newAlias) {
        Optional<Cuenta> cuentaBuscada = cuentaRepository.findByUserId(userId);
        if (cuentaBuscada.isPresent()) {
            Cuenta cuenta = cuentaBuscada.get();
            cuenta.setAlias(newAlias);
            cuentaRepository.save(cuenta);
        } else {
            throw new RuntimeException("Cuenta no encontrada para el usuario con ID: " + userId);
        }
    }
}
