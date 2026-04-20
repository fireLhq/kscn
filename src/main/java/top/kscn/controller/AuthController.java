package top.kscn.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import top.kscn.common.Result;
import top.kscn.entity.dto.auth.AuthLoginDTO;
import top.kscn.entity.dto.auth.AuthPasswordCodeDTO;
import top.kscn.entity.dto.auth.AuthPasswordDTO;
import top.kscn.entity.dto.auth.AuthRegisterCodeDTO;
import top.kscn.entity.dto.auth.AuthRegisterDTO;
import top.kscn.exception.CustomException;
import top.kscn.service.LoginLogService;
import top.kscn.service.UserService;
import top.kscn.utils.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private LoginLogService loginLogService;

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
    public Result<?> postAuthRegister(@RequestBody @Valid AuthRegisterDTO dto) {
        userService.postAuthRegister(dto);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    public Result<?> postAuthLogin(@RequestBody @Valid AuthLoginDTO dto, HttpServletRequest request, HttpServletResponse response) {
        try {
            String token = userService.postAuthLogin(dto);
            response.addCookie(buildCookie(cookieName, token, cookieMaxAge));
            Claims claims = JwtUtil.parseToken(token);
            Long userId = claims.getId() == null ? null : Long.valueOf(claims.getId());
            loginLogService.record(userId, 1, "登录成功", request);
            return Result.success("登录成功", token);
        } catch (CustomException e) {
            loginLogService.record(null, 0, "登录失败: " + e.getMessage() + "，账号: " + dto.getAccount(), request);
            throw e;
        } catch (Exception e) {
            loginLogService.record(null, 0, "登录失败: 系统异常，账号: " + dto.getAccount(), request);
            throw e;
        }
    }

    @PostMapping("/logout")
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
