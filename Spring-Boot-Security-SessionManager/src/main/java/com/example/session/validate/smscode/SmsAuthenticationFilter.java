package com.example.session.validate.smscode;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dengzhiming
 * @date 2020/2/23 18:22
 */
public class SmsAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    public static final String MOBILE_KEY = "mobile";
    private String mobileParameter = "mobile";
    private boolean postOnly = true;

    protected SmsAuthenticationFilter() {
        //指定当请求为/login/mobile,请求方法为POST时该过滤器生效
        super(new AntPathRequestMatcher("/login/mobile","POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)throws AuthenticationException {
        if(postOnly && !"POST".equalsIgnoreCase(request.getMethod())){
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String mobile = obtainMobile(request);
        if(mobile == null){
            mobile = "";
        }
        mobile = mobile.trim();
        SmsAuthenticationToken authRequest = new SmsAuthenticationToken(mobile);
        setDetails(request,authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private String obtainMobile(HttpServletRequest request) {
        return request.getParameter(mobileParameter);
    }

    private void setDetails(HttpServletRequest request, SmsAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

    public void setMobileParameter(String mobileParameter) {
        Assert.hasText(mobileParameter, "mobile parameter must not be empty or null");
        this.mobileParameter = mobileParameter;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public final String getMobileParameter() {
        return mobileParameter;
    }
}
