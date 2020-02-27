package com.example.session.security;

import com.example.session.handler.MyAuthenticationFailureHandler;
import com.example.session.handler.MyAuthenticationSucessHandler;
import com.example.session.validate.graphicscode.ValidateCodeFilter;
import com.example.session.validate.smscode.SmsAuthenticationConfig;
import com.example.session.validate.smscode.SmsCodeFilter;
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
    @Autowired
    private MySessionExpiredStrategy sessionExpiredStrategy;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
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
                    // 无需认证的请求路径
                    .antMatchers("/login.html","/css/**","/code/image","/code/sms","/session/invalid").permitAll()
                    .anyRequest()    // 所有请求
                    .authenticated() // 都需要认证
                .and()
                    .sessionManagement()                    // 添加 Session管理器
                    .invalidSessionUrl("/session/invalid")  // Session失效后跳转到这个链接
                    .maximumSessions(1)
                    //.maxSessionsPreventsLogin(true)
                    .expiredSessionStrategy(sessionExpiredStrategy)
                .and()
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
