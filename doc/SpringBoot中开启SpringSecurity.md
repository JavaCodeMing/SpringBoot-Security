
```
1.Spring Security是一款基于Spring的安全框架,主要包含认证和授权两大安全模块,和另外一款流行的安全框架Apache Shiro相比,它拥有更为强大的功能;
2.Spring Security也可以轻松的自定义扩展以满足各种需求,并且对常见的Web安全攻击提供了防护支持;
```
# 开启Spring Security
```text
1.引入依赖:
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
2.创建TestController:
    @RestController
    public class TestController {
        @GetMapping("hello")
        public String hello() {
            return "hello spring security";
        }
    }
3.启动项目,访问测试: http://localhost:8080/hello
    [1]访问路径被重定向到登录路径: http://localhost:8080/login
    [2]Sping Security默认开启一个基于表单类型的认证,所有服务的访问都必须先过这个认证:
        (1)默认的用户名为user;
        (2)密码由Sping Security自动生成,并在项目启动时打印在控制台;
    [3]输入用户名和密码,验证成功后,重定向到开始的访问路径,进行请求;
4.若要使用HTTP Basic的认证方式(页面弹出了个HTTP Basic认证框),需要添加一下配置:
    @Configuration
    public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            //http.formLogin()              // 表单方式
            http.httpBasic()                // httpBasic方式
                    .and()
                    .authorizeRequests()    // 授权配置
                    .anyRequest()           // 所有请求
                    .authenticated();       // 都需要认证
        }
    }
    (1)WebSecurityConfigurerAdapter是由Spring Security提供的Web应用安全配置的适配器;
```
# 基本原理
```text
1.代码的执行过程:
    request->UsernamePasswordAuthenticationFilter->BasicAuthenticationFilter->...->ExceptionTranslateFilter->FilterSecurityInterceptor->RESTfull API
    response<-UsernamePasswordAuthenticationFilter<-BasicAuthenticationFilter<-...<-ExceptionTranslateFilter<-FilterSecurityInterceptor<-RESTfull API
    Spring Security包含了众多的过滤器,这些过滤器形成了一条链,所有请求都必须通过这些过滤器后才能成功访问到资源:
        [1]UsernamePasswordAuthenticationFilter: 用于处理基于表单方式的登录认证;
        [2]BasicAuthenticationFilter: 用于处理基于HTTP Basic方式的登录验证;
        [3]...: 可能包含一系列别的过滤器(可以通过相应配置开启)
        [4]FilterSecurityInterceptor: 用于判断当前请求身份认证是否成功,是否有相应的权限;
            当身份认证失败或者权限不足的时候便会抛出相应的异常;
        [5]ExceptionTranslateFilter: 用于处理了FilterSecurityInterceptor抛出的异常并进行处理;
            如需要身份认证时将请求重定向到相应的认证页面,当认证失败或者权限不足时返回相应的提示信息;
```
