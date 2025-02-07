package com.DigitalHouse.repository;

import com.DigitalHouse.entity.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction,String > {

    List<Transaction> findByCuentaIdAndTipo(Long accountId, String type);
    List<Transaction> findAllByUserId(String userId);
}
