package com.example.session.validate.smscode;

import com.example.session.security.UserDetailService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author dengzhiming
 * @date 2020/2/23 18:46
 */
public class SmsAuthenticationProvider implements AuthenticationProvider {
    private UserDetailService userDetailService;

    //用于编写具体的身份认证逻辑
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken authenticationToken = (SmsAuthenticationToken) authentication;
        //从SmsAuthenticationToken中取出了手机号信息,再通过手机号去数据库中查询用户信息
        UserDetails userDetails = userDetailService.loadUserByUsername((String) authenticationToken.getPrincipal());
        //如果存在该用户则认证通过,构造一个认证通过的Token,包含了用户信息和用户权限
        if (userDetails == null) {
            throw new InternalAuthenticationServiceException("未找到与该手机号对应的用户");
        }
        SmsAuthenticationToken authenticationResult = new SmsAuthenticationToken(userDetails, userDetails.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());
        return authenticationResult;
    }

    //指定支持处理的Token类型为SmsAuthenticationToken
    @Override
    public boolean supports(Class<?> aClass) {
        return SmsAuthenticationToken.class.isAssignableFrom(aClass);
    }

    public UserDetailService getUserDetailService() {
        return userDetailService;
    }

    public void setUserDetailService(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }
}
