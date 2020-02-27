
```text
1.Spring Security默认的退出登录URL为/logout,退出登录后,Spring Security会做如下处理:
    [1]使当前的Sesion失效;
    [2]清除与当前用户关联的RememberMe记录;
    [3]清空当前的SecurityContext;
    [4]重定向到登录页;
2.自定义退出登录行为:
    [1]修改配置类BrowserSecurityConfig:
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // 添加验证码校验过滤器
            http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                // 添加短信验证码校验过滤器
                .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                .formLogin()                                // 表单登录
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
                    .antMatchers("/login.html","/css/**",
                            "/code/image","/code/sms","/session/invalid","/signout/success").permitAll()
                    .anyRequest()    // 所有请求
                    .authenticated() // 都需要认证
                .and()
                    .sessionManagement()                    // 添加 Session管理器
                    .invalidSessionUrl("/session/invalid")  // Session失效后跳转到这个链接
                    .maximumSessions(1)
                    //.maxSessionsPreventsLogin(true)
                    .expiredSessionStrategy(sessionExpiredStrategy)
                .and()
                .and()
                    .logout()
                    .logoutUrl("/signout")
                    .logoutSuccessUrl("/signout/success")
                    .deleteCookies("JSESSIONID")
                .and().csrf().disable()
                // 将短信验证码认证配置加到Spring Security中
                .apply(smsAuthenticationConfig); 
        }
        (1)将请求/signout/success"加入免认证路径;
        (2)配置退出登录的URL为"/signout";
        (3)配置退出成功后跳转的URL为"/signout/success";
        (4)配置退出成功后删除名称为"JSESSIONID"的cookie;
    [2]在Controller中添加退出成功后的请求方法:
        @GetMapping("/signout/success")
        public String signout() {
            return "退出成功，请重新登录";
        }
3.通过logoutSuccessHandler代替logoutUrl处理退出成功后的逻辑:
    [1]自定义实现LogoutSuccessHandler:
        @Component
        public class MyLogOutSuccessHandler implements LogoutSuccessHandler {
            @Override
            public void onLogoutSuccess(HttpServletRequest httpServletRequest, 
                    HttpServletResponse httpServletResponse,Authentication authentication) 
                    throws IOException, ServletException {
                httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpServletResponse.setContentType("application/json;charset=utf-8");
                httpServletResponse.getWriter().write("退出成功，请重新登录");
            }
        }
    [2]修改配置类BrowserSecurityConfig:
        @Autowired
        private MyLogOutSuccessHandler logOutSuccessHandler;
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // 添加验证码校验过滤器
            http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                // 添加短信验证码校验过滤器
                .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                .formLogin()                                // 表单登录
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
                    .antMatchers("/login.html","/css/**",
                            "/code/image","/code/sms","/session/invalid","/signout/success").permitAll()
                    .anyRequest()    // 所有请求
                    .authenticated() // 都需要认证
                .and()
                    .sessionManagement()                    // 添加 Session管理器
                    .invalidSessionUrl("/session/invalid")  // Session失效后跳转到这个链接
                    .maximumSessions(1)
                    //.maxSessionsPreventsLogin(true)
                    .expiredSessionStrategy(sessionExpiredStrategy)
                .and()
                .and()
                    .logout()
                    .logoutUrl("/signout")
                    //.logoutSuccessUrl("/signout/success")
                    .logoutSuccessHandler(logOutSuccessHandler)
                    .deleteCookies("JSESSIONID")
                .and().csrf().disable()
                // 将短信验证码认证配置加到Spring Security中
                .apply(smsAuthenticationConfig); 
        }
```
