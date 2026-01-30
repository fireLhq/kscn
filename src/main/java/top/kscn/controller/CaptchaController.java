package top.kscn.controller;

import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaRequest;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaResponse;
import com.aliyun.captcha20230305.Client;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.kscn.common.Result;
import top.kscn.entity.Captcha;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/user/auth")
public class CaptchaController {

    @Autowired
    private Captcha captcha;

    @PostMapping("/captcha")
    public Result<Boolean> verifyCaptcha(@RequestBody String captchaVerifyParam) throws Exception {

        // ====================== 1. 初始化配置 ======================
        Config config = new Config();

        // 设置您的AccessKey ID 和 AccessKey Secret
        config.accessKeyId = captcha.getAccessKeyId();
        config.accessKeySecret = captcha.getAccessKeySecret();

        config.endpoint = "captcha.cn-shanghai.aliyuncs.com"; //设置请求地址
        config.connectTimeout = 5000; // 设置连接超时为5000毫秒
        config.readTimeout = 5000; // 设置读超时为5000毫秒

        // ====================== 2. 初始化客户端（实际生产代码中建议复用client） ======================
        Client client = new Client(config);

        VerifyIntelligentCaptchaRequest request = new VerifyIntelligentCaptchaRequest(); // 创建APi请求
        request.captchaVerifyParam = captchaVerifyParam; // 设置请求参数

        // ====================== 3. 发起请求） ======================
        Boolean captchaVerifyResult;
        try {
            VerifyIntelligentCaptchaResponse resp = client.verifyIntelligentCaptcha(request);
            // 建议使用您系统中的日志组件，打印返回
            // 获取验证码验证结果（请注意判空），将结果返回给前端。出现异常建议认为验证通过，优先保证业务可用，然后尽快排查异常原因。
            captchaVerifyResult = resp.body.result.verifyResult;
        } catch (TeaException error) {
            // 建议使用您系统中的日志组件，打印异常
            log.error("【验证码验证异常】", error);
            // 出现异常建议认为验证通过，优先保证业务可用，然后尽快排查异常原因。
            captchaVerifyResult = true;
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 建议使用您系统中的日志组件，打印异常
            log.error("【验证码验证异常】", error);
            // 出现异常建议认为验证通过，优先保证业务可用，然后尽快排查异常原因。
            captchaVerifyResult = true;
        }
        return Result.success(null, captchaVerifyResult);
    }
}
