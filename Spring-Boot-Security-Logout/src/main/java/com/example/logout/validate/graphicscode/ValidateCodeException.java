package com.example.logout.validate.graphicscode;

import org.springframework.security.core.AuthenticationException;

/**
 * @author dengzhiming
 * @date 2020/2/22 12:59
 */
public class ValidateCodeException extends AuthenticationException {
    public ValidateCodeException(String message) {
        super(message);
    }
}
