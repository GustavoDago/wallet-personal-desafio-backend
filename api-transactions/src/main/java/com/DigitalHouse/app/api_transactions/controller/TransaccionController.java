package com.DigitalHouse.app.api_transactions.controller;


import com.DigitalHouse.app.api_transactions.entity.Transaction;
import com.DigitalHouse.app.api_transactions.exceptions.NotApprovedTransaction;
import com.DigitalHouse.app.api_transactions.exceptions.ResourceNotFoundException;
import com.DigitalHouse.app.api_transactions.records.CreateActivityRequest;
import com.DigitalHouse.app.api_transactions.records.RecordTransaction;
import com.DigitalHouse.app.api_transactions.service.TransaccionServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping()
public class TransaccionController {
    @Autowired
    private TransaccionServiceImp transactionService;

    public TransaccionController(TransaccionServiceImp transaccionService) {
        this.transactionService = transaccionService;
    }

    @GetMapping("/users/{userId}/activities")
    public ResponseEntity<List<Transaction>> getUserActivities(
            @PathVariable String userId,
            @RequestHeader("Authorization") String token,
            @RequestParam Optional<Integer> limit) {
        List<Transaction> activities = transactionService.getUserActivities(userId, token, limit);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/users/{userId}/activities/")
    public ResponseEntity<RecordTransaction> getUserActivities(
            @PathVariable String userId,
            @RequestHeader("Authorization") String token,
            @PathVariable String activityId) throws ResourceNotFoundException {
        RecordTransaction activity = transactionService.getUserActivity(userId, token, activityId);
        return ResponseEntity.ok(activity);
    }

    @PostMapping("/users/{userId}/activities")
    public ResponseEntity<?> createActivity(
            @PathVariable String userId,
            @RequestBody CreateActivityRequest activityRequest,
            @RequestHeader("Authorization") String token) throws ResourceNotFoundException, NotApprovedTransaction {


        try {
            Transaction transaction;
            if ("Deposit".equals(activityRequest.type().getType())) {
                transaction = transactionService.createDeposit(userId, activityRequest, token);

            } else if ("Transfer".equals(activityRequest.type().getType())) {
                transaction = transactionService.createTransfer(userId, activityRequest, token);
            } else {
                return ResponseEntity.badRequest().body("Invalid activity type");
            }
            return ResponseEntity.ok(transaction);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (NotApprovedTransaction e) {
            throw new NotApprovedTransaction(e.getMessage());
        }

    }
}
