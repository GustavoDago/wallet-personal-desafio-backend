package com.DigitalHouse.feign;


import DPB.Transactions.model.Account;
import DPB.Transactions.model.RecordAccount;
import com.DigitalHouse.Entity.Cuenta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-service")
public interface AccountFeignClient {
    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<Cuenta> getAccount(@PathVariable String userId,
                                             @RequestHeader("Authorization") String accessToken);
    @PutMapping("/users/{userId}/accounts/{accountId}")
    public ResponseEntity<?> updateAccountBalance(@PathVariable String userId,
                                           @PathVariable String accountId,
                                           @RequestBody RecordAccount account,
                                           @RequestHeader("Authorization") String accessToken);
    @GetMapping("/account/{accountId}")
    public RecordAccount findAccount (@PathVariable String accountId);
}