package com.example.validatecode.validate;

import org.springframework.security.core.AuthenticationException;

/**
 * @author dengzhiming
 * @date 2020/2/22 12:59
 */
public class ValidateCodeException extends AuthenticationException {
    ValidateCodeException(String message) {
        super(message);
    }
}
