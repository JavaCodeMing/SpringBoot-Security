
```text
1.在《Spring Security短信验证码登录》的基础上进行Session管理;
2.Session超时设置:
    [1]Session超时时间也就是用户登录的有效时间,只需在配置文件中添加如下配置:
        server:
          servlet:
            session:
              timeout: 30m
    [2]修改配置文件BrowserSecurityConfig:
        (配置Session管理器,并配置Session失效后要跳转的URL)
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
                    .antMatchers("/login.html","/css/**","/code/image","/code/sms","/session/invalid").permitAll()
                    .anyRequest()    // 所有请求
                    .authenticated() // 都需要认证
                .and()
                    .sessionManagement()                    // 添加 Session管理器
                    .invalidSessionUrl("/session/invalid")  // Session失效后跳转到这个链接
                .and().csrf().disable()
                .apply(smsAuthenticationConfig); // 将短信验证码认证配置加到Spring Security中
        }
    [3]在Controller里添加一个方法,映射该请求:
        @GetMapping("/session/invalid")
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        public String sessionInvalid(){
            return "session已失效，请重新认证";
        }
    [4]将Session的超时时间设置为1m(即1分钟),重启项目,认证后等待60秒并刷新页面,请求跳转到自定义的URL:/session/invalid;
3.Session并发控制:
    (Session并发控制可以控制一个账号同一时刻最多能登录多少个)
    [1]自定义过期策略MySessionExpiredStrategy:
        @Component
        public class MySessionExpiredStrategy implements SessionInformationExpiredStrategy {
            @Override
            public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
                HttpServletResponse response = event.getResponse();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json;charset=utf-8");
                response.getWriter()
                    .write("您的账号已经在别的地方登录，当前登录已失效。如果密码遭到泄露，请立即修改密码！");
            }
        }        
    [2]修改配置类BrowserSecurityConfig:
        @Autowired
        private MySessionExpiredStrategy sessionExpiredStrategy;
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
                    .antMatchers("/login.html","/css/**","/code/image","/code/sms","/session/invalid").permitAll()
                    .anyRequest()    // 所有请求
                    .authenticated() // 都需要认证
                .and()
                    .sessionManagement()                    // 添加 Session管理器
                    .invalidSessionUrl("/session/invalid")  // Session失效后跳转到这个链接
                    .maximumSessions(1)
                    .expiredSessionStrategy(sessionExpiredStrategy)
                .and()
                .and().csrf().disable()
                .apply(smsAuthenticationConfig); // 将短信验证码认证配置加到Spring Security中
        }
        (1)maximumSessions(1): 配置最大Session并发数量为1个;(后登录的会挤掉前面登录的)
        (2)expiredSessionStrategy配置了Session在并发下失效后的处理策略;
    [3]先将Session超时时间设置久一点(如30m),然后重启项目,分别在两个浏览器中登录,前登录的页面刷新,提示账号已登录;
    [4]当Session达到最大有效数时,不再允许相同的账户登录的设置:
        @Autowired
        private MySessionExpiredStrategy sessionExpiredStrategy;
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
                    .antMatchers("/login.html","/css/**","/code/image","/code/sms","/session/invalid").permitAll()
                    .anyRequest()    // 所有请求
                    .authenticated() // 都需要认证
                .and()
                    .sessionManagement()                    // 添加 Session管理器
                    .invalidSessionUrl("/session/invalid")  // Session失效后跳转到这个链接
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(true)    
                    .expiredSessionStrategy(sessionExpiredStrategy)
                .and()
                .and().csrf().disable()
                .apply(smsAuthenticationConfig); // 将短信验证码认证配置加到Spring Security中
        }
        (实际开发中发现Session并发控制只对Spring Security默认的登录方式(账号密码登录)有效,而像短信验证码或社交账号登录并不生效)
4.Session集群处理:
    [1]Session集群处理的意义和方案:
        (1)当登录成功后,用户认证的信息存储在Session中,而这些Session默认是存储在运行运用的服务器上的,比如Tomcat,netty等;
        (2)当应用集群部署时,用户在A应用上登录认证了,后续通过负载均衡可能会把请求发送到B应用,而B应用服务器上并没有与该请求匹配的认证Session信息,所以用户就需要重新进行认证;
        (3)把Session信息存储在第三方容器里(如Redis集群),而不是各自的服务器,这样应用集群就可以通过第三方容器来共享Session了;
    [2]引入Redis和Spring Session依赖:
        <!-- Spring session 对接Redis的必要依赖 -->
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-data-redis</artifactId>
        </dependency>
        <!-- spring boot 整合Redis核心依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
    [3]在yml中配置Session存储方式为Redis及Redis的配置:
        spring:  
          session:
            store-type: redis
            redis:
              # 更新策略,ON_SAVE在调用#SessionRepository#save(Session)时,在response commit前刷新缓存(默认)
              # IMMEDIATE只要有任何更新就会刷新缓存
              flush-mode: on_save
              # 存储session的密钥的命名空间(默认spring:session)
              namespace: spring:session
          redis:
            database: 1
            host: localhost
            port: 6379
    [4]使用图形验证码登录,出现了问题: BufferedImage对象不能序列化到Redis
        (1)将原来的ImageCode类改造成ImageCode和ValidateCode:
            public class ValidateCode implements Serializable {
                //code验证码
                private String code;
                //expireTime过期时间
                private LocalDateTime expireTime;
                public ValidateCode(String code, int expireIn) {
                    this.code = code;
                    this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
                }
                public ValidateCode(String code, LocalDateTime expireTime) {
                    this.code = code;
                    this.expireTime = expireTime;
                }
                //isExpire方法用于判断验证码是否已过期
                boolean isExpire() {
                    return LocalDateTime.now().isAfter(expireTime);
                }
                public String getCode() { return code; }
                public void setCode(String code) { this.code = code; }
                public LocalDateTime getExpireTime() { return expireTime; }
                public void setExpireTime(LocalDateTime expireTime) {
                    this.expireTime = expireTime;
                }
            }
            public class ImageCode extends ValidateCode {
                //image图片
                private BufferedImage image;
                public ImageCode(BufferedImage image, String code, int expireIn) {
                    super(code, expireIn);
                    this.image = image;
                }
                public ImageCode(BufferedImage image, String code, LocalDateTime expireTime) {
                    super(code,expireTime);
                    this.image = image;
                }
                public BufferedImage getImage() { return image; }
                public void setImage(BufferedImage image) { this.image = image; }
            }
        (2)改造Controller中生成图形验证码的方法:
            @GetMapping("/code/image")
            public void createCode(HttpServletRequest request, HttpServletResponse response)
                    throws IOException {
                ImageCode imageCode = createImageCode();
                BufferedImage image = imageCode.getImage();
                imageCode.setImage(null);
                //将短信验证码保存到了Session中,对应的key为SESSION_KEY_SMS_CODE
                sessionStrategy.setAttribute(new ServletWebRequest(request),
                    SESSION_KEY_IMAGE_COE, imageCode);
                ImageIO.write(image, "jpeg", response.getOutputStream());
            }
    [5]测试:
        (1)开启Redis,并且启动两个应用实例,一个端口为8080,另一个端口为8090;
        (2)在8080端口应用上登录,将登录成功后的地址的端口改成8090访问,结果任然可以访问;
        (3)这就实现了集群化Session管理;
5.其他操作: SessionRegistry包含了一些使用的操作Session的方法
    [1]踢出用户(让Session失效):
        String currentSessionId = request.getRequestedSessionId();
        sessionRegistry.getSessionInformation(sessionId).expireNow();
    [2]获取所有Session信息:
        List<Object> principals = sessionRegistry.getAllPrincipals();
```
