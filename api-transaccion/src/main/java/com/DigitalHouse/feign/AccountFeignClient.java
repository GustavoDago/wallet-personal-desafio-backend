package com.DigitalHouse.feign;

import com.DigitalHouse.Entity.Cuenta;
import com.DigitalHouse.records.RecordAccount;
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