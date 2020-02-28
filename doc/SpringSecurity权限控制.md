
# ��ȫע��
```text
1.Spring Security�ṩ�����ֲ�ͬ�İ�ȫע��:
    [1]Spring Security�Դ���@Securedע��;
    [2]JSR-250��@RolesAllowedע��;
    [3]���ʽ������ע��,����@PreAuthorize��@PostAuthorize��@PreFilter�� @PostFilter;
2.@Securedע���ʹ��:
    [1]��Spring-Security.xml������@Securedע��:
        <global-method-security secured-annotations="enabled"/>
    [2]����: (ֻ��ӵ��Ȩ��"ADMIN"���û����ܷ���)
        @Secured("ROLE_ADMIN")
        public void test(){
            ...
        }
        (Ȩ�޲���ʱ,�����׳�Access Denied�쳣)
    [3]@Securedע����ʹ��һ��String������Ϊ����:(������Ҫ�߱����е�һ��Ȩ�޲ſɷ���)
        @Secured({"ROLE_ADMIN","ROLE_USER"})
        public void test(){
            ...
        }
3.@RolesAllowedע���ʹ��:
    [1]��Spring-Security.xml������@RolesAllowedע��:
        <global-method-security jsr250-annotations="enabled"/>
    [2]����: (ֻ��ӵ��Ȩ�ޡ�ROLE_ADMIN�����û����ܷ���)
        @RolesAllowed("ROLE_ADMIN")
        public void test(){
            ...
        }
4.SpELע��:
    [1]��Spring-Security.xml������SpELע��:
        <global-method-security pre-post-annotations="enabled"/>
    [2]Spring Security֧�ֵ�����SpEL���ʽ:
        (1)authentication: �û���֤����;
        (2)denyAll: ���ʼ��Ϊfalse;
        (3)hasAnyRole(list of roles): ����û�����Ȩָ��������Ȩ��,���Ϊtrue;
        (4)hasRole(role): ����û���������ָ����Ȩ��,��� Ϊtrue;
        (5)hasIpAddress(IP Adress): �û���ַ;
        (6)isAnonymous(): �Ƿ�Ϊ�����û�;
        (7)isAuthenticated(): ���������û�;
        (8)isFullyAuthenticated: ��������Ҳ����remember-me��֤;
        (9)isRemberMe(): remember-me��֤;
        (10)permitAll: ʼ��Ϊtrue;
        (11)principal: �û���Ҫ��Ϣ����;
    [3]@PreAuthorizeע��: (���ڷ���ǰ��֤Ȩ��)
        @PreAuthorize("hasRole('ROLE_ADMIN') and #form.note.length() <= 1000 or hasRole('ROLE_VIP')")
        public void writeBlog(Form form){
            ...
        }
        (1)��Ҫ��"ROLE_ADMIN"Ȩ���ұ���note�ֶ��������ó���1000��,������"ROLE_VIP"Ȩ��;
        (2)Spring Security�ܹ���鴫�뷽���Ĳ���,���ʽ�е�#form���־�ֱ�������˷����е�ͬ������;
    [4]@PostAuthorizeע��: (���ڷ�������֤Ȩ��)
        @PostAuthorize("returnObject.user.userName == principal.username")
        public User getUserById(long id){
            ...        
        }
        (1)Spring Security��SpEL���ṩ����ΪreturnObject�ı���,���Ի�ȡ���ض���user;
        (2)�ж�user����username�Ƿ�ͷ��ʸ÷������û�������û���һ��,��һ�����׳��쳣;
    [5]@PreFilterע��: (�ڽ��뷽��ǰ��������ֵ)
        @PreFilter("filterObject.username == 'testuser2'")
        public User getUserById(long id){
            ...        
        }
        (1)Spring Security��SpEL���ṩ����ΪfilterObject�ı���,ָ���Ƿ��ؼ����еĵ�ǰ����;
    [6]@PostFilterע��: (�ڷ���ִ�к���˽��)
        @PostFilter("filterObject.sex == '��' ")
        public List<User> getUserList(){
            ...
        }
```
# Ȩ�޿���
```text
1.��SpringBoot�п���Spring Security�İ�ȫע��:
    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
        ...
    }
2.�޸�UserDetailService: (�����ض��û�"admin"��Ȩ��,�����û�"test"Ȩ��)
    @Configuration
    public class UserDetailService implements UserDetailsService {
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Override
        public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
            // ģ��һ���û�,������ݿ��ȡ�߼�
            MyUser user = new MyUser();
            user.setUserName(s);
            user.setPassword(passwordEncoder.encode("123456"));
            // ������ܺ������
            System.out.println(user.getPassword());
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (StringUtils.equalsIgnoreCase("kimi", s)) {
                authorities = AuthorityUtils.commaSeparatedStringToAuthorityList("admin");
            } else {
                authorities = AuthorityUtils.commaSeparatedStringToAuthorityList("test");
            }
            return new User(user.getUserName(), user.getPassword(), user.isEnabled(),
                user.isAccountNonExpired(), user.isCredentialsNonExpired(),
                user.isAccountNonLocked(), authorities);
        }
    }    
3.��Controller�����һ������: (������֤Ȩ�޵�¼)
    @GetMapping("/auth/admin")
    @PreAuthorize("hasAuthority('admin')")
    public String authenticationTest() {
        return "��ӵ��adminȨ�ޣ����Բ鿴";
    }
    [1]����ϵͳ,ʹ��"kimi"�˺ŵ�¼,��¼�ɹ����ٷ���"/auth/admin",����������;
    [2]�˳���¼,ʹ�������˺ŵ�¼,��¼�ɹ����ٷ���"/auth/admin",��Ӧ����ҳ�沢����403������;
4.�Զ���Ȩ�޲��㴦����: (������Ȩ�޲���ʱ���д������)
    [1]����һ��������MyAuthenticationAccessDeniedHandler:
        @Component
        public class MyAuthenticationAccessDeniedHandler implements AccessDeniedHandler {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                    AccessDeniedException accessDeniedException) throws IOException {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("�ܱ�Ǹ����û�и÷���Ȩ��");
            }
        }
    [2]�޸�������BrowserSecurityConfig:
        @Autowired
        private MyAuthenticationAccessDeniedHandler authenticationAccessDeniedHandler;
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.exceptionHandling()
                    .accessDeniedHandler(authenticationAccessDeniedHandler)
                    .and()
                        // �����֤��У�������
                        .addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                        // ��Ӷ�����֤��У�������
                        .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                    .formLogin()                                        // ����¼
                        .loginPage("/login.html")                       // ��¼��ת URL
                        .loginProcessingUrl("/login")                   // �������¼ URL
                        .successHandler(authenticationSucessHandler)    // �����¼�ɹ�
                        .failureHandler(authenticationFailureHandler)   // �����¼ʧ��
                    .and()
                        .rememberMe()
                        .tokenRepository(persistentTokenRepository())
                        .tokenValiditySeconds(3600)
                        .userDetailsService(userDetailService)
                    .and()
                        .authorizeRequests()                            // ��Ȩ����
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
