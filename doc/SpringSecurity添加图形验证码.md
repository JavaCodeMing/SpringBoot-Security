
```
1.�����֤����¿��Է�Ϊ�������� 
    [1]���������������֤��ͼƬ;
    [2]����֤��ͼƬ��ʾ����¼ҳ��;
    [3]��֤�����м�����֤��У��;
2.Spring Security����֤У������UsernamePasswordAuthenticationFilter��������ɵ�,������֤��У���߼�Ӧ�������������֮ǰ;
3.�ý���Spring Security�Զ����û���֤�Ļ����ϼ�����֤��У�鹦��
```

```text
1.������֤��ͼƬ
    [1]������֤�빦���õ�������������������
        dependency
            groupIdorg.springframework.socialgroupId
            artifactIdspring-social-configartifactId
            version1.1.6.RELEASEversion
        dependency
        dependency
            groupIdorg.apache.commonsgroupId
            artifactIdcommons-lang3artifactId
            version3.9version
        dependency
    [2]������֤�����ImageCode
        public class ImageCode {
            imageͼƬ
            private BufferedImage image;
            code��֤��
            private String code;
            expireTime����ʱ��
            private LocalDateTime expireTime;
            public ImageCode(BufferedImage image, String code, int expireIn) {
                this.image = image;
                this.code = code;
                this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
            }
            public ImageCode(BufferedImage image,String code,LocalDateTime expireTime) {
                this.image = image;
                this.code = code;
                this.expireTime = expireTime;
            }
            isExpire���������ж���֤���Ƿ��ѹ���
            boolean isExpire() { return LocalDateTime.now().isAfter(expireTime); }
            public BufferedImage getImage() { return image; }
            public void setImage(BufferedImage image) { this.image = image; }
            public String getCode() { return code; }
            public void setCode(String code) { this.code = code; }
            public LocalDateTime getExpireTime() { return expireTime; }
            public void setExpireTime(LocalDateTime expireTime) {
                this.expireTime = expireTime;
            }
        }
    [3]����ValidateCodeController (���ڴ���������֤������)
        @RestController
        public class ValidateController {
            public final static String SESSION_KEY_IMAGE_COE = SESSION_KEY_IMAGE_COE;
            private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
            @GetMapping(codeimage)
            public void createCode(HttpServletRequest request,HttpServletResponse response) 
                throws IOException {
                ImageCode imageCode = createImageCode();
                sessionStrategy.setAttribute(new ServletWebRequest(request),SESSION_KEY_IMAGE_COE,imageCode);
                ImageIO.write(imageCode.getImage(), jpeg, response.getOutputStream());
            }
            private ImageCode createImageCode() {
                ��֤��ͼƬ���
                int width = 110;
                ��֤��ͼƬ����
                int height = 45;
                ��֤��λ��
                int length = 6;
                ��֤����Чʱ�� 60s
                int expireIn = 60;
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                ���ڻ���ͼ�εĶ���
                Graphics graphics = image.getGraphics();
                Random random = new Random();
                setColor ָ����ͼ�ε���ɫ,�Ժ���ͼ�β�����Ч
                graphics.setColor(getRandColor(200, 250));
                fillRect ���ָ���ľ���(x,yȷ��ԭ��λ��;width,heightȷ����Ⱥ͸߶�)
                graphics.fillRect(0, 0, width, height);
                graphics.setColor(getRandColor(160, 200));
                setFont ָ����ͼ�����ݵ�����,�Ժ����ı�������Ч
                graphics.setFont(new Font(Times New Roman, Font.ITALIC, 20));
                for (int i = 0; i  155; i++) {
                    int x = random.nextInt(width);
                    int y = random.nextInt(height);
                    int xl = random.nextInt(12);
                    int yl = random.nextInt(12);
                    drawLine ʹ�õ�ǰ��ɫ����һ����
                    drawLine(��һ��x����,��һ��y����,�ڶ���x����,�ڶ���y����)
                    �˴����ڻ��Ʊ�Ӱ,��������ʶ���Ѷ�
                    graphics.drawLine(x, y, x + xl, y + yl);
                }
                StringBuilder sRand = new StringBuilder();
                 ������֤�����ֵ
                for (int i = 0; i  length; i++) {
                    String rand = String.valueOf(random.nextInt(10));
                    �洢���ɵ���֤����ֵ,����֮�����֤
                    sRand.append(rand);
                    graphics.setColor(new Color(20+random.nextInt(110),20+random.nextInt(110),20+random.nextInt(110)));
                    drawString ʹ��ָ����ͼ�����ݵ�ǰ�������ɫ���ַ��������ı�
                    drawString(Ҫ���Ƶ��ı�,x����,y����)
                    graphics.drawString(rand, 18  i + 5, 28);
                }
                ͼ�λ��ƽ���
                graphics.dispose();
                return new ImageCode(image, sRand.toString(), expireIn);
            }
            private Color getRandColor(int fc, int bc) {
                Random random = new Random();
                if (fc  255) { fc = 255; }
                if (bc  255) { bc = 255; }
                int r = fc + random.nextInt(bc - fc);
                int g = fc + random.nextInt(bc - fc);
                int b = fc + random.nextInt(bc - fc);
                return new Color(r, g, b);
            }
        }
2.��¼����
    [1]�޸ĵ�¼ҳlogin.html 
        !DOCTYPE html
        html
        head
            meta charset=UTF-8
            title��¼title
            link rel=stylesheet href=.csslogin.css type=textcss
        head
        body
        form class=login-page action=login     method=post
            div class=form
                h3�˻���¼h3
                input type=text placeholder=�û��� name=username required=required
                input type=password placeholder=���� name=password required=required
                span style=display inline;
                    input type=text name=imageCode placeholder=��֤�� style=width 50%;
                    !--src���Զ�ӦValidateController��createImageCode����--
                    img src=codeimage style=margin-bottom -17px
                span
                button type=submit��¼button
            div
        form
        body
        html
    [2]�޸�������BrowserSecurityConfig (��������֤�����������Ϊ������)
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.formLogin()                              ����¼
             http.httpBasic()                      HTTP Basic
                .loginPage(login.html)                ��¼��ת URL
                .loginProcessingUrl(login)            �������¼ URL
                .successHandler(authenticationSucessHandler)   �����¼�ɹ�
                .failureHandler(authenticationFailureHandler)  �����¼ʧ��
                .and()
                .authorizeRequests()                     ��Ȩ����
                 ��¼��תURL������֤
                .antMatchers( login.html,css,codeimage).permitAll() 
                .anyRequest()                            ��������
                .authenticated()                         ����Ҫ��֤
                .and().csrf().disable();                 �ر�CSRF��������
        }
3.��֤���������֤��У��
    [1]������֤�����͵��쳣��
        public class ValidateCodeException extends AuthenticationException {
            ValidateCodeException(String message) {
                super(message);
            }
        }
        (ע �̳е���AuthenticationException������Exception)
    [2]������֤��У��Ĺ�����ValidateCodeFilter
        (�ù��������ڴ����û���¼�߼��Ĺ�����֮ǰ,��ֻ����֤��У��ͨ�����ȥУ���û���������)
        @Component
        public class ValidateCodeFilter extends OncePerRequestFilter {
            @Autowired
            private AuthenticationFailureHandler authenticationFailureHandler;
            private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
            @Override
            protected void doFilterInternal(HttpServletRequest httpServletRequest,
                        HttpServletResponse httpServletResponse, FilterChain filterChain)
                    throws ServletException, IOException {
                if (StringUtils.equalsIgnoreCase(login, httpServletRequest.getRequestURI())
                        && StringUtils.equalsIgnoreCase(httpServletRequest.getMethod(), post)) {
                    try {
                        validateCode(new ServletWebRequest(httpServletRequest));
                    } catch (ValidateCodeException e) {
                        authenticationFailureHandler.onAuthenticationFailure(httpServletRequest,httpServletResponse,e);
                        return;
                    }
                }
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            }
            private void validateCode(ServletWebRequest servletWebRequest) throws ServletRequestBindingException {
                 ��session�л�ȡ��֤�����
                ImageCode codeInSession = (ImageCode)sessionStrategy.getAttribute(servletWebRequest, 
                    ValidateController.SESSION_KEY_IMAGE_COE);
                 �������л�ȡ��֤�����
                String codeInRequest = 
                    ServletRequestUtils.getStringParameter(servletWebRequest.getRequest(), imageCode);
                if (codeInRequest == null) {
                    throw new ValidateCodeException(��֤�벻���ڣ�);
                }
                if (StringUtils.isBlank(codeInRequest)) {
                    throw new ValidateCodeException(��֤�벻��Ϊ��);
                }
                if (codeInSession.isExpire()) {
                     ��֤����ں��Ƴ�
                    sessionStrategy.removeAttribute(servletWebRequest, ValidateController.SESSION_KEY_IMAGE_COE);
                    throw new ValidateCodeException(��֤���ѹ��ڣ�);
                }
                if (!StringUtils.equalsIgnoreCase(codeInSession.getCode(), codeInRequest)) {
                    throw new ValidateCodeException(��֤�벻��ȷ��);
                }
                 ��֤��ɹ���֤һ�κ��Ƴ�
                sessionStrategy.removeAttribute(servletWebRequest, ValidateController.SESSION_KEY_IMAGE_COE);
            }
        }
        (1)ValidateCodeFilter�̳���org.springframework.web.filter.OncePerRequestFilter,�ù�����ֻ��ִ��һ��;
        (2)��doFilterInternal�����������ж�������URL�Ƿ�Ϊlogin,��·����Ӧ��¼form����action·��,
            ����ķ����Ƿ�ΪPOST���ǵĻ�������֤��У���߼�������ֱ��ִ��filterChain.doFilter�ô���������;
        (3)������֤��У��Ĺ����в����쳣ʱ,����Spring Security��У��ʧ�ܴ�����AuthenticationFailureHandler���д���;
    [3]�޸�������BrowserSecurityConfig (�����֤����֤����)
        @Override
        protected void configure(HttpSecurity http) throws Exception {
             �����֤��У�������
            http.addFilterBefore(validateCodeFilter,UsernamePasswordAuthenticationFilter.class)
                .formLogin()                             ����¼
                 http.httpBasic()                      HTTP Basic
                .loginPage(login.html)                ��¼��ת URL
                .loginProcessingUrl(login)            �������¼ URL
                .successHandler(authenticationSucessHandler)   �����¼�ɹ�
                .failureHandler(authenticationFailureHandler)  �����¼ʧ��
                .and()
                .authorizeRequests()                     ��Ȩ����
                 ������֤������·��
                .antMatchers(login.html,css,codeimage).permitAll() 
                .anyRequest()     ��������
                .authenticated()  ����Ҫ��֤
                .and().csrf().disable();
        }
4.������Ŀ,����httplocalhost8080login.html,�ֱ���֤
    [1]��֤��Ϊ��;
    [2]��֤�벻��ȷ;
    [3]��֤�����;
    [4]��֤����ȷ;
```
