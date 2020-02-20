package com.example.start.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dengzhiming
 * @date 2020/2/18 10:09
 */
@RestController
public class TestController {
    @GetMapping("hello")
    public String hello(){
        return "hello spring security";
    }
}
