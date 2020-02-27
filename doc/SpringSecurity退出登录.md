
```text
1.Spring SecurityĬ�ϵ��˳���¼URLΪ/logout,�˳���¼��,Spring Security�������´���:
    [1]ʹ��ǰ��SesionʧЧ;
    [2]����뵱ǰ�û�������RememberMe��¼;
    [3]��յ�ǰ��SecurityContext;
    [4]�ض��򵽵�¼ҳ;
2.�Զ����˳���¼��Ϊ:
    [1]�޸�������BrowserSecurityConfig:
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // �����֤��У�������
            http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                // ��Ӷ�����֤��У�������
                .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                .formLogin()                                // ����¼
                    .loginPage("/login.html")               // ��¼��ת URL
                    .loginProcessingUrl("/login")           // �������¼ URL
                    .successHandler(authenticationSucessHandler)  // �����¼�ɹ�
                    .failureHandler(authenticationFailureHandler) // �����¼ʧ��
                .and()
                    .rememberMe()
                    .tokenRepository(persistentTokenRepository())
                    .tokenValiditySeconds(3600)
                    .userDetailsService(userDetailService)
                .and()
                    .authorizeRequests()                    // ��Ȩ����
                    // ������֤������·��
                    .antMatchers("/login.html","/css/**",
                            "/code/image","/code/sms","/session/invalid","/signout/success").permitAll()
                    .anyRequest()    // ��������
                    .authenticated() // ����Ҫ��֤
                .and()
                    .sessionManagement()                    // ��� Session������
                    .invalidSessionUrl("/session/invalid")  // SessionʧЧ����ת���������
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
                // ��������֤����֤���üӵ�Spring Security��
                .apply(smsAuthenticationConfig); 
        }
        (1)������/signout/success"��������֤·��;
        (2)�����˳���¼��URLΪ"/signout";
        (3)�����˳��ɹ�����ת��URLΪ"/signout/success";
        (4)�����˳��ɹ���ɾ������Ϊ"JSESSIONID"��cookie;
    [2]��Controller������˳��ɹ�������󷽷�:
        @GetMapping("/signout/success")
        public String signout() {
            return "�˳��ɹ��������µ�¼";
        }
3.ͨ��logoutSuccessHandler����logoutUrl�����˳��ɹ�����߼�:
    [1]�Զ���ʵ��LogoutSuccessHandler:
        @Component
        public class MyLogOutSuccessHandler implements LogoutSuccessHandler {
            @Override
            public void onLogoutSuccess(HttpServletRequest httpServletRequest, 
                    HttpServletResponse httpServletResponse,Authentication authentication) 
                    throws IOException, ServletException {
                httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpServletResponse.setContentType("application/json;charset=utf-8");
                httpServletResponse.getWriter().write("�˳��ɹ��������µ�¼");
            }
        }
    [2]�޸�������BrowserSecurityConfig:
        @Autowired
        private MyLogOutSuccessHandler logOutSuccessHandler;
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // �����֤��У�������
            http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                // ��Ӷ�����֤��У�������
                .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                .formLogin()                                // ����¼
                    .loginPage("/login.html")               // ��¼��ת URL
                    .loginProcessingUrl("/login")           // �������¼ URL
                    .successHandler(authenticationSucessHandler)  // �����¼�ɹ�
                    .failureHandler(authenticationFailureHandler) // �����¼ʧ��
                .and()
                    .rememberMe()
                    .tokenRepository(persistentTokenRepository())
                    .tokenValiditySeconds(3600)
                    .userDetailsService(userDetailService)
                .and()
                    .authorizeRequests()                    // ��Ȩ����
                    // ������֤������·��
                    .antMatchers("/login.html","/css/**",
                            "/code/image","/code/sms","/session/invalid","/signout/success").permitAll()
                    .anyRequest()    // ��������
                    .authenticated() // ����Ҫ��֤
                .and()
                    .sessionManagement()                    // ��� Session������
                    .invalidSessionUrl("/session/invalid")  // SessionʧЧ����ת���������
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
                // ��������֤����֤���üӵ�Spring Security��
                .apply(smsAuthenticationConfig); 
        }
```
