
```text
Spring Security支持我们自定义认证的过程,如处理用户信息获取逻辑,使用我们自定义的
登录页面替换Spring Security默认的登录页及自定义登录成功或失败后的处理逻辑等;
```
# 自定义认证过程
```text
1.自定义认证的过程需要实现Spring Security提供的UserDetailService接口:
    public interface UserDetailsService {
        // 唯一的抽象方法: loadUserByUsername
        UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    }
    [1]loadUserByUsername方法返回一个UserDetail对象:
        // 该接口包含一些用于描述用户信息的方法
        public interface UserDetails extends Serializable {
            Collection<? extends GrantedAuthority> getAuthorities();
            String getPassword();
            String getUsername();
            boolean isAccountNonExpired();
            boolean isAccountNonLocked();
            boolean isCredentialsNonExpired();
            boolean isEnabled();
        }
        (1)getAuthorities(): 获取用户包含的权限,返回权限集合,权限是一个继承了GrantedAuthority的对象;
        (2)getPassword(): 用于获取密码;
        (3)getUsername(): 用于获取用户名;
        (4)isAccountNonExpired(): 返回boolean类型,用于判断账户是否未过期,未过期返回true反之返回false;
        (5)isAccountNonLocked(): 用于判断账户是否未锁定;
        (6)isCredentialsNonExpired(): 用于判断用户凭证是否没过期,即密码是否未过期;
        (7)isEnabled(): 用于判断用户是否可用;
    [2]实际使用中,可使用自定义实现类,也可使用Spring Security提供的UserDetails接口实现类User;
2.创建实体类: (用于存放模拟的用户数据)
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
3.在配置类中配置密码加密对象:
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 该加密对象对相同的密码加密后可生成不同的结果
        return new BCryptPasswordEncoder();
    }
    (PasswordEncoder是一个密码加密接口,而BCryptPasswordEncoder是Spring Security提供的一个实现方法)
4.创建MyUserDetailService实现UserDetailService:
    @Configuration
    public class UserDetailService implements UserDetailsService {
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Override
        public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
            // 模拟一个用户,替代数据库获取逻辑
            MyUser user = new MyUser();
            user.setUserName(s);
            user.setPassword(this.passwordEncoder.encode("123456"));
            // 输出加密后的密码
            System.out.println(user.getPassword());
            return new User(s, user.getPassword(), user.isEnabled(),
                user.isAccountNonExpired(), user.isCredentialsNonExpired(),
                user.isAccountNonLocked(), AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));
        }
    }
    [1]Spring Security提供的UserDetails接口的实现类User:
        User(String username, String password,Collection<? extends GrantedAuthority> authorities)
    [2]AuthorityUtils.commaSeparatedStringToAuthorityList("admin"):
        模拟一个admin的权限,该方法可以将逗号分隔的字符串转换为权限集合;
    [3]该类的逻辑: 模拟一个用户名随意,密码为123456,且具有admin权限的用户登录;
```
# 替换默认登录页
```text
1.创建自定义登录页login.html:(在src/main/resources/resources目录下)
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <title>登录</title>
        <link rel="stylesheet" href="./css/login.css" type="text/css">
    </head>
    <body>
    <form class="login-page" action="/login" method="post">
        <div class="form">
            <h3>账户登录</h3>
            <input type="text" placeholder="用户名" name="username" required="required"/>
            <input type="password" placeholder="密码" name="password" required="required"/>
            <button type="submit">登录</button>
        </div>
    </form>
    </body>
    </html>
    (记得拷贝css文件)
2.修改配置类: (让Spring Security跳转到自定义的登录页面)
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()                        // 表单方式
            // http.httpBasic()             // HTTP Basic
            .loginPage("/login.html")       // 登录页
            .loginProcessingUrl("/login")   // 登录请求
            .and()
            .authorizeRequests()            // 授权配置
            .antMatchers("/login.html","/css/**").permitAll()
            .anyRequest()                   // 所有请求
            .authenticated()                // 都需要认证
            .and().csrf().disable();        // 关闭CSRF攻击防御
    }
    [1].loginPage("/login.html"): 指定了跳转到登录页面的请求URL;
    [2].loginProcessingUrl("/login"): 对应登录页面form表单的action="/login"
    [3].antMatchers("/login.html","/css/**").permitAll():
        (1)表示跳转到登录页面的请求不被拦截,否则会进入无限循环;
        (2)表示访问css目录下的静态文件不被拦截,否则页面无法正常渲染;
    [4].csrf().disable(): 表示关闭CSRF攻击防御;
3.处理新需求: (在未登录的情况下,当用户访问html资源时跳转到登录页,否则返回JSON格式数据,状态码为401)
    [1]修改配置类:
        @Override
        protected void configure(HttpSecurity http) throws Exception {    
            http.formLogin()                            // 表单登录
                // http.httpBasic() // HTTP Basic
                .loginPage("/authentication/require")   // 登录跳转 URL
                .loginProcessingUrl("/login")           // 处理表单登录 URL
                .and()
                .authorizeRequests()                    // 授权配置
                // 登录跳转 URL 无需认证
                .antMatchers("/authentication/require", "/login.html","/css/**").permitAll()
                .anyRequest()                           // 所有请求
                .authenticated()                        // 都需要认证
                .and().csrf().disable();                // 关闭CSRF攻击防御
        }
    [2]定义控制器BrowserSecurityController: (用来处理访问非HTML的请求)
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
                return "访问的资源需要身份认证！";
            }
        }
    [3]以上处理的作用:
        (1)将登录页的请求变更为"/authentication/require",则非".html"结尾的请求都会被拦截到登录页请求;
        (2)设置登录页请求为无需认证,则非".html"结尾的请求被拦截到登录页请求后,可通过对应的控制器方法处理;
        (3)BrowserSecurityController: 
            1)HttpSessionRequestCache为Spring Security提供的用于缓存请求的对象;
            2)通过调用HttpSessionRequestCache的getRequest方法可以获取到本次请求的HTTP信息;
            3)DefaultRedirectStrategy的sendRedirect为Spring Security提供的用于处理重定向的方法
```
# 处理成功和失败
```text
1.Spring Security有一套默认的处理登录成功和失败的方法:
    [1]当用户登录成功时,页面会跳转到引发登录的请求,比如在未登录的情况下访问http://localhost:8080/hello,
        页面会跳转到登录页,登录成功后再跳转回来;
    [2]登录失败时则是跳转到Spring Security默认的错误提示页面;
2.自定义登录成功逻辑:
    [1]实现AuthenticationSuccessHandler接口的onAuthenticationSuccess方法: (用于改变默认的处理成功逻辑)
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
        (1)Authentication参数既包含了认证请求的一些信息,比如IP,请求的SessionId等,也包含了用户信息(User对象);
        (2)用户登录成功后页面将打印出Authentication对象的信息;
    [2]修改配置类BrowserSecurityConfig: (使自定义处理成功逻辑生效)
        @Configuration
        public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
            @Autowired
            private MyAuthenticationSucessHandler authenticationSucessHandler;
            @Bean
            public PasswordEncoder passwordEncoder() {
                // 该加密对象对相同的密码加密后可生成不同的结果
                return new BCryptPasswordEncoder();
            }
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http.formLogin()                             // 表单登录
                    // http.httpBasic() // HTTP Basic
                    .loginPage("/authentication/require")   // 登录跳转 URL
                    .loginProcessingUrl("/login")           // 处理表单登录 URL
                    .successHandler(authenticationSucessHandler) // 处理登录成功
                    .and()
                    .authorizeRequests()                    // 授权配置
                    // 登录跳转URL无需认证
                    .antMatchers("/authentication/require","/login.html","/css/**").permitAll()
                    .anyRequest()                           // 所有请求
                    .authenticated()                        // 都需要认证
                    .and().csrf().disable();                // 关闭CSRF攻击防御
            }
        }
    [3]重启项目,访问http://localhost:8080/login.html,输入用户名和密码登录成功后打印Authentication对象的信息:
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
        (password,credentials这些敏感信息,Spring Security已经将其屏蔽)
    [4]在此基础上,做登录成功后页面的跳转:
        (1)修改MyAuthenticationSucessHandler:
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
        (2)修改配置类BrowserSecurityConfig:
            @Configuration
            public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
                @Autowired
                private MyAuthenticationSucessHandler authenticationSucessHandler;
                @Override
                protected void configure(HttpSecurity http) throws Exception {
                    http.formLogin()                                // 表单登录
                        // http.httpBasic()                         // HTTP Basic
                        .loginPage("/login.html")                   // 登录跳转 URL
                        .loginProcessingUrl("/login")               // 处理表单登录 URL
                        .successHandler(authenticationSucessHandler)// 处理登录成功
                        .and()
                        .authorizeRequests()                        // 授权配置
                        // 登录跳转 URL 无需认证
                        .antMatchers( "/login.html","/css/**").permitAll() 
                        .anyRequest()                               // 所有请求
                        .authenticated()                            // 都需要认证
                        .and().csrf().disable();                    // 关闭CSRF攻击防御
                }
                @Bean
                public PasswordEncoder passwordEncoder() {
                    // 该加密对象对相同的密码加密后可生成不同的结果
                    return new BCryptPasswordEncoder();
                }
            }
    [5]登录成功后,指定去访问的路径:
        (1)修改MyAuthenticationSucessHandler:
            @Component
            public class MyAuthenticationSucessHandler implements AuthenticationSuccessHandler {
                private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
                @Override
                public void onAuthenticationSuccess(HttpServletRequest request, 
                        HttpServletResponse response,Authentication authentication) throws IOException {
                    redirectStrategy.sendRedirect(request, response, "/index");
                }
            }
        (2)修改TestController: (添加以下方法中的一个,效果都相同)
            @GetMapping("index")
            public Object index(){
                return SecurityContextHolder.getContext().getAuthentication();
            }
            @GetMapping("index")
            public Object index(Authentication authentication) {
                return authentication;
            }
3.自定义登录失败逻辑: (和自定义登录成功处理逻辑类似)
    [1]实现AuthenticationFailureHandler的onAuthenticationFailure方法:
        @Component
        public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, 
                    HttpServletResponse response,AuthenticationException exception) 
                    throws IOException {
            }
        }
        (1)onAuthenticationFailure方法的AuthenticationException参数是一个抽象类;
        (2)Spring Security根据登录失败的原因封装了许多对应的实现类，查看AuthenticationException的Hierarchy:
            (不同的失败原因对应不同的异常)
            BadCredentialsException: 用户名或密码错误;
            UsernameNotFoundException: 用户不存在;
            LockedException: 用户被锁定;
    [2]若要在登录失败的时候返回失败信息:
        (1)修改MyAuthenticationFailureHandler:
            @Component
            public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
                @Autowired
                private ObjectMapper mapper;
                @Override
                public void onAuthenticationFailure(HttpServletRequest request, 
                    HttpServletResponse response,AuthenticationException exception) throws IOException {
                    //状态码定义为500,即系统内部异常
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write(mapper.writeValueAsString(exception.getMessage()));
                }
            }
        (2)修改配置类BrowserSecurityConfig:
            @Configuration
            public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
                @Autowired
                private MyAuthenticationSucessHandler authenticationSucessHandler;
                @Autowired
                private MyAuthenticationFailureHandler authenticationFailureHandler;
                @Override
                protected void configure(HttpSecurity http) throws Exception {
                http.formLogin()                            // 表单登录
                    // http.httpBasic()                     // HTTP Basic
                    .loginPage("/login.html")               // 登录跳转 URL
                    .loginProcessingUrl("/login")           // 处理表单登录 URL
                    .successHandler(authenticationSucessHandler) // 处理登录成功
                    .failureHandler(authenticationFailureHandler)// 处理登录失败
                    .and()
                    .authorizeRequests()                    // 授权配置
                    // 登录跳转URL无需认证
                    .antMatchers( "/login.html","/css/**").permitAll() 
                    .anyRequest()                           // 所有请求
                    .authenticated()                        // 都需要认证
                    .and().csrf().disable();                // 关闭CSRF攻击防御
                }
                @Bean
                public PasswordEncoder passwordEncoder() {
                    // 该加密对象对相同的密码加密后可生成不同的结果
                    return new BCryptPasswordEncoder();
                }
            }            
```
