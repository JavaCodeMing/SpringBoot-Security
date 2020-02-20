package com.example.start.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author dengzhiming
 * @date 2020/2/18 13:24
 */
@Configuration
public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()                // 表单方式
        //http.httpBasic()
                .and()
                .authorizeRequests()    // 授权配置
                .anyRequest()           // 所有请求
                .authenticated();       // 都需要认证
    }
}
