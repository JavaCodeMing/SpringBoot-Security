
```text
Spring Security֧�������Զ�����֤�Ĺ���,�紦���û���Ϣ��ȡ�߼�,ʹ�������Զ����
��¼ҳ���滻Spring SecurityĬ�ϵĵ�¼ҳ���Զ����¼�ɹ���ʧ�ܺ�Ĵ����߼���;
```
# �Զ�����֤����
```text
1.�Զ�����֤�Ĺ�����Ҫʵ��Spring Security�ṩ��UserDetailService�ӿ�:
    public interface UserDetailsService {
        // Ψһ�ĳ��󷽷�: loadUserByUsername
        UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    }
    [1]loadUserByUsername��������һ��UserDetail����:
        // �ýӿڰ���һЩ���������û���Ϣ�ķ���
        public interface UserDetails extends Serializable {
            Collection<? extends GrantedAuthority> getAuthorities();
            String getPassword();
            String getUsername();
            boolean isAccountNonExpired();
            boolean isAccountNonLocked();
            boolean isCredentialsNonExpired();
            boolean isEnabled();
        }
        (1)getAuthorities(): ��ȡ�û�������Ȩ��,����Ȩ�޼���,Ȩ����һ���̳���GrantedAuthority�Ķ���;
        (2)getPassword(): ���ڻ�ȡ����;
        (3)getUsername(): ���ڻ�ȡ�û���;
        (4)isAccountNonExpired(): ����boolean����,�����ж��˻��Ƿ�δ����,δ���ڷ���true��֮����false;
        (5)isAccountNonLocked(): �����ж��˻��Ƿ�δ����;
        (6)isCredentialsNonExpired(): �����ж��û�ƾ֤�Ƿ�û����,�������Ƿ�δ����;
        (7)isEnabled(): �����ж��û��Ƿ����;
    [2]ʵ��ʹ����,��ʹ���Զ���ʵ����,Ҳ��ʹ��Spring Security�ṩ��UserDetails�ӿ�ʵ����User;
2.����ʵ����: (���ڴ��ģ����û�����)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class MyUser implements Serializable {
        private String userName;
        private String password;
        private boolean accountNonExpired = true;
        private boolean accountNonLocked= true;
        private boolean credentialsNonExpired= true;
        private boolean enabled= true;
    }
3.��������������������ܶ���:
    @Bean
    public PasswordEncoder passwordEncoder() {
        // �ü��ܶ������ͬ��������ܺ�����ɲ�ͬ�Ľ��
        return new BCryptPasswordEncoder();
    }
    (PasswordEncoder��һ��������ܽӿ�,��BCryptPasswordEncoder��Spring Security�ṩ��һ��ʵ�ַ���)
4.����MyUserDetailServiceʵ��UserDetailService:
    @Configuration
    public class UserDetailService implements UserDetailsService {
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Override
        public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
            // ģ��һ���û�,������ݿ��ȡ�߼�
            MyUser user = new MyUser();
            user.setUserName(s);
            user.setPassword(this.passwordEncoder.encode("123456"));
            // ������ܺ������
            System.out.println(user.getPassword());
            return new User(s, user.getPassword(), user.isEnabled(),
                user.isAccountNonExpired(), user.isCredentialsNonExpired(),
                user.isAccountNonLocked(), AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));
        }
    }
    [1]Spring Security�ṩ��UserDetails�ӿڵ�ʵ����User:
        User(String username, String password,Collection<? extends GrantedAuthority> authorities)
    [2]AuthorityUtils.commaSeparatedStringToAuthorityList("admin"):
        ģ��һ��admin��Ȩ��,�÷������Խ����ŷָ����ַ���ת��ΪȨ�޼���;
    [3]������߼�: ģ��һ���û�������,����Ϊ123456,�Ҿ���adminȨ�޵��û���¼;
```
# �滻Ĭ�ϵ�¼ҳ
```text
1.�����Զ����¼ҳlogin.html:(��src/main/resources/resourcesĿ¼��)
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <title>��¼</title>
        <link rel="stylesheet" href="./css/login.css" type="text/css">
    </head>
    <body>
    <form class="login-page" action="/login" method="post">
        <div class="form">
            <h3>�˻���¼</h3>
            <input type="text" placeholder="�û���" name="username" required="required"/>
            <input type="password" placeholder="����" name="password" required="required"/>
            <button type="submit">��¼</button>
        </div>
    </form>
    </body>
    </html>
    (�ǵÿ���css�ļ�)
2.�޸�������: (��Spring Security��ת���Զ���ĵ�¼ҳ��)
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()                        // ����ʽ
            // http.httpBasic()             // HTTP Basic
            .loginPage("/login.html")       // ��¼ҳ
            .loginProcessingUrl("/login")   // ��¼����
            .and()
            .authorizeRequests()            // ��Ȩ����
            .antMatchers("/login.html","/css/**").permitAll()
            .anyRequest()                   // ��������
            .authenticated()                // ����Ҫ��֤
            .and().csrf().disable();        // �ر�CSRF��������
    }
    [1].loginPage("/login.html"): ָ������ת����¼ҳ�������URL;
    [2].loginProcessingUrl("/login"): ��Ӧ��¼ҳ��form����action="/login"
    [3].antMatchers("/login.html","/css/**").permitAll():
        (1)��ʾ��ת����¼ҳ������󲻱�����,������������ѭ��;
        (2)��ʾ����cssĿ¼�µľ�̬�ļ���������,����ҳ���޷�������Ⱦ;
    [4].csrf().disable(): ��ʾ�ر�CSRF��������;
3.����������: (��δ��¼�������,���û�����html��Դʱ��ת����¼ҳ,���򷵻�JSON��ʽ����,״̬��Ϊ401)
    [1]�޸�������:
        @Override
        protected void configure(HttpSecurity http) throws Exception {    
            http.formLogin()                            // ����¼
                // http.httpBasic() // HTTP Basic
                .loginPage("/authentication/require")   // ��¼��ת URL
                .loginProcessingUrl("/login")           // �������¼ URL
                .and()
                .authorizeRequests()                    // ��Ȩ����
                // ��¼��ת URL ������֤
                .antMatchers("/authentication/require", "/login.html","/css/**").permitAll()
                .anyRequest()                           // ��������
                .authenticated()                        // ����Ҫ��֤
                .and().csrf().disable();                // �ر�CSRF��������
        }
    [2]���������BrowserSecurityController: (����������ʷ�HTML������)
        @RestController
        public class BrowserSecurityController {
            private RequestCache requestCache = new HttpSessionRequestCache();
            private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
            @GetMapping("/authentication/require")
            @ResponseStatus(HttpStatus.UNAUTHORIZED)
            public String requireAuthentication(HttpServletRequest request, 
                HttpServletResponse response) throws IOException {
                SavedRequest savedRequest = requestCache.getRequest(request, response);
                if (savedRequest != null) {
                    String targetUrl = savedRequest.getRedirectUrl();
                    if (StringUtils.endsWithIgnoreCase(targetUrl, ".html")) {
                        redirectStrategy.sendRedirect(request, response, "/login.html");
                    }
                }
                return "���ʵ���Դ��Ҫ�����֤��";
            }
        }
    [3]���ϴ��������:
        (1)����¼ҳ��������Ϊ"/authentication/require",���".html"��β�����󶼻ᱻ���ص���¼ҳ����;
        (2)���õ�¼ҳ����Ϊ������֤,���".html"��β���������ص���¼ҳ�����,��ͨ����Ӧ�Ŀ�������������;
        (3)BrowserSecurityController: 
            1)HttpSessionRequestCacheΪSpring Security�ṩ�����ڻ�������Ķ���;
            2)ͨ������HttpSessionRequestCache��getRequest�������Ի�ȡ�����������HTTP��Ϣ;
            3)DefaultRedirectStrategy��sendRedirectΪSpring Security�ṩ�����ڴ����ض���ķ���
```
# ����ɹ���ʧ��
```text
1.Spring Security��һ��Ĭ�ϵĴ����¼�ɹ���ʧ�ܵķ���:
    [1]���û���¼�ɹ�ʱ,ҳ�����ת��������¼������,������δ��¼������·���http://localhost:8080/hello,
        ҳ�����ת����¼ҳ,��¼�ɹ�������ת����;
    [2]��¼ʧ��ʱ������ת��Spring SecurityĬ�ϵĴ�����ʾҳ��;
2.�Զ����¼�ɹ��߼�:
    [1]ʵ��AuthenticationSuccessHandler�ӿڵ�onAuthenticationSuccess����: (���ڸı�Ĭ�ϵĴ���ɹ��߼�)
        @Component
        public class MyAuthenticationSucessHandler implements AuthenticationSuccessHandler {
            @Autowired
            private ObjectMapper mapper;
            @Override
            public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, 
                    HttpServletResponse httpServletResponse,Authentication authentication) 
                    throws IOException, ServletException {
                httpServletResponse.setContentType("application/json;charset=utf-8");
                httpServletResponse.getWriter().write(mapper.writeValueAsString(authentication));
            }
        }
        (1)Authentication�����Ȱ�������֤�����һЩ��Ϣ,����IP,�����SessionId��,Ҳ�������û���Ϣ(User����);
        (2)�û���¼�ɹ���ҳ�潫��ӡ��Authentication�������Ϣ;
    [2]�޸�������BrowserSecurityConfig: (ʹ�Զ��崦��ɹ��߼���Ч)
        @Configuration
        public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
            @Autowired
            private MyAuthenticationSucessHandler authenticationSucessHandler;
            @Bean
            public PasswordEncoder passwordEncoder() {
                // �ü��ܶ������ͬ��������ܺ�����ɲ�ͬ�Ľ��
                return new BCryptPasswordEncoder();
            }
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http.formLogin()                             // ����¼
                    // http.httpBasic() // HTTP Basic
                    .loginPage("/authentication/require")   // ��¼��ת URL
                    .loginProcessingUrl("/login")           // �������¼ URL
                    .successHandler(authenticationSucessHandler) // �����¼�ɹ�
                    .and()
                    .authorizeRequests()                    // ��Ȩ����
                    // ��¼��תURL������֤
                    .antMatchers("/authentication/require","/login.html","/css/**").permitAll()
                    .anyRequest()                           // ��������
                    .authenticated()                        // ����Ҫ��֤
                    .and().csrf().disable();                // �ر�CSRF��������
            }
        }
    [3]������Ŀ,����http://localhost:8080/login.html,�����û����������¼�ɹ����ӡAuthentication�������Ϣ:
        {
            "authorities": [{
                "authority": "admin"
            }],
            "details": {
                "remoteAddress": "0:0:0:0:0:0:0:1",
                "sessionId": "E7194BB3DDFDD7EFBF1C6973059B9BA2"
            },
            "authenticated": true,
            "principal": {
                "password": null,
                "username": "user",
                "authorities": [{
                    "authority": "admin"
                }],
                "accountNonExpired": true,
                "accountNonLocked": true,
                "credentialsNonExpired": true,
                "enabled": true
            },
            "credentials": null,
            "name": "user"
        }
        (password,credentials��Щ������Ϣ,Spring Security�Ѿ���������)
    [4]�ڴ˻�����,����¼�ɹ���ҳ�����ת:
        (1)�޸�MyAuthenticationSucessHandler:
            @Component
            public class MyAuthenticationSucessHandler implements AuthenticationSuccessHandler {
                @Autowired
                private ObjectMapper mapper;
                private RequestCache requestCache = new HttpSessionRequestCache();
                private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
                @Override
                public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, 
                        HttpServletResponse httpServletResponse, Authentication authentication) 
                        throws IOException, ServletException {
                    SavedRequest savedRequest = requestCache.getRequest(httpServletRequest,httpServletResponse);
                    redirectStrategy.sendRedirect(httpServletRequest,httpServletResponse,savedRequest.getRedirectUrl());
                }
            }
        (2)�޸�������BrowserSecurityConfig:
            @Configuration
            public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
                @Autowired
                private MyAuthenticationSucessHandler authenticationSucessHandler;
                @Override
                protected void configure(HttpSecurity http) throws Exception {
                    http.formLogin()                                // ����¼
                        // http.httpBasic()                         // HTTP Basic
                        .loginPage("/login.html")                   // ��¼��ת URL
                        .loginProcessingUrl("/login")               // �������¼ URL
                        .successHandler(authenticationSucessHandler)// �����¼�ɹ�
                        .and()
                        .authorizeRequests()                        // ��Ȩ����
                        // ��¼��ת URL ������֤
                        .antMatchers( "/login.html","/css/**").permitAll() 
                        .anyRequest()                               // ��������
                        .authenticated()                            // ����Ҫ��֤
                        .and().csrf().disable();                    // �ر�CSRF��������
                }
                @Bean
                public PasswordEncoder passwordEncoder() {
                    // �ü��ܶ������ͬ��������ܺ�����ɲ�ͬ�Ľ��
                    return new BCryptPasswordEncoder();
                }
            }
    [5]��¼�ɹ���,ָ��ȥ���ʵ�·��:
        (1)�޸�MyAuthenticationSucessHandler:
            @Component
            public class MyAuthenticationSucessHandler implements AuthenticationSuccessHandler {
                private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
                @Override
                public void onAuthenticationSuccess(HttpServletRequest request, 
                        HttpServletResponse response,Authentication authentication) throws IOException {
                    redirectStrategy.sendRedirect(request, response, "/index");
                }
            }
        (2)�޸�TestController: (������·����е�һ��,Ч������ͬ)
            @GetMapping("index")
            public Object index(){
                return SecurityContextHolder.getContext().getAuthentication();
            }
            @GetMapping("index")
            public Object index(Authentication authentication) {
                return authentication;
            }
3.�Զ����¼ʧ���߼�: (���Զ����¼�ɹ������߼�����)
    [1]ʵ��AuthenticationFailureHandler��onAuthenticationFailure����:
        @Component
        public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, 
                    HttpServletResponse response,AuthenticationException exception) 
                    throws IOException {
            }
        }
        (1)onAuthenticationFailure������AuthenticationException������һ��������;
        (2)Spring Security���ݵ�¼ʧ�ܵ�ԭ���װ������Ӧ��ʵ���࣬�鿴AuthenticationException��Hierarchy:
            (��ͬ��ʧ��ԭ���Ӧ��ͬ���쳣)
            BadCredentialsException: �û������������;
            UsernameNotFoundException: �û�������;
            LockedException: �û�������;
    [2]��Ҫ�ڵ�¼ʧ�ܵ�ʱ�򷵻�ʧ����Ϣ:
        (1)�޸�MyAuthenticationFailureHandler:
            @Component
            public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
                @Autowired
                private ObjectMapper mapper;
                @Override
                public void onAuthenticationFailure(HttpServletRequest request, 
                    HttpServletResponse response,AuthenticationException exception) throws IOException {
                    //״̬�붨��Ϊ500,��ϵͳ�ڲ��쳣
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write(mapper.writeValueAsString(exception.getMessage()));
                }
            }
        (2)�޸�������BrowserSecurityConfig:
            @Configuration
            public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
                @Autowired
                private MyAuthenticationSucessHandler authenticationSucessHandler;
                @Autowired
                private MyAuthenticationFailureHandler authenticationFailureHandler;
                @Override
                protected void configure(HttpSecurity http) throws Exception {
                http.formLogin()                            // ����¼
                    // http.httpBasic()                     // HTTP Basic
                    .loginPage("/login.html")               // ��¼��ת URL
                    .loginProcessingUrl("/login")           // �������¼ URL
                    .successHandler(authenticationSucessHandler) // �����¼�ɹ�
                    .failureHandler(authenticationFailureHandler)// �����¼ʧ��
                    .and()
                    .authorizeRequests()                    // ��Ȩ����
                    // ��¼��תURL������֤
                    .antMatchers( "/login.html","/css/**").permitAll() 
                    .anyRequest()                           // ��������
                    .authenticated()                        // ����Ҫ��֤
                    .and().csrf().disable();                // �ر�CSRF��������
                }
                @Bean
                public PasswordEncoder passwordEncoder() {
                    // �ü��ܶ������ͬ��������ܺ�����ɲ�ͬ�Ľ��
                    return new BCryptPasswordEncoder();
                }
            }            
```
