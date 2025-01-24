package com.DigitalHouse.feignClients;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "account-service")
public interface AccountFeignClient {

}
