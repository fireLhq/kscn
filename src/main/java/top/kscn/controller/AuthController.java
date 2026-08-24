package top.kscn.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import top.kscn.common.Result;
import top.kscn.annotation.OperationLog;
import top.kscn.entity.dto.auth.AuthLoginDTO;
import top.kscn.entity.dto.auth.AuthPasswordCodeDTO;
import top.kscn.entity.dto.auth.AuthPasswordDTO;
import top.kscn.entity.dto.auth.AuthRegisterCodeDTO;
import top.kscn.entity.dto.auth.AuthRegisterDTO;
import top.kscn.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Value("${cookie.name}")
    private String cookieName;
    @Value("${cookie.secure}")
    private boolean cookieSecure;
    @Value("${cookie.max-age}")
    private int cookieMaxAge;

    @PostMapping("/captcha/verify")
    public Result<Boolean> postAuthCaptchaVerify(@RequestBody String captchaVerifyParam) throws Exception {
        return Result.success(null, userService.postAuthCaptchaVerify(captchaVerifyParam));
    }

    @PostMapping("/register/code")
    public Result<?> postAuthRegisterCode(@RequestBody @Valid AuthRegisterCodeDTO dto) {
        userService.postAuthRegisterCode(dto);
        return Result.success("验证码已发送，请查收您的邮箱", null);
    }

    @PostMapping("/register")
    @OperationLog(eventType = "USER", action = "REGISTER", resourceType = "USER")
    public Result<?> postAuthRegister(@RequestBody @Valid AuthRegisterDTO dto) {
        userService.postAuthRegister(dto);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    @OperationLog(eventType = "LOGIN", action = "LOGIN")
    public Result<?> postAuthLogin(@RequestBody @Valid AuthLoginDTO dto, HttpServletRequest request, HttpServletResponse response) {
        String token = userService.postAuthLogin(dto);
        response.addCookie(buildCookie(cookieName, token, cookieMaxAge));
        return Result.success("登录成功", token);
    }

    @PostMapping("/logout")
    @OperationLog(eventType = "USER", action = "LOGOUT", resourceType = "USER")
    public Result<?> postAuthLogout(HttpServletResponse response) {
        response.addCookie(buildCookie(cookieName, null, 0));
        return Result.success("登出成功", null);
    }

    @PostMapping("/password/code")
    public Result<?> postAuthPasswordCode(@RequestBody @Valid AuthPasswordCodeDTO dto) {
        userService.postAuthPasswordCode(dto);
        return Result.success("验证码已发送，请查收您的邮箱", null);
    }

    @PutMapping("/password")
    @OperationLog(eventType = "USER", action = "RESET_PASSWORD", resourceType = "USER")
    public Result<?> putAuthPassword(@RequestBody @Valid AuthPasswordDTO dto) {
        userService.putAuthPassword(dto);
        return Result.success("密码重置成功", null);
    }

    private Cookie buildCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
