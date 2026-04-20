package top.kscn.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code; // 响应码
    private String message; // 响应消息
    private T data; // 响应数据

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static Result<?> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
