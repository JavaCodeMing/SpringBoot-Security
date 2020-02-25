
```
1.时下另一种非常常见的网站登录方式为手机短信验证码登录,但Spring Security默认只提供了账号密码的登录认证逻辑;
2.要实现手机短信验证码登录认证功能,需要模仿Spring Security账号密码登录逻辑代码来实现一套自己的认证逻辑;
3.在《Spring Security添加图形验证码》的基础上来集成短信验证码登录的功能;
```

# 短信验证码生成
```text
1.定义短信验证码对象SmsCode:
    public class SmsCode {
        //手机验证码
        private String code;
        //过期时间
        private LocalDateTime expireTime;
        public SmsCode(String code, int expireIn) {
            this.code = code;
            this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
        }
        public SmsCode(String code, LocalDateTime expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
        //用于判断短信验证码是否已过期
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
2.修改ValidateCodeController: (加入生成短信验证码相关请求对应的方法)
    @RestController
    public class ValidateController {
        public final static String SESSION_KEY_SMS_CODE = "SESSION_KEY_SMS_CODE";
        private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
        @GetMapping("/code/sms")
        public void createSmsCode(HttpServletRequest request, HttpServletResponse response,
                String mobile) throws IOException {
            SmsCode smsCode = createSMSCode();
            //将短信验证码保存到了Session中,对应的key为SESSION_KEY_SMS_CODE
            sessionStrategy.setAttribute(new ServletWebRequest(request), 
                SESSION_KEY_SMS_CODE + mobile, smsCode);
            // 输出验证码到控制台代替短信发送服务
            System.out.println("您的登录验证码为：" + smsCode.getCode() + "，有效时间为60秒");
        }
        private SmsCode createSMSCode() {
            //生成了一个6位的纯数字随机数,有效时间为60秒
            String code = RandomStringUtils.randomNumeric(6);
            return new SmsCode(code, 60);
        }
    }
```
# 改造登录页
```text
1.修改登录页login.html: (添加如下内容)
    <form class="login-page" action="/login/mobile" method="post">
        <div class="form">
            <h3>短信验证码登录</h3>
            <input type="text" placeholder="手机号" name="mobile" value="17777777777" required="required"/>
            <span style="display: inline">
                <input type="text" name="smsCode" placeholder="短信验证码" style="width: 50%;"/>
                <a href="javascript:void(0);" onclick="sendSms()">发送验证码</a>
            </span>
            <button type="submit">登录</button>
        </div>
    </form>
    <script type="text/javascript">
        function sendSms(){
            var httpRequest = new XMLHttpRequest();
            httpRequest.open('GET', '/code/sms?mobile=17777777777', true);
            httpRequest.send();
        }
    </script>
2.修改配置类BrowserSecurityConfig: (设置"/code/sms"请求为免验证)
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 添加验证码校验过滤器
        http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin()                                    // 表单登录
            // http.httpBasic()                             // HTTP Basic
            .loginPage("/login.html")                       // 登录跳转 URL
            .loginProcessingUrl("/login")                   // 处理表单登录 URL
            .successHandler(authenticationSucessHandler)      // 处理登录成功
            .failureHandler(authenticationFailureHandler)     // 处理登录失败
            .and()
            .rememberMe()
            .tokenRepository(persistentTokenRepository())
            .tokenValiditySeconds(3600)
            .userDetailsService(userDetailService)
            .and()
            .authorizeRequests()                    // 授权配置
            // 无需认证的请求路径
            .antMatchers("/login.html","/css/**","/code/image","/code/sms").permitAll() 
            .anyRequest()                            // 所有请求
            .authenticated()                         // 都需要认证
            .and().csrf().disable()
            .apply(smsAuthenticationConfig); // 将短信验证码认证配置加到Spring Security中
    }
```
# 添加短信验证码认证
```text
1.在Spring Security中,使用用户名密码认证的过程:
    UsernamePasswordAuthenticationFilter -> AuthenticationManager -> DaoAuthenticationProvvider -> UserDetailService -> UserDetails -> Authentication
    [1]Spring Security使用UsernamePasswordAuthenticationFilter过滤器来拦截用户名密码认证请求,将用户名和密码封装成一个UsernamePasswordToken对象交给AuthenticationManager处理;
    [2]AuthenticationManager将挑出一个支持处理该类型Token的AuthenticationProvider(这里为DaoAuthenticationProvider,AuthenticationProvider的其中一个实现类)来进行认证;
    [3]认证过程中DaoAuthenticationProvider将调用UserDetailService的loadUserByUsername方法来处理认证;
    [4]如果认证通过(即UsernamePasswordToken中的用户名和密码相符)则返回一个UserDetails类型对象,并将认证信息保存到Session中,认证后便可通过Authentication对象获取到认证的信息;
2.仿照Spring Security的用户名密码认证实现短信验证码认证的流程:
    SmsAuthenticationFilter -> AuthenticationManager -> SmsAuthenticationProvvider -> UserDetailService -> UserDetails -> Authentication
    [1]流程介绍:
        (1)自定义一个名为SmsAuthenticationFitler的过滤器来拦截短信验证码登录请求,并将手机号码封装到一个叫SmsAuthenticationToken的对象中;
        (2)将SmsAuthenticationToken交由AuthenticationManager处理;
        (3)定义一个支持处理SmsAuthenticationToken对象的SmsAuthenticationProvider,SmsAuthenticationProvider调用UserDetailService的loadUserByUsername方法来处理认证;
        (4)通过SmsAuthenticationToken中的手机号去数据库中查询是否有与之对应的用户;若有则将该用户信息封装到UserDetails对象中返回并将认证后的信息保存到Authentication对象;
    [1]为了实现这个流程,需要定义SmsAuthenticationFitler、SmsAuthenticationToken和SmsAuthenticationProvider，并将这些组建组合起来添加到Spring Security中:
        (1)定义SmsAuthenticationToken:
            (查看UsernamePasswordAuthenticationToken的源码,将其复制出来重命名为SmsAuthenticationToken,并稍作修改)
            public class SmsAuthenticationToken extends AbstractAuthenticationToken {
                private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
                //在认证之前principal存的是手机号,认证之后存的是用户信息
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
            (UsernamePasswordAuthenticationToken原来还包含一个credentials属性用于存放密码,这里不需要就去掉了)
        (2)定义SmsAuthenticationFilter:
            (复制UsernamePasswordAuthenticationFilter源码并稍作修改,用于处理短信验证码登录请求)
            public class SmsAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
                public static final String MOBILE_KEY = "mobile";
                private String mobileParameter = MOBILE_KEY;
                private boolean postOnly = true;
                protected SmsAuthenticationFilter() {
                    //指定当请求为/login/mobile,请求方法为POST时该过滤器生效
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
        (3)定义SmsAuthenticationProvider: (处理该类型Token的类)
            public class SmsAuthenticationProvider implements AuthenticationProvider {
                private UserDetailService userDetailService;
                //用于编写具体的身份认证逻辑
                @Override
                public Authentication authenticate(Authentication authentication) 
                        throws AuthenticationException {
                    SmsAuthenticationToken authenticationToken = (SmsAuthenticationToken) authentication;
                    //从SmsAuthenticationToken中取出了手机号信息,再通过手机号去数据库中查询用户信息
                    UserDetails userDetails = 
                        userDetailService.loadUserByUsername((String) authenticationToken.getPrincipal());
                    //如果存在该用户则认证通过,构造一个认证通过的Token,包含了用户信息和用户权限
                    if (userDetails == null) {
                        throw new InternalAuthenticationServiceException("未找到与该手机号对应的用户");
                    }
                    SmsAuthenticationToken authenticationResult = 
                        new SmsAuthenticationToken(userDetails, userDetails.getAuthorities());
                    authenticationResult.setDetails(authenticationToken.getDetails());
                    return authenticationResult;
                }
                //指定支持处理的Token类型为SmsAuthenticationToken
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
        (4)定义SmsCodeFilter: (短信验证码的校验逻辑,以上逻辑都是验证用户及其权限)
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
                        throw new ValidateCodeException("验证码不存在，请重新发送！");
                    }
                    if (StringUtils.isBlank(smsCodeInRequest)) {
                        throw new ValidateCodeException("验证码不能为空！");
                    }
                    if (!StringUtils.equalsIgnoreCase(codeInSession.getCode(), smsCodeInRequest)) {
                        throw new ValidateCodeException("验证码不正确！");
                    }
                    sessionStrategy.removeAttribute(servletWebRequest, ValidateController.SESSION_KEY_SMS_CODE + mobile);
                }
            }
        (5)创建一个配置类SmsAuthenticationConfig: (使以上手机用户权限配置生效)
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
            1)配置SmsAuthenticationFilter,分别设置了AuthenticationManager、AuthenticationSuccessHandler和AuthenticationFailureHandler属性;
            2)配置SmsAuthenticationProvider,只需要将自定义的UserDetailService注入进来即可;
            3)调用HttpSecurity的authenticationProvider方法指定了AuthenticationProvider为SmsAuthenticationProvider,
                并将SmsAuthenticationFilter过滤器添加到了UsernamePasswordAuthenticationFilter后面;
        (6)修改配置类BrowserSecurityConfig: (配置短信验证码校验过滤器)
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
                    // 添加验证码校验过滤器
                http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                    // 添加短信验证码校验过滤器
                    .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class) 
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
                    .antMatchers("/login.html","/css/**","/code/image","/code/sms").permitAll() 
                    .anyRequest()    // 所有请求
                    .authenticated() // 都需要认证
                    .and().csrf().disable()
                    .apply(smsAuthenticationConfig); // 将短信验证码认证配置加到Spring Security中
            }
```
