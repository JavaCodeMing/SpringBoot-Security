
```
1.ʱ����һ�ַǳ���������վ��¼��ʽΪ�ֻ�������֤���¼,��Spring SecurityĬ��ֻ�ṩ���˺�����ĵ�¼��֤�߼�;
2.Ҫʵ���ֻ�������֤���¼��֤����,��Ҫģ��Spring Security�˺������¼�߼�������ʵ��һ���Լ�����֤�߼�;
3.�ڡ�Spring Security���ͼ����֤�롷�Ļ����������ɶ�����֤���¼�Ĺ���;
```

# ������֤������
```text
1.���������֤�����SmsCode:
    public class SmsCode {
        //�ֻ���֤��
        private String code;
        //����ʱ��
        private LocalDateTime expireTime;
        public SmsCode(String code, int expireIn) {
            this.code = code;
            this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
        }
        public SmsCode(String code, LocalDateTime expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
        //�����ж϶�����֤���Ƿ��ѹ���
        boolean isExpire() { 
            return LocalDateTime.now().isAfter(expireTime); 
        }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code;}
        public LocalDateTime getExpireTime() { return expireTime; }
        public void setExpireTime(LocalDateTime expireTime) {
            this.expireTime = expireTime;
        }
    }
2.�޸�ValidateCodeController: (�������ɶ�����֤����������Ӧ�ķ���)
    @RestController
    public class ValidateController {
        public final static String SESSION_KEY_SMS_CODE = "SESSION_KEY_SMS_CODE";
        private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
        @GetMapping("/code/sms")
        public void createSmsCode(HttpServletRequest request, HttpServletResponse response,
                String mobile) throws IOException {
            SmsCode smsCode = createSMSCode();
            //��������֤�뱣�浽��Session��,��Ӧ��keyΪSESSION_KEY_SMS_CODE
            sessionStrategy.setAttribute(new ServletWebRequest(request), 
                SESSION_KEY_SMS_CODE + mobile, smsCode);
            // �����֤�뵽����̨������ŷ��ͷ���
            System.out.println("���ĵ�¼��֤��Ϊ��" + smsCode.getCode() + "����Чʱ��Ϊ60��");
        }
        private SmsCode createSMSCode() {
            //������һ��6λ�Ĵ����������,��Чʱ��Ϊ60��
            String code = RandomStringUtils.randomNumeric(6);
            return new SmsCode(code, 60);
        }
    }
```
# �����¼ҳ
```text
1.�޸ĵ�¼ҳlogin.html: (�����������)
    <form class="login-page" action="/login/mobile" method="post">
        <div class="form">
            <h3>������֤���¼</h3>
            <input type="text" placeholder="�ֻ���" name="mobile" value="17777777777" required="required"/>
            <span style="display: inline">
                <input type="text" name="smsCode" placeholder="������֤��" style="width: 50%;"/>
                <a href="javascript:void(0);" onclick="sendSms()">������֤��</a>
            </span>
            <button type="submit">��¼</button>
        </div>
    </form>
    <script type="text/javascript">
        function sendSms(){
            var httpRequest = new XMLHttpRequest();
            httpRequest.open('GET', '/code/sms?mobile=17777777777', true);
            httpRequest.send();
        }
    </script>
2.�޸�������BrowserSecurityConfig: (����"/code/sms"����Ϊ����֤)
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // �����֤��У�������
        http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin()                                    // ����¼
            // http.httpBasic()                             // HTTP Basic
            .loginPage("/login.html")                       // ��¼��ת URL
            .loginProcessingUrl("/login")                   // �������¼ URL
            .successHandler(authenticationSucessHandler)      // �����¼�ɹ�
            .failureHandler(authenticationFailureHandler)     // �����¼ʧ��
            .and()
            .rememberMe()
            .tokenRepository(persistentTokenRepository())
            .tokenValiditySeconds(3600)
            .userDetailsService(userDetailService)
            .and()
            .authorizeRequests()                    // ��Ȩ����
            // ������֤������·��
            .antMatchers("/login.html","/css/**","/code/image","/code/sms").permitAll() 
            .anyRequest()                            // ��������
            .authenticated()                         // ����Ҫ��֤
            .and().csrf().disable()
            .apply(smsAuthenticationConfig); // ��������֤����֤���üӵ�Spring Security��
    }
```
# ��Ӷ�����֤����֤
```text
1.��Spring Security��,ʹ���û���������֤�Ĺ���:
    UsernamePasswordAuthenticationFilter -> AuthenticationManager -> DaoAuthenticationProvvider -> UserDetailService -> UserDetails -> Authentication
    [1]Spring Securityʹ��UsernamePasswordAuthenticationFilter�������������û���������֤����,���û����������װ��һ��UsernamePasswordToken���󽻸�AuthenticationManager����;
    [2]AuthenticationManager������һ��֧�ִ��������Token��AuthenticationProvider(����ΪDaoAuthenticationProvider,AuthenticationProvider������һ��ʵ����)��������֤;
    [3]��֤������DaoAuthenticationProvider������UserDetailService��loadUserByUsername������������֤;
    [4]�����֤ͨ��(��UsernamePasswordToken�е��û������������)�򷵻�һ��UserDetails���Ͷ���,������֤��Ϣ���浽Session��,��֤����ͨ��Authentication�����ȡ����֤����Ϣ;
2.����Spring Security���û���������֤ʵ�ֶ�����֤����֤������:
    SmsAuthenticationFilter -> AuthenticationManager -> SmsAuthenticationProvvider -> UserDetailService -> UserDetails -> Authentication
    [1]���̽���:
        (1)�Զ���һ����ΪSmsAuthenticationFitler�Ĺ����������ض�����֤���¼����,�����ֻ������װ��һ����SmsAuthenticationToken�Ķ�����;
        (2)��SmsAuthenticationToken����AuthenticationManager����;
        (3)����һ��֧�ִ���SmsAuthenticationToken�����SmsAuthenticationProvider,SmsAuthenticationProvider����UserDetailService��loadUserByUsername������������֤;
        (4)ͨ��SmsAuthenticationToken�е��ֻ���ȥ���ݿ��в�ѯ�Ƿ�����֮��Ӧ���û�;�����򽫸��û���Ϣ��װ��UserDetails�����з��ز�����֤�����Ϣ���浽Authentication����;
    [1]Ϊ��ʵ���������,��Ҫ����SmsAuthenticationFitler��SmsAuthenticationToken��SmsAuthenticationProvider��������Щ�齨���������ӵ�Spring Security��:
        (1)����SmsAuthenticationToken:
            (�鿴UsernamePasswordAuthenticationToken��Դ��,���临�Ƴ���������ΪSmsAuthenticationToken,�������޸�)
            public class SmsAuthenticationToken extends AbstractAuthenticationToken {
                private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
                //����֤֮ǰprincipal������ֻ���,��֤֮�������û���Ϣ
                private final Object principal;
                public SmsAuthenticationToken(String mobile) {
                    super(null);
                    this.principal = mobile;
                    setAuthenticated(false);
                }
                public SmsAuthenticationToken(Object principal,Collection<? extends GrantedAuthority> authorities) {
                    super(authorities);
                    this.principal = principal;
                    super.setAuthenticated(true);
                }
                @Override
                public Object getCredentials() { return null; }
                @Override
                public Object getPrincipal() { return this.principal; }
                @Override
                public void setAuthenticated(boolean isAuthenticated){
                    if(isAuthenticated){
                        throw new IllegalArgumentException(
                            "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
                    }
                    super.setAuthenticated(false);
                }
            }
            (UsernamePasswordAuthenticationTokenԭ��������һ��credentials�������ڴ������,���ﲻ��Ҫ��ȥ����)
        (2)����SmsAuthenticationFilter:
            (����UsernamePasswordAuthenticationFilterԴ�벢�����޸�,���ڴ��������֤���¼����)
            public class SmsAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
                public static final String MOBILE_KEY = "mobile";
                private String mobileParameter = MOBILE_KEY;
                private boolean postOnly = true;
                protected SmsAuthenticationFilter() {
                    //ָ��������Ϊ/login/mobile,���󷽷�ΪPOSTʱ�ù�������Ч
                    super(new AntPathRequestMatcher("/login/mobile","POST"));
                }
                @Override
                public Authentication attemptAuthentication(HttpServletRequest request, 
                        HttpServletResponse response)throws AuthenticationException {
                    if(postOnly && !"POST".equalsIgnoreCase(request.getMethod())){
                        throw new AuthenticationServiceException("Authentication method not supported: "+request.getMethod());
                    }
                    String mobile = obtainMobile(request);
                    if(mobile == null){ mobile = ""; }
                    mobile = mobile.trim();
                    SmsAuthenticationToken authRequest = new SmsAuthenticationToken(mobile);
                    setDetails(request,authRequest);
                    return this.getAuthenticationManager().authenticate(authRequest);
                }
                private void setDetails(HttpServletRequest request, SmsAuthenticationToken authRequest) {
                    authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
                }
                private String obtainMobile(HttpServletRequest request) {
                    return request.getParameter(mobileParameter);
                }
                public void setPostOnly(boolean postOnly) { this.postOnly = postOnly; }
                public final String getMobileParameter() { return mobileParameter; }
            }
        (3)����SmsAuthenticationProvider: (���������Token����)
            public class SmsAuthenticationProvider implements AuthenticationProvider {
                private UserDetailService userDetailService;
                //���ڱ�д����������֤�߼�
                @Override
                public Authentication authenticate(Authentication authentication) 
                        throws AuthenticationException {
                    SmsAuthenticationToken authenticationToken = (SmsAuthenticationToken) authentication;
                    //��SmsAuthenticationToken��ȡ�����ֻ�����Ϣ,��ͨ���ֻ���ȥ���ݿ��в�ѯ�û���Ϣ
                    UserDetails userDetails = 
                        userDetailService.loadUserByUsername((String) authenticationToken.getPrincipal());
                    //������ڸ��û�����֤ͨ��,����һ����֤ͨ����Token,�������û���Ϣ���û�Ȩ��
                    if (userDetails == null) {
                        throw new InternalAuthenticationServiceException("δ�ҵ�����ֻ��Ŷ�Ӧ���û�");
                    }
                    SmsAuthenticationToken authenticationResult = 
                        new SmsAuthenticationToken(userDetails, userDetails.getAuthorities());
                    authenticationResult.setDetails(authenticationToken.getDetails());
                    return authenticationResult;
                }
                //ָ��֧�ִ����Token����ΪSmsAuthenticationToken
                @Override
                public boolean supports(Class<?> aClass) {
                    return SmsAuthenticationToken.class.isAssignableFrom(aClass);
                }
                public UserDetailService getUserDetailService() {
                    return userDetailService;
                }
                public void setUserDetailService(UserDetailService userDetailService) {
                    this.userDetailService = userDetailService;
                }
            }            
        (4)����SmsCodeFilter: (������֤���У���߼�,�����߼�������֤�û�����Ȩ��)
            @Component
            public class SmsCodeFilter extends OncePerRequestFilter {
                @Autowired
                private AuthenticationFailureHandler authenticationFailureHandler;
                private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
                @Override
                protected void doFilterInternal(HttpServletRequest httpServletRequest, 
                        HttpServletResponse httpServletResponse, FilterChain filterChain) 
                        throws ServletException, IOException {
                    if (StringUtils.equalsIgnoreCase("/login/mobile", httpServletRequest.getRequestURI()) 
                        && StringUtils.equalsIgnoreCase(httpServletRequest.getMethod(), "post")) {
                        try {
                            validateCode(new ServletWebRequest(httpServletRequest));
                        } catch (ValidateCodeException e) {
                            authenticationFailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, e);
                            return;
                        }
                    }
                    filterChain.doFilter(httpServletRequest, httpServletResponse);
                }
                private void validateCode(ServletWebRequest servletWebRequest) throws ServletRequestBindingException {
                    String smsCodeInRequest = ServletRequestUtils.getStringParameter(servletWebRequest.getRequest(), "smsCode");
                    String mobile = ServletRequestUtils.getStringParameter(servletWebRequest.getRequest(), "mobile");
                    SmsCode codeInSession = 
                    (SmsCode)sessionStrategy.getAttribute(servletWebRequest, ValidateController.SESSION_KEY_SMS_CODE + mobile);
                    if (codeInSession == null) {
                        throw new ValidateCodeException("��֤�벻���ڣ������·��ͣ�");
                    }
                    if (StringUtils.isBlank(smsCodeInRequest)) {
                        throw new ValidateCodeException("��֤�벻��Ϊ�գ�");
                    }
                    if (!StringUtils.equalsIgnoreCase(codeInSession.getCode(), smsCodeInRequest)) {
                        throw new ValidateCodeException("��֤�벻��ȷ��");
                    }
                    sessionStrategy.removeAttribute(servletWebRequest, ValidateController.SESSION_KEY_SMS_CODE + mobile);
                }
            }
        (5)����һ��������SmsAuthenticationConfig: (ʹ�����ֻ��û�Ȩ��������Ч)
            @Component
            public class SmsAuthenticationConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
                @Autowired
                private AuthenticationSuccessHandler authenticationSuccessHandler;
                @Autowired
                private AuthenticationFailureHandler authenticationFailureHandler;
                @Autowired
                private UserDetailService userDetailService;
                @Override
                public void configure(HttpSecurity httpSecurity) throws Exception {
                    SmsAuthenticationFilter smsAuthenticationFilter = new SmsAuthenticationFilter();
                    smsAuthenticationFilter.setAuthenticationManager(httpSecurity.getSharedObject(AuthenticationManager.class));
                    smsAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
                    smsAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
                    SmsAuthenticationProvider smsAuthenticationProvider = new SmsAuthenticationProvider();
                    smsAuthenticationProvider.setUserDetailService(userDetailService);
                    httpSecurity.authenticationProvider(smsAuthenticationProvider)
                        .addFilterAfter(smsAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                }
            }
            1)����SmsAuthenticationFilter,�ֱ�������AuthenticationManager��AuthenticationSuccessHandler��AuthenticationFailureHandler����;
            2)����SmsAuthenticationProvider,ֻ��Ҫ���Զ����UserDetailServiceע���������;
            3)����HttpSecurity��authenticationProvider����ָ����AuthenticationProviderΪSmsAuthenticationProvider,
                ����SmsAuthenticationFilter��������ӵ���UsernamePasswordAuthenticationFilter����;
        (6)�޸�������BrowserSecurityConfig: (���ö�����֤��У�������)
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
                    // �����֤��У�������
                http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                    // ��Ӷ�����֤��У�������
                    .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                    .formLogin()                            // ����¼
                    // http.httpBasic()                     // HTTP Basic
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
                    .antMatchers("/login.html","/css/**","/code/image","/code/sms").permitAll() 
                    .anyRequest()    // ��������
                    .authenticated() // ����Ҫ��֤
                    .and().csrf().disable()
                    .apply(smsAuthenticationConfig); // ��������֤����֤���üӵ�Spring Security��
            }
```
