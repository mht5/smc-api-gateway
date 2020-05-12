package com.smc.service;

import com.smc.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
@Service
public interface FeignUserService {

    @GetMapping("/find-by-id")
    User findUserById(@RequestParam int id);

}
