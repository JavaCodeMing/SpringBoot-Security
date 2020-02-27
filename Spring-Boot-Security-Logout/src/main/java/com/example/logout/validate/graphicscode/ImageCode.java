package com.example.logout.validate.graphicscode;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

/**
 * @author dengzhiming
 * @date 2020/2/21 16:59
 */
public class ImageCode extends ValidateCode {
    //image图片
    private BufferedImage image;

    public ImageCode(BufferedImage image, String code, int expireIn) {
        super(code, expireIn);
        this.image = image;
    }

    public ImageCode(BufferedImage image, String code, LocalDateTime expireTime) {
        super(code,expireTime);
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

}
