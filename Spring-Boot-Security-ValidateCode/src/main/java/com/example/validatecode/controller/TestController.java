package com.example.validatecode.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dengzhiming
 * @date 2020/2/18 10:09
 */
@RestController
public class TestController {
    @GetMapping("/hello")
    public String hello(){
        return "hello spring security";
    }

    @GetMapping("index")
    public Object index(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /*@GetMapping("index")
    public Object index(Authentication authentication) {
        return authentication;
    }*/
}
