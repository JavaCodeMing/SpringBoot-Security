
```text
1.����վ�ĵ�¼ҳ����,��ס��ѡ����һ���ܳ����Ĺ���,��ѡ��ס�Һ���һ��ʱ����,�û�������е�¼�����Ϳ��Է���ϵͳ��Դ;
2.��Spring Security����Ӽ�ס�ҹ��ܼܺ�,���¹�����: 
    [1]���û���ѡ�˼�ס��ѡ���¼�ɹ���,Spring Security������һ��token��ʶ,Ȼ�󽫸�token��ʶ�־û������ݿ�,
        ��������һ�����token���Ӧ��cookie���ظ������;
    [2]���û�����ʱ���ٴη���ϵͳʱ,�����cookieû�й���,Spring Security������cookie��������Ϣ�����ݿ��л�ȡ
        ��Ӧ��token��Ϣ,Ȼ����û��Զ���ɵ�¼����;
3.������Spring Security���ͼ����֤��Ļ���������Ӽ�ס�ҵĹ���;
```

```text
(Spring Security�ļ�ס�ҹ��ܵ�ʵ����Ҫʹ�����ݿ����־û�token)
1.���mysql,druid��mybatis����:
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
2.��application.yml���������Դ����:
    spring:
      datasource:
        druid:
          #���ݿ�������ã�ʹ��Druid����Դ
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/security?serverTimezone=GMT%2B8
          username: root
          password: root
          # ���ӳ�����
          initial-size: 5
          min-idle: 5
          max-active: 20
          # ���ӵȴ���ʱʱ��
          max-wait: 30000
          # ���ü����ԹرյĿ������Ӽ��ʱ��(���������ӵ�����)
          time-between-eviction-runs-millis: 60000
          # ���������ڳ��е���С����ʱ��
          min-evictable-idle-time-millis: 300000
          validation-query: select '1' from dual
          test-while-idle: true
          test-on-borrow: false
          test-on-return: false
          # ��PSCache������ָ��ÿ��������PSCache�Ĵ�С
          pool-prepared-statements: true
          max-open-prepared-statements: 20
          max-pool-prepared-statement-per-connection-size: 20
          # ���ü��ͳ�����ص�filters, ȥ�����ؽ���sql�޷�ͳ��, 'wall'���ڷ���ǽ
          filters: stat,wall
          # Spring���AOP����㣬��x.y.z.service.*,���ö��Ӣ�Ķ��ŷָ�
          aop-patterns: com.springboot.service.*
          # WebStatFilter����
          web-stat-filter:
            enabled: true
            # ��ӹ��˹���
            url-pattern: /*
            # ���Թ��˵ĸ�ʽ
            exclusions: '*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*'
          # StatViewServlet����
          stat-view-servlet:
            enabled: true
            # ����·��Ϊ/druidʱ����ת��StatViewServlet
            url-pattern: /druid/*
            # �Ƿ��ܹ���������
            reset-enable: false
            # ��Ҫ�˺�������ܷ��ʿ���̨
            login-username: admin
            login-password: admin
            # IP������
            # allow: 127.0.0.1
            #��IP����������ͬ����ʱ��deny������allow��
            # deny: 192.168.1.218
          # ����StatFilter
          filter:
            stat:
              log-slow-sql: true
3.��BrowserSecurityConfig�����ø�token�־û�����:
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
    [1]PersistentTokenRepositoryΪһ���ӿ���,���ڳ־û�token��Ϣ(�˴��־û������ݿ�),
        ʵ��ʹ�õ�����ʵ����JdbcTokenRepositoryImpl;
    [2]JdbcTokenRepositoryImpl��Ҫָ������Դ,���Խ����úõ�����Դ����DataSourceע��
        �����õ�JdbcTokenRepositoryImpl��dataSource������;
    [3]createTableOnStartup���������Ƿ�������Ŀʱ��������token��Ϣ�����ݱ�(false:�ֶ�����);
    [4]JdbcTokenRepositoryImpl��Դ���п��ҵ��������:
        CREATE TABLE persistent_logins (
            username VARCHAR (64) NOT NULL,
            series VARCHAR (64) PRIMARY KEY,
            token VARCHAR (64) NOT NULL,
            last_used TIMESTAMP NOT NULL
        )
4.�����¼ҳlogin.html: (�����ס�ҵĹ�ѡѡ��)
    <input type="checkbox" name="remember-me"/> ��ס��
    (����name���Ա���Ϊremember-me)
5.�޸�������BrowserSecurityConfig: (������ס�ҹ���)
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // �����֤��У�������
        http.addFilterBefore(validateCodeFilter,UsernamePasswordAuthenticationFilter.class) 
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
            .antMatchers("/login.html","/css/**","/code/image").permitAll()
            .anyRequest()    // ��������
            .authenticated() // ����Ҫ��֤
            .and().csrf().disable();
    }
    [1]rememberMe()���ڿ�����ס�ҹ���;
    [2]tokenRepository(persistentTokenRepository())����ָ��token�־û�����;
    [3]tokenValiditySeconds������token����Чʱ��,��ΪΪ��;
    [4]userDetailsService(userDetailService)���ڴ���ͨ��token�����Զ���¼;
6.������Ŀ,��¼ҳ��,������ȷ���û������뼰��֤��,�ɹ���¼��,
    �����������remember-me��cookie���������ݿ�Ҳ������token��Ϣ;
```
