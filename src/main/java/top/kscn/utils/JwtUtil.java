package top.kscn.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String secret = "DefaultJwtSecretKeyForDemoOnlyDontUseInProduction";

    private static final Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    private static final long expiration = 36000000L;

    // 创建JWT
    public static String createToken(Long userId, String username, String email, Integer role) {
        return Jwts.builder() // 创建JWT的Builder对象
                .setId(String.valueOf(userId)) // 设置JWT的唯一标识
                .claim("username", username) // 添加自定义的Claim
                .claim("email", email) // 添加自定义的Claim
                .claim("role", role) // 添加自定义的Claim
                .setIssuedAt(new Date()) // 设置JWT的签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 设置JWT的过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 使用HS256算法和秘钥进行签名
                .compact(); // 生成JWT字符串
    }

    // 解析JWT
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // 设置签名秘钥
                .build() // 创建JwtParser对象
                .parseClaimsJws(token) // 解析JWT字符串
                .getBody(); // 获取Claims对象
    }
}
