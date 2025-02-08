package Cards.feignClients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "api-usuario")
public interface UserFeignClient {
    @GetMapping("/users/{userId}")
    ResponseEntity<Map> getUserData(@PathVariable String userId, @RequestHeader("Authorization") String accessToken);
}

