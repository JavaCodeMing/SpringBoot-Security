package com.example.smscode.security;

import com.example.smscode.handler.MyAuthenticationFailureHandler;
import com.example.smscode.handler.MyAuthenticationSucessHandler;
import com.example.smscode.validate.graphicscode.ValidateCodeFilter;
import com.example.smscode.validate.smscode.SmsAuthenticationConfig;
import com.example.smscode.validate.smscode.SmsCodeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

/**
 * @author dengzhiming
 * @date 2020/2/18 13:24
 */
@Configuration
public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailService userDetailService;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private ValidateCodeFilter validateCodeFilter;
    @Autowired
    private SmsCodeFilter smsCodeFilter;
    @Autowired
    private SmsAuthenticationConfig smsAuthenticationConfig;
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
        /*http.formLogin()                                // 表单登录
                // http.httpBasic()                     // HTTP Basic
                .loginPage("/login.html")               // 登录跳转 URL
                .loginProcessingUrl("/login")           // 处理表单登录 URL
                .successHandler(authenticationSucessHandler)  // 处理登录成功
                .failureHandler(authenticationFailureHandler) // 处理登录失败
                .and()
                .authorizeRequests()                    // 授权配置
                .antMatchers( "/login.html","/css/**","/code/image").permitAll() // 登录跳转 URL 无需认证
                .anyRequest()                           // 所有请求
                .authenticated()                        // 都需要认证
                .and().csrf().disable();                // 关闭CSRF攻击防御*/
        /*http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class) // 添加验证码校验过滤器
                .formLogin()                            // 表单登录
                // http.httpBasic()                     // HTTP Basic
                .loginPage("/login.html")               // 登录跳转 URL
                .loginProcessingUrl("/login")           // 处理表单登录 URL
                .successHandler(authenticationSucessHandler)  // 处理登录成功
                .failureHandler(authenticationFailureHandler) // 处理登录失败
                .and()
                .authorizeRequests()                    // 授权配置
                .antMatchers("/login.html","/css/**","/code/image").permitAll() // 无需认证的请求路径
                .anyRequest()    // 所有请求
                .authenticated() // 都需要认证
                .and().csrf().disable();*/
        http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class) // 添加验证码校验过滤器
                .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class) // 添加短信验证码校验过滤器
                .formLogin()                            // 表单登录
                // http.httpBasic()                     // HTTP Basic
                .loginPage("/login.html")               // 登录跳转 URL
                .loginProcessingUrl("/login")           // 处理表单登录 URL
                .successHandler(authenticationSucessHandler)  // 处理登录成功
                .failureHandler(authenticationFailureHandler) // 处理登录失败
                .and()
                .rememberMe()
                .tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(3600)
                .userDetailsService(userDetailService)
                .and()
                .authorizeRequests()                    // 授权配置
                .antMatchers("/login.html","/css/**","/code/image","/code/sms").permitAll() // 无需认证的请求路径
                .anyRequest()    // 所有请求
                .authenticated() // 都需要认证
                .and().csrf().disable()
                .apply(smsAuthenticationConfig); // 将短信验证码认证配置加到Spring Security中
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 该加密对象对相同的密码加密后可生成不同的结果
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        jdbcTokenRepository.setCreateTableOnStartup(false);
        return jdbcTokenRepository;
    }
}
