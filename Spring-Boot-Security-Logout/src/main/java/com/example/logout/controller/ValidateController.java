package com.example.logout.controller;

import com.example.logout.validate.graphicscode.ImageCode;
import com.example.logout.validate.smscode.SmsCode;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * @author dengzhiming
 * @date 2020/2/22 10:09
 */
@RestController
public class ValidateController {
    public final static String SESSION_KEY_IMAGE_COE = "SESSION_KEY_IMAGE_COE";
    public final static String SESSION_KEY_SMS_CODE = "SESSION_KEY_SMS_CODE";
    private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

    @GetMapping("/code/image")
    public void createCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ImageCode imageCode = createImageCode();
        BufferedImage image = imageCode.getImage();
        imageCode.setImage(null);
        //将短信验证码保存到了Session中,对应的key为SESSION_KEY_SMS_CODE
        sessionStrategy.setAttribute(new ServletWebRequest(request), SESSION_KEY_IMAGE_COE, imageCode);
        ImageIO.write(image, "jpeg", response.getOutputStream());
    }

    @GetMapping("/code/sms")
    public void createSmsCode(HttpServletRequest request, HttpServletResponse response, String mobile) throws IOException {
        SmsCode smsCode = createSmsCode();
        sessionStrategy.setAttribute(new ServletWebRequest(request), SESSION_KEY_SMS_CODE + mobile, smsCode);
        // 输出验证码到控制台代替短信发送服务
        System.out.println("您的登录验证码为：" + smsCode.getCode() + "，有效时间为60秒");
    }

    private SmsCode createSmsCode() {
        //生成了一个6位的纯数字随机数,有效时间为60秒
        String code = RandomStringUtils.randomNumeric(6);
        return new SmsCode(code, 60);
    }

    private ImageCode createImageCode() {
        //验证码图片宽度
        int width = 110;
        //验证码图片长度
        int height = 45;
        //验证码位数
        int length = 6;
        //验证码有效时间 60s
        int expireIn = 60;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //用于绘制图形的对象
        Graphics graphics = image.getGraphics();
        Random random = new Random();
        //setColor: 指定此图形的颜色,对后续图形操作生效
        graphics.setColor(getRandColor(200, 250));
        //fillRect: 填充指定的矩形(x,y:确定原点位置;width,height:确定宽度和高度)
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(getRandColor(160, 200));
        //setFont: 指定此图形内容的字体,对后续文本操作生效
        graphics.setFont(new Font("Times New Roman", Font.ITALIC, 20));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            //drawLine: 使用当前颜色绘制一条线
            //drawLine(第一点x坐标,第一点y坐标,第二点x坐标,第二点y坐标)
            //此处用于绘制背影,用于提升识别难度
            graphics.drawLine(x, y, x + xl, y + yl);
        }
        StringBuilder sRand = new StringBuilder();
        // 设置验证码的码值
        for (int i = 0; i < length; i++) {
            String rand = String.valueOf(random.nextInt(10));
            //存储生成的验证码码值,用于之后的验证
            sRand.append(rand);
            graphics.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            //drawString: 使用指定的图形内容当前字体和颜色的字符串绘制文本
            //drawString(要绘制的文本,x坐标,y坐标)
            graphics.drawString(rand, 18 * i + 5, 28);
        }
        //图形绘制结束
        graphics.dispose();
        return new ImageCode(image, sRand.toString(), expireIn);
    }

    private Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
