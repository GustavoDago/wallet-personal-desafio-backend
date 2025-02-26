package com.DigitalHouse.app.api_transactions.feign;



import com.DigitalHouse.app.api_transactions.records.RecordAccount;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "api-cuenta")
public interface CuentaFeignClient {
    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<?> getAccount(@PathVariable String userId,
                                             @RequestHeader("Authorization") String accessToken);
    @PutMapping("/users/{userId}/accounts/{accountId}")
    public ResponseEntity<?> updateAccountBalance(@PathVariable String userId,
                                           @PathVariable String accountId,
                                           @RequestBody RecordAccount account,
                                           @RequestHeader("Authorization") String accessToken);
    @GetMapping("/account/{accountId}")
    public RecordAccount findAccount (@PathVariable String accountId);

    @GetMapping("account/AccountByUserId/{userId}")
    public String getAccountIdByUserId(@PathVariable String userId,
                                       @RequestHeader("Authorization") String accessToken);
}