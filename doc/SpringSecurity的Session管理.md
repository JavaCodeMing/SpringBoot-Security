
```text
1.�ڡ�Spring Security������֤���¼���Ļ����Ͻ���Session����;
2.Session��ʱ����:
    [1]Session��ʱʱ��Ҳ�����û���¼����Чʱ��,ֻ���������ļ��������������:
        server:
          servlet:
            session:
              timeout: 30m
    [2]�޸������ļ�BrowserSecurityConfig:
        (����Session������,������SessionʧЧ��Ҫ��ת��URL)
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
                    .antMatchers("/login.html","/css/**","/code/image","/code/sms","/session/invalid").permitAll()
                    .anyRequest()    // ��������
                    .authenticated() // ����Ҫ��֤
                .and()
                    .sessionManagement()                    // ��� Session������
                    .invalidSessionUrl("/session/invalid")  // SessionʧЧ����ת���������
                .and().csrf().disable()
                .apply(smsAuthenticationConfig); // ��������֤����֤���üӵ�Spring Security��
        }
    [3]��Controller�����һ������,ӳ�������:
        @GetMapping("/session/invalid")
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        public String sessionInvalid(){
            return "session��ʧЧ����������֤";
        }
    [4]��Session�ĳ�ʱʱ������Ϊ1m(��1����),������Ŀ,��֤��ȴ�60�벢ˢ��ҳ��,������ת���Զ����URL:/session/invalid;
3.Session��������:
    (Session�������ƿ��Կ���һ���˺�ͬһʱ������ܵ�¼���ٸ�)
    [1]�Զ�����ڲ���MySessionExpiredStrategy:
        @Component
        public class MySessionExpiredStrategy implements SessionInformationExpiredStrategy {
            @Override
            public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
                HttpServletResponse response = event.getResponse();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json;charset=utf-8");
                response.getWriter()
                    .write("�����˺��Ѿ��ڱ�ĵط���¼����ǰ��¼��ʧЧ����������⵽й¶���������޸����룡");
            }
        }        
    [2]�޸�������BrowserSecurityConfig:
        @Autowired
        private MySessionExpiredStrategy sessionExpiredStrategy;
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
                    .antMatchers("/login.html","/css/**","/code/image","/code/sms","/session/invalid").permitAll()
                    .anyRequest()    // ��������
                    .authenticated() // ����Ҫ��֤
                .and()
                    .sessionManagement()                    // ��� Session������
                    .invalidSessionUrl("/session/invalid")  // SessionʧЧ����ת���������
                    .maximumSessions(1)
                    .expiredSessionStrategy(sessionExpiredStrategy)
                .and()
                .and().csrf().disable()
                .apply(smsAuthenticationConfig); // ��������֤����֤���üӵ�Spring Security��
        }
        (1)maximumSessions(1): �������Session��������Ϊ1��;(���¼�Ļἷ��ǰ���¼��)
        (2)expiredSessionStrategy������Session�ڲ�����ʧЧ��Ĵ������;
    [3]�Ƚ�Session��ʱʱ�����þ�һ��(��30m),Ȼ��������Ŀ,�ֱ�������������е�¼,ǰ��¼��ҳ��ˢ��,��ʾ�˺��ѵ�¼;
    [4]��Session�ﵽ�����Ч��ʱ,����������ͬ���˻���¼������:
        @Autowired
        private MySessionExpiredStrategy sessionExpiredStrategy;
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
                    .antMatchers("/login.html","/css/**","/code/image","/code/sms","/session/invalid").permitAll()
                    .anyRequest()    // ��������
                    .authenticated() // ����Ҫ��֤
                .and()
                    .sessionManagement()                    // ��� Session������
                    .invalidSessionUrl("/session/invalid")  // SessionʧЧ����ת���������
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(true)    
                    .expiredSessionStrategy(sessionExpiredStrategy)
                .and()
                .and().csrf().disable()
                .apply(smsAuthenticationConfig); // ��������֤����֤���üӵ�Spring Security��
        }
        (ʵ�ʿ����з���Session��������ֻ��Spring SecurityĬ�ϵĵ�¼��ʽ(�˺������¼)��Ч,���������֤����罻�˺ŵ�¼������Ч)
4.Session��Ⱥ����:
    [1]Session��Ⱥ���������ͷ���:
        (1)����¼�ɹ���,�û���֤����Ϣ�洢��Session��,����ЩSessionĬ���Ǵ洢���������õķ������ϵ�,����Tomcat,netty��;
        (2)��Ӧ�ü�Ⱥ����ʱ,�û���AӦ���ϵ�¼��֤��,����ͨ�����ؾ�����ܻ�������͵�BӦ��,��BӦ�÷������ϲ�û���������ƥ�����֤Session��Ϣ,�����û�����Ҫ���½�����֤;
        (3)��Session��Ϣ�洢�ڵ�����������(��Redis��Ⱥ),�����Ǹ��Եķ�����,����Ӧ�ü�Ⱥ�Ϳ���ͨ������������������Session��;
    [2]����Redis��Spring Session����:
        <!-- Spring session �Խ�Redis�ı�Ҫ���� -->
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-data-redis</artifactId>
        </dependency>
        <!-- spring boot ����Redis�������� -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
    [3]��yml������Session�洢��ʽΪRedis��Redis������:
        spring:  
          session:
            store-type: redis
            redis:
              # ���²���,ON_SAVE�ڵ���#SessionRepository#save(Session)ʱ,��response commitǰˢ�»���(Ĭ��)
              # IMMEDIATEֻҪ���κθ��¾ͻ�ˢ�»���
              flush-mode: on_save
              # �洢session����Կ�������ռ�(Ĭ��spring:session)
              namespace: spring:session
          redis:
            database: 1
            host: localhost
            port: 6379
    [4]ʹ��ͼ����֤���¼,����������: BufferedImage���������л���Redis
        (1)��ԭ����ImageCode������ImageCode��ValidateCode:
            public class ValidateCode implements Serializable {
                //code��֤��
                private String code;
                //expireTime����ʱ��
                private LocalDateTime expireTime;
                public ValidateCode(String code, int expireIn) {
                    this.code = code;
                    this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
                }
                public ValidateCode(String code, LocalDateTime expireTime) {
                    this.code = code;
                    this.expireTime = expireTime;
                }
                //isExpire���������ж���֤���Ƿ��ѹ���
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
                //imageͼƬ
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
        (2)����Controller������ͼ����֤��ķ���:
            @GetMapping("/code/image")
            public void createCode(HttpServletRequest request, HttpServletResponse response)
                    throws IOException {
                ImageCode imageCode = createImageCode();
                BufferedImage image = imageCode.getImage();
                imageCode.setImage(null);
                //��������֤�뱣�浽��Session��,��Ӧ��keyΪSESSION_KEY_SMS_CODE
                sessionStrategy.setAttribute(new ServletWebRequest(request),
                    SESSION_KEY_IMAGE_COE, imageCode);
                ImageIO.write(image, "jpeg", response.getOutputStream());
            }
    [5]����:
        (1)����Redis,������������Ӧ��ʵ��,һ���˿�Ϊ8080,��һ���˿�Ϊ8090;
        (2)��8080�˿�Ӧ���ϵ�¼,����¼�ɹ���ĵ�ַ�Ķ˿ڸĳ�8090����,�����Ȼ���Է���;
        (3)���ʵ���˼�Ⱥ��Session����;
5.��������: SessionRegistry������һЩʹ�õĲ���Session�ķ���
    [1]�߳��û�(��SessionʧЧ):
        String currentSessionId = request.getRequestedSessionId();
        sessionRegistry.getSessionInformation(sessionId).expireNow();
    [2]��ȡ����Session��Ϣ:
        List<Object> principals = sessionRegistry.getAllPrincipals();
```
