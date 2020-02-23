package com.example.rememberme.validate;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

/**
 * @author dengzhiming
 * @date 2020/2/21 16:59
 */
public class ImageCode {
    //image图片
    private BufferedImage image;
    //code验证码
    private String code;
    //expireTime过期时间
    private LocalDateTime expireTime;

    public ImageCode(BufferedImage image, String code, int expireIn) {
        this.image = image;
        this.code = code;
        this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
    }

    public ImageCode(BufferedImage image, String code, LocalDateTime expireTime) {
        this.image = image;
        this.code = code;
        this.expireTime = expireTime;
    }

    //isExpire方法用于判断验证码是否已过期
    boolean isExpire() {
        return LocalDateTime.now().isAfter(expireTime);
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
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
