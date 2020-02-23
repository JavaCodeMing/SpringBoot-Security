
```text
1.在网站的登录页面中,记住我选项是一个很常见的功能,勾选记住我后在一段时间内,用户无需进行登录操作就可以访问系统资源;
2.在Spring Security中添加记住我功能很简单,大致过程是: 
    [1]当用户勾选了记住我选项并登录成功后,Spring Security会生成一个token标识,然后将该token标识持久化到数据库,
        并且生成一个与该token相对应的cookie返回给浏览器;
    [2]当用户过段时间再次访问系统时,如果该cookie没有过期,Spring Security便会根据cookie包含的信息从数据库中获取
        相应的token信息,然后帮用户自动完成登录操作;
3.本节在Spring Security添加图形验证码的基础上来添加记住我的功能;
```

```text
(Spring Security的记住我功能的实现需要使用数据库来持久化token)
1.添加mysql,druid和mybatis依赖:
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.1.20</version>
    </dependency>
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.1.1</version>
    </dependency>
2.在application.yml中添加数据源配置:
    spring:
      datasource:
        druid:
          #数据库访问配置，使用Druid数据源
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/security?serverTimezone=GMT%2B8
          username: root
          password: root
          # 连接池配置
          initial-size: 5
          min-idle: 5
          max-active: 20
          # 连接等待超时时间
          max-wait: 30000
          # 配置检测可以关闭的空闲连接间隔时间(检测空闲连接的周期)
          time-between-eviction-runs-millis: 60000
          # 配置连接在池中的最小生存时间
          min-evictable-idle-time-millis: 300000
          validation-query: select '1' from dual
          test-while-idle: true
          test-on-borrow: false
          test-on-return: false
          # 打开PSCache，并且指定每个连接上PSCache的大小
          pool-prepared-statements: true
          max-open-prepared-statements: 20
          max-pool-prepared-statement-per-connection-size: 20
          # 配置监控统计拦截的filters, 去掉后监控界面sql无法统计, 'wall'用于防火墙
          filters: stat,wall
          # Spring监控AOP切入点，如x.y.z.service.*,配置多个英文逗号分隔
          aop-patterns: com.springboot.service.*
          # WebStatFilter配置
          web-stat-filter:
            enabled: true
            # 添加过滤规则
            url-pattern: /*
            # 忽略过滤的格式
            exclusions: '*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*'
          # StatViewServlet配置
          stat-view-servlet:
            enabled: true
            # 访问路径为/druid时，跳转到StatViewServlet
            url-pattern: /druid/*
            # 是否能够重置数据
            reset-enable: false
            # 需要账号密码才能访问控制台
            login-username: admin
            login-password: admin
            # IP白名单
            # allow: 127.0.0.1
            #　IP黑名单（共同存在时，deny优先于allow）
            # deny: 192.168.1.218
          # 配置StatFilter
          filter:
            stat:
              log-slow-sql: true
3.在BrowserSecurityConfig中配置个token持久化对象:
    @Autowired
    private UserDetailService userDetailService;
    @Autowired
    private DataSource dataSource;
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        jdbcTokenRepository.setCreateTableOnStartup(false);
        return jdbcTokenRepository;
    }
    [1]PersistentTokenRepository为一个接口类,用于持久化token信息(此处持久化到数据库),
        实际使用的是其实现类JdbcTokenRepositoryImpl;
    [2]JdbcTokenRepositoryImpl需要指定数据源,所以将配置好的数据源对象DataSource注入
        并配置到JdbcTokenRepositoryImpl的dataSource属性中;
    [3]createTableOnStartup属性用于是否启动项目时创建保存token信息的数据表(false:手动创建);
    [4]JdbcTokenRepositoryImpl的源码中可找到建表语句:
        CREATE TABLE persistent_logins (
            username VARCHAR (64) NOT NULL,
            series VARCHAR (64) PRIMARY KEY,
            token VARCHAR (64) NOT NULL,
            last_used TIMESTAMP NOT NULL
        )
4.改造登录页login.html: (加入记住我的勾选选项)
    <input type="checkbox" name="remember-me"/> 记住我
    (其中name属性必须为remember-me)
5.修改配置类BrowserSecurityConfig: (开启记住我功能)
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 添加验证码校验过滤器
        http.addFilterBefore(validateCodeFilter,UsernamePasswordAuthenticationFilter.class) 
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
            .antMatchers("/login.html","/css/**","/code/image").permitAll()
            .anyRequest()    // 所有请求
            .authenticated() // 都需要认证
            .and().csrf().disable();
    }
    [1]rememberMe()用于开启记住我功能;
    [2]tokenRepository(persistentTokenRepository())用于指定token持久化方法;
    [3]tokenValiditySeconds配置了token的有效时长,单为为秒;
    [4]userDetailsService(userDetailService)用于处理通过token对象自动登录;
6.重启项目,登录页面,输入正确的用户名密码及验证码,成功登录后,
    浏览器保存了remember-me的cookie对象并且数据库也保存了token信息;
```
