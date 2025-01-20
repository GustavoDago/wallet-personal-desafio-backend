package com.DigitalHouse.Repository;

import com.DigitalHouse.Entity.Cuenta;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaRepository extends CrudRepository<Cuenta,Long> {
    Optional<Cuenta> findByUserId(Long userId);
}
