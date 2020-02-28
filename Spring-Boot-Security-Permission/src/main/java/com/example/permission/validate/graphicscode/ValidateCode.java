package com.example.permission.validate.graphicscode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author dengzhiming
 * @date 2020/2/27 17:10
 */
public class ValidateCode implements Serializable {
    //code验证码
    private String code;
    //expireTime过期时间
    private LocalDateTime expireTime;

    public ValidateCode(String code, int expireIn) {
        this.code = code;
        this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
    }
    public ValidateCode(String code, LocalDateTime expireTime) {
        this.code = code;
        this.expireTime = expireTime;
    }

    //isExpire方法用于判断验证码是否已过期
    boolean isExpire() {
        return LocalDateTime.now().isAfter(expireTime);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
}
