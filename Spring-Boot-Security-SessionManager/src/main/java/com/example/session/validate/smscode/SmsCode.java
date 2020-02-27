package com.example.session.validate.smscode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author dengzhiming
 * @date 2020/2/23 16:56
 */
public class SmsCode implements Serializable {
    //手机验证码
    private String code;
    //过期时间
    private LocalDateTime expireTime;
    public SmsCode(String code, int expireIn) {
        this.code = code;
        this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
    }
    public SmsCode(String code, LocalDateTime expireTime) {
        this.code = code;
        this.expireTime = expireTime;
    }
    //用于判断短信验证码是否已过期
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
