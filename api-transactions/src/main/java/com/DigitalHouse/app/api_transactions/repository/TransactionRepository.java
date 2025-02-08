package com.DigitalHouse.app.api_transactions.repository;


import com.DigitalHouse.app.api_transactions.entity.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction,String > {

    List<Transaction> findAllByUserId(String userId);
}
