
```
1.添加验证码大致可以分为三个步骤 
    [1]根据随机数生成验证码图片;
    [2]将验证码图片显示到登录页面;
    [3]认证流程中加入验证码校验;
2.Spring Security的认证校验是由UsernamePasswordAuthenticationFilter过滤器完成的,所以验证码校验逻辑应该在这个过滤器之前;
3.该节在Spring Security自定义用户认证的基础上加入验证码校验功能
```

```text
1.生成验证码图片
    [1]引入验证码功能用到的依赖及工具类依赖
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
    [2]定义验证码对象ImageCode
        public class ImageCode {
            image图片
            private BufferedImage image;
            code验证码
            private String code;
            expireTime过期时间
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
            isExpire方法用于判断验证码是否已过期
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
    [3]定义ValidateCodeController (用于处理生成验证码请求)
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
                验证码图片宽度
                int width = 110;
                验证码图片长度
                int height = 45;
                验证码位数
                int length = 6;
                验证码有效时间 60s
                int expireIn = 60;
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                用于绘制图形的对象
                Graphics graphics = image.getGraphics();
                Random random = new Random();
                setColor 指定此图形的颜色,对后续图形操作生效
                graphics.setColor(getRandColor(200, 250));
                fillRect 填充指定的矩形(x,y确定原点位置;width,height确定宽度和高度)
                graphics.fillRect(0, 0, width, height);
                graphics.setColor(getRandColor(160, 200));
                setFont 指定此图形内容的字体,对后续文本操作生效
                graphics.setFont(new Font(Times New Roman, Font.ITALIC, 20));
                for (int i = 0; i  155; i++) {
                    int x = random.nextInt(width);
                    int y = random.nextInt(height);
                    int xl = random.nextInt(12);
                    int yl = random.nextInt(12);
                    drawLine 使用当前颜色绘制一条线
                    drawLine(第一点x坐标,第一点y坐标,第二点x坐标,第二点y坐标)
                    此处用于绘制背影,用于提升识别难度
                    graphics.drawLine(x, y, x + xl, y + yl);
                }
                StringBuilder sRand = new StringBuilder();
                 设置验证码的码值
                for (int i = 0; i  length; i++) {
                    String rand = String.valueOf(random.nextInt(10));
                    存储生成的验证码码值,用于之后的验证
                    sRand.append(rand);
                    graphics.setColor(new Color(20+random.nextInt(110),20+random.nextInt(110),20+random.nextInt(110)));
                    drawString 使用指定的图形内容当前字体和颜色的字符串绘制文本
                    drawString(要绘制的文本,x坐标,y坐标)
                    graphics.drawString(rand, 18  i + 5, 28);
                }
                图形绘制结束
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
2.登录改造
    [1]修改登录页login.html 
        !DOCTYPE html
        html
        head
            meta charset=UTF-8
            title登录title
            link rel=stylesheet href=.csslogin.css type=textcss
        head
        body
        form class=login-page action=login     method=post
            div class=form
                h3账户登录h3
                input type=text placeholder=用户名 name=username required=required
                input type=password placeholder=密码 name=password required=required
                span style=display inline;
                    input type=text name=imageCode placeholder=验证码 style=width 50%;
                    !--src属性对应ValidateController的createImageCode方法--
                    img src=codeimage style=margin-bottom -17px
                span
                button type=submit登录button
            div
        form
        body
        html
    [2]修改配置类BrowserSecurityConfig (将生成验证码的请求设置为不拦截)
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.formLogin()                              表单登录
             http.httpBasic()                      HTTP Basic
                .loginPage(login.html)                登录跳转 URL
                .loginProcessingUrl(login)            处理表单登录 URL
                .successHandler(authenticationSucessHandler)   处理登录成功
                .failureHandler(authenticationFailureHandler)  处理登录失败
                .and()
                .authorizeRequests()                     授权配置
                 登录跳转URL无需认证
                .antMatchers( login.html,css,codeimage).permitAll() 
                .anyRequest()                            所有请求
                .authenticated()                         都需要认证
                .and().csrf().disable();                 关闭CSRF攻击防御
        }
3.认证流程添加验证码校验
    [1]定义验证码类型的异常类
        public class ValidateCodeException extends AuthenticationException {
            ValidateCodeException(String message) {
                super(message);
            }
        }
        (注 继承的是AuthenticationException而不是Exception)
    [2]定义验证码校验的过滤器ValidateCodeFilter
        (该过滤器是在处理用户登录逻辑的过滤器之前,即只有验证码校验通过后采去校验用户名和密码)
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
                 从session中获取验证码对象
                ImageCode codeInSession = (ImageCode)sessionStrategy.getAttribute(servletWebRequest, 
                    ValidateController.SESSION_KEY_IMAGE_COE);
                 从请求中获取验证码对象
                String codeInRequest = 
                    ServletRequestUtils.getStringParameter(servletWebRequest.getRequest(), imageCode);
                if (codeInRequest == null) {
                    throw new ValidateCodeException(验证码不存在！);
                }
                if (StringUtils.isBlank(codeInRequest)) {
                    throw new ValidateCodeException(验证码不能为空);
                }
                if (codeInSession.isExpire()) {
                     验证码过期后移除
                    sessionStrategy.removeAttribute(servletWebRequest, ValidateController.SESSION_KEY_IMAGE_COE);
                    throw new ValidateCodeException(验证码已过期！);
                }
                if (!StringUtils.equalsIgnoreCase(codeInSession.getCode(), codeInRequest)) {
                    throw new ValidateCodeException(验证码不正确！);
                }
                 验证码成功验证一次后移除
                sessionStrategy.removeAttribute(servletWebRequest, ValidateController.SESSION_KEY_IMAGE_COE);
            }
        }
        (1)ValidateCodeFilter继承了org.springframework.web.filter.OncePerRequestFilter,该过滤器只会执行一次;
        (2)在doFilterInternal方法中我们判断了请求URL是否为login,该路径对应登录form表单的action路径,
            请求的方法是否为POST，是的话进行验证码校验逻辑，否则直接执行filterChain.doFilter让代码往下走;
        (3)当在验证码校验的过程中捕获到异常时,调用Spring Security的校验失败处理器AuthenticationFailureHandler进行处理;
    [3]修改配置类BrowserSecurityConfig (添加验证码验证过滤)
        @Override
        protected void configure(HttpSecurity http) throws Exception {
             添加验证码校验过滤器
            http.addFilterBefore(validateCodeFilter,UsernamePasswordAuthenticationFilter.class)
                .formLogin()                             表单登录
                 http.httpBasic()                      HTTP Basic
                .loginPage(login.html)                登录跳转 URL
                .loginProcessingUrl(login)            处理表单登录 URL
                .successHandler(authenticationSucessHandler)   处理登录成功
                .failureHandler(authenticationFailureHandler)  处理登录失败
                .and()
                .authorizeRequests()                     授权配置
                 无需认证的请求路径
                .antMatchers(login.html,css,codeimage).permitAll() 
                .anyRequest()     所有请求
                .authenticated()  都需要认证
                .and().csrf().disable();
        }
4.重启项目,访问httplocalhost8080login.html,分别验证
    [1]验证码为空;
    [2]验证码不正确;
    [3]验证码过期;
    [4]验证码正确;
```
