
# 安全注解
```text
1.Spring Security提供了三种不同的安全注解:
    [1]Spring Security自带的@Secured注解;
    [2]JSR-250的@RolesAllowed注解;
    [3]表达式驱动的注解,包括@PreAuthorize、@PostAuthorize、@PreFilter和 @PostFilter;
2.@Secured注解的使用:
    [1]在Spring-Security.xml中启用@Secured注解:
        <global-method-security secured-annotations="enabled"/>
    [2]案例: (只有拥有权限"ADMIN"的用户才能访问)
        @Secured("ROLE_ADMIN")
        public void test(){
            ...
        }
        (权限不足时,方法抛出Access Denied异常)
    [3]@Secured注解中使用一个String数组作为参数:(至少需要具备其中的一个权限才可访问)
        @Secured({"ROLE_ADMIN","ROLE_USER"})
        public void test(){
            ...
        }
3.@RolesAllowed注解的使用:
    [1]在Spring-Security.xml中启用@RolesAllowed注解:
        <global-method-security jsr250-annotations="enabled"/>
    [2]案例: (只有拥有权限“ROLE_ADMIN”的用户才能访问)
        @RolesAllowed("ROLE_ADMIN")
        public void test(){
            ...
        }
4.SpEL注解:
    [1]在Spring-Security.xml中启用SpEL注解:
        <global-method-security pre-post-annotations="enabled"/>
    [2]Spring Security支持的所有SpEL表达式:
        (1)authentication: 用户认证对象;
        (2)denyAll: 结果始终为false;
        (3)hasAnyRole(list of roles): 如果用户被授权指定的任意权限,结果为true;
        (4)hasRole(role): 如果用户被授予了指定的权限,结果 为true;
        (5)hasIpAddress(IP Adress): 用户地址;
        (6)isAnonymous(): 是否为匿名用户;
        (7)isAuthenticated(): 不是匿名用户;
        (8)isFullyAuthenticated: 不是匿名也不是remember-me认证;
        (9)isRemberMe(): remember-me认证;
        (10)permitAll: 始终为true;
        (11)principal: 用户主要信息对象;
    [3]@PreAuthorize注解: (用于方法前验证权限)
        @PreAuthorize("hasRole('ROLE_ADMIN') and #form.note.length() <= 1000 or hasRole('ROLE_VIP')")
        public void writeBlog(Form form){
            ...
        }
        (1)需要有"ROLE_ADMIN"权限且表单中note字段字数不得超过1000字,或者有"ROLE_VIP"权限;
        (2)Spring Security能够检查传入方法的参数,表达式中的#form部分就直接引用了方法中的同名参数;
    [4]@PostAuthorize注解: (用于方法后验证权限)
        @PostAuthorize("returnObject.user.userName == principal.username")
        public User getUserById(long id){
            ...        
        }
        (1)Spring Security在SpEL中提供了名为returnObject的变量,可以获取返回对象user;
        (2)判断user属性username是否和访问该方法的用户对象的用户名一样,不一样则抛出异常;
    [5]@PreFilter注解: (在进入方法前过滤输入值)
        @PreFilter("filterObject.username == 'testuser2'")
        public User getUserById(long id){
            ...        
        }
        (1)Spring Security在SpEL中提供了名为filterObject的变量,指的是返回集合中的当前对象;
    [6]@PostFilter注解: (在方法执行后过滤结果)
        @PostFilter("filterObject.sex == '男' ")
        public List<User> getUserList(){
            ...
        }
```
# 权限控制
```text
1.在SpringBoot中开启Spring Security的安全注解:
    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
        ...
    }
2.修改UserDetailService: (给与特定用户"admin"的权限,其他用户"test"权限)
    @Configuration
    public class UserDetailService implements UserDetailsService {
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Override
        public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
            // 模拟一个用户,替代数据库获取逻辑
            MyUser user = new MyUser();
            user.setUserName(s);
            user.setPassword(passwordEncoder.encode("123456"));
            // 输出加密后的密码
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
3.在Controller中添加一个方法: (用于验证权限登录)
    @GetMapping("/auth/admin")
    @PreAuthorize("hasAuthority('admin')")
    public String authenticationTest() {
        return "您拥有admin权限，可以查看";
    }
    [1]启动系统,使用"kimi"账号登录,登录成功后再访问"/auth/admin",可正常访问;
    [2]退出登录,使用其他账号登录,登录成功后再访问"/auth/admin",响应错误页面并返回403错误码;
4.自定义权限不足处理器: (用来对权限不足时进行处理操作)
    [1]新增一个处理器MyAuthenticationAccessDeniedHandler:
        @Component
        public class MyAuthenticationAccessDeniedHandler implements AccessDeniedHandler {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                    AccessDeniedException accessDeniedException) throws IOException {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("很抱歉，您没有该访问权限");
            }
        }
    [2]修改配置类BrowserSecurityConfig:
        @Autowired
        private MyAuthenticationAccessDeniedHandler authenticationAccessDeniedHandler;
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.exceptionHandling()
                    .accessDeniedHandler(authenticationAccessDeniedHandler)
                    .and()
                        // 添加验证码校验过滤器
                        .addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                        // 添加短信验证码校验过滤器
                        .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class) 
                    .formLogin()                                        // 表单登录
                        .loginPage("/login.html")                       // 登录跳转 URL
                        .loginProcessingUrl("/login")                   // 处理表单登录 URL
                        .successHandler(authenticationSucessHandler)    // 处理登录成功
                        .failureHandler(authenticationFailureHandler)   // 处理登录失败
                    .and()
                        .rememberMe()
                        .tokenRepository(persistentTokenRepository())
                        .tokenValiditySeconds(3600)
                        .userDetailsService(userDetailService)
                    .and()
                        .authorizeRequests()                            // 授权配置
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
