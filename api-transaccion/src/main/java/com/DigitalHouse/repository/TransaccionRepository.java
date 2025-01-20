package com.DigitalHouse.repository;

import com.DigitalHouse.entity.Transaccion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TransaccionRepository extends CrudRepository<Transaccion,Long> {

    List<Transaccion> findByAccountIdAndType(Long accountId, String type);

}
