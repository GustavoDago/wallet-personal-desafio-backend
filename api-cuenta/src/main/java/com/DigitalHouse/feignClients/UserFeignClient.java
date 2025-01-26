package com.DigitalHouse.feignClients;


import com.DigitalHouse.Entity.Cuenta;
import org.keycloak.admin.client.resource.UserResource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "api-usuario")
public interface UserFeignClient {
    @PostMapping("/users/{userId}/accounts")
    ResponseEntity<Cuenta> createAccount(@PathVariable String userId, @RequestHeader("Authorization") String accessToken, Cuenta cuenta);

    @GetMapping("/userName/{userId}")
    ResponseEntity<String> obtenerUserName (@PathVariable("userId") String userId, @RequestHeader("Authorization") String  accessToken);
}

