package com.example.authentication.config;

import com.example.authentication.handler.MyAuthenticationFailureHandler;
import com.example.authentication.handler.MyAuthenticationSucessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author dengzhiming
 * @date 2020/2/18 13:24
 */
@Configuration
public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private MyAuthenticationSucessHandler authenticationSucessHandler;
    @Autowired
    private MyAuthenticationFailureHandler authenticationFailureHandler;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /*http.formLogin()                  // 表单方式
                //http.httpBasic()
                .and()
                .authorizeRequests()        // 授权配置
                .anyRequest()               // 所有请求
                .authenticated();           // 都需要认证*/
        /*http.formLogin()                        // 表单方式
                // http.httpBasic()             // HTTP Basic
                .loginPage("/login.html")       // 登录页
                .loginProcessingUrl("/login")   // 登录请求
                .and()
                .authorizeRequests()            // 授权配置
                .antMatchers("/login.html","/css/**").permitAll()
                .anyRequest()                   // 所有请求
                .authenticated()                // 都需要认证
                .and()
                .csrf().disable();              // 关闭CSRF攻击防御*/
        /*http.formLogin()                                // 表单登录
                // http.httpBasic() // HTTP Basic
                .loginPage("/authentication/require")   // 登录跳转 URL
                .loginProcessingUrl("/login")           // 处理表单登录 URL
                .and()
                .authorizeRequests()                    // 授权配置
                .antMatchers("/authentication/require", "/login.html","/css/**").permitAll() // 登录跳转 URL 无需认证
                .anyRequest()                           // 所有请求
                .authenticated()                        // 都需要认证
                .and().csrf().disable();                // 关闭CSRF攻击防御*/
        /*http.formLogin()                                // 表单登录
                // http.httpBasic() // HTTP Basic
                .loginPage("/authentication/require")   // 登录跳转 URL
                .loginProcessingUrl("/login")           // 处理表单登录 URL
                .successHandler(authenticationSucessHandler) // 处理登录成功
                .and()
                .authorizeRequests()                    // 授权配置
                .antMatchers("/authentication/require", "/login.html","/css/**").permitAll() // 登录跳转 URL 无需认证
                .anyRequest()                           // 所有请求
                .authenticated()                        // 都需要认证
                .and().csrf().disable();                // 关闭CSRF攻击防御*/
        /*http.formLogin()                                // 表单登录
                // http.httpBasic()                     // HTTP Basic
                .loginPage("/login.html")               // 登录跳转 URL
                .loginProcessingUrl("/login")           // 处理表单登录 URL
                .successHandler(authenticationSucessHandler) // 处理登录成功
                .and()
                .authorizeRequests()                    // 授权配置
                .antMatchers( "/login.html","/css/**").permitAll() // 登录跳转 URL 无需认证
                .anyRequest()                           // 所有请求
                .authenticated()                        // 都需要认证
                .and().csrf().disable();                // 关闭CSRF攻击防御*/
        http.formLogin()                                // 表单登录
                // http.httpBasic()                     // HTTP Basic
                .loginPage("/login.html")               // 登录跳转 URL
                .loginProcessingUrl("/login")           // 处理表单登录 URL
                .successHandler(authenticationSucessHandler) // 处理登录成功
                .failureHandler(authenticationFailureHandler) // 处理登录失败
                .and()
                .authorizeRequests()                    // 授权配置
                .antMatchers( "/login.html","/css/**").permitAll() // 登录跳转 URL 无需认证
                .anyRequest()                           // 所有请求
                .authenticated()                        // 都需要认证
                .and().csrf().disable();                // 关闭CSRF攻击防御

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 该加密对象对相同的密码加密后可生成不同的结果
        return new BCryptPasswordEncoder();
    }
}
