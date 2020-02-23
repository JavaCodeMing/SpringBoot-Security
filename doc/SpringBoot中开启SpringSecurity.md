
```
1.Spring Security��һ�����Spring�İ�ȫ���,��Ҫ������֤����Ȩ����ȫģ��,������һ�����еİ�ȫ���Apache Shiro���,��ӵ�и�Ϊǿ��Ĺ���;
2.Spring SecurityҲ�������ɵ��Զ�����չ�������������,���ҶԳ�����Web��ȫ�����ṩ�˷���֧��;
```
# ����Spring Security
```text
1.��������:
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
2.����TestController:
    @RestController
    public class TestController {
        @GetMapping("hello")
        public String hello() {
            return "hello spring security";
        }
    }
3.������Ŀ,���ʲ���: http://localhost:8080/hello
    [1]����·�����ض��򵽵�¼·��: http://localhost:8080/login
    [2]Sping SecurityĬ�Ͽ���һ�����ڱ����͵���֤,���з���ķ��ʶ������ȹ������֤:
        (1)Ĭ�ϵ��û���Ϊuser;
        (2)������Sping Security�Զ�����,������Ŀ����ʱ��ӡ�ڿ���̨;
    [3]�����û���������,��֤�ɹ���,�ض��򵽿�ʼ�ķ���·��,��������;
4.��Ҫʹ��HTTP Basic����֤��ʽ(ҳ�浯���˸�HTTP Basic��֤��),��Ҫ���һ������:
    @Configuration
    public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            //http.formLogin()              // ����ʽ
            http.httpBasic()                // httpBasic��ʽ
                    .and()
                    .authorizeRequests()    // ��Ȩ����
                    .anyRequest()           // ��������
                    .authenticated();       // ����Ҫ��֤
        }
    }
    (1)WebSecurityConfigurerAdapter����Spring Security�ṩ��WebӦ�ð�ȫ���õ�������;
```
# ����ԭ��
```text
1.�����ִ�й���:
    request->UsernamePasswordAuthenticationFilter->BasicAuthenticationFilter->...->ExceptionTranslateFilter->FilterSecurityInterceptor->RESTfull API
    response<-UsernamePasswordAuthenticationFilter<-BasicAuthenticationFilter<-...<-ExceptionTranslateFilter<-FilterSecurityInterceptor<-RESTfull API
    Spring Security�������ڶ�Ĺ�����,��Щ�������γ���һ����,�������󶼱���ͨ����Щ����������ܳɹ����ʵ���Դ:
        [1]UsernamePasswordAuthenticationFilter: ���ڴ�����ڱ���ʽ�ĵ�¼��֤;
        [2]BasicAuthenticationFilter: ���ڴ������HTTP Basic��ʽ�ĵ�¼��֤;
        [3]...: ���ܰ���һϵ�б�Ĺ�����(����ͨ����Ӧ���ÿ���)
        [4]FilterSecurityInterceptor: �����жϵ�ǰ���������֤�Ƿ�ɹ�,�Ƿ�����Ӧ��Ȩ��;
            �������֤ʧ�ܻ���Ȩ�޲����ʱ�����׳���Ӧ���쳣;
        [5]ExceptionTranslateFilter: ���ڴ�����FilterSecurityInterceptor�׳����쳣�����д���;
            ����Ҫ�����֤ʱ�������ض�����Ӧ����֤ҳ��,����֤ʧ�ܻ���Ȩ�޲���ʱ������Ӧ����ʾ��Ϣ;
```
