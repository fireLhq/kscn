package top.kscn.entity;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class Captcha {
    @Value("${aliyun.captcha.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.captcha.access-key-secret}")
    private String accessKeySecret;
}
