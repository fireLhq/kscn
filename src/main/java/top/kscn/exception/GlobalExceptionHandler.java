package top.kscn.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import top.kscn.common.Result;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Result<?>> handleCustomException(CustomException e) {
        log.warn("[业务] {}", e.getMessage());
        return ResponseEntity
                .status(e.getCode())
                .body(Result.error(e.getCode(), e.getMessage()));
    }

    /**
     * 参数异常（统一处理）
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<Result<?>> handleParamException(Exception e) {
        log.warn("[参数] {}", e.getMessage());
        return ResponseEntity
                .status(400)
                .body(Result.error(400, "参数错误，请检查输入"));
    }

    /**
     * 登录认证异常
     */
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<Result<?>> handleAuthException(Exception e) {
        log.warn("[认证] {}", e.getMessage());
        return ResponseEntity
                .status(401)
                .body(Result.error(401, "账号或密码错误"));
    }

    /**
     * 访问权限异常
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Result<?>> handleAuthDenied(AuthorizationDeniedException e) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            log.warn("[认证] 未登录，{}", e.getMessage());
            return ResponseEntity
                    .status(401)
                    .body(Result.error(401, "请先登录"));
        }
        log.warn("[认证] 无权限访问，{}", e.getMessage());
        return ResponseEntity
                .status(403)
                .body(Result.error(403, "无权限访问"));
    }

    /**
     * 文件过大
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<?>> handleUploadException(MaxUploadSizeExceededException e) {
        log.warn("[上传] 文件过大", e);
        return ResponseEntity
                .status(413)
                .body(Result.error(413, "文件太大，无法上传"));
    }

    /**
     * 数据库异常
     */
    @ExceptionHandler({DataAccessException.class, SQLException.class})
    public ResponseEntity<Result<?>> handleDbException(Exception e) {
        log.error("[数据库] 操作失败", e);
        return ResponseEntity
                .status(500)
                .body(Result.error(500, "系统繁忙，请稍后重试"));
    }

    /**
     * 网络异常
     */
    @ExceptionHandler({ConnectException.class, TimeoutException.class})
    public ResponseEntity<Result<?>> handleNetworkException(Exception e) {
        log.error("[网络] 请求失败", e);
        return ResponseEntity
                .status(503)
                .body(Result.error(503, "网络异常，请稍后重试"));
    }

    /**
     * 运行时异常（兜底一层）
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<?>> handleRuntimeException(RuntimeException e) {
        log.error("[运行时] 未知错误", e);
        return ResponseEntity
                .status(500)
                .body(Result.error(500, "系统出错，请联系管理员"));
    }

    /**
     * 兜底异常（最后一道防线）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e) {
        log.error("[系统] 未知异常", e);
        return ResponseEntity
                .status(500)
                .body(Result.error(500, "系统异常，请稍后重试"));
    }
}