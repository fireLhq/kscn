package top.kscn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import top.kscn.service.RedisCacheService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    private static final String REGISTER_PREFIX = "register:";
    private static final String EMAIL_CODE_PREFIX = "email:code:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 保存注册信息
     *
     * @param email    邮箱
     * @param password 密码
     * @param code     验证码
     */
    @Override
    public void saveRegisterInfo(String email, String password, String code) {
        Map<String, String> tempUser = new HashMap<>();
        tempUser.put("password", password);
        tempUser.put("code", code);
        redisTemplate.opsForValue().set(REGISTER_PREFIX + email, tempUser, 5, TimeUnit.MINUTES);
    }

    @Override
    public void saveEmailCode(String email, String code) {
        redisTemplate.opsForValue().set(EMAIL_CODE_PREFIX + email, code, 5, TimeUnit.MINUTES);
    }

    @Override
    public Map<String, String> getTempUser(String email) {
        return (Map<String, String>) redisTemplate.opsForValue().get(REGISTER_PREFIX + email);
    }

    @Override
    public String getEmailCode(String email) {
        return (String) redisTemplate.opsForValue().get(EMAIL_CODE_PREFIX + email);
    }

    @Override
    public void removeTempUser(String email) {
        redisTemplate.delete(REGISTER_PREFIX + email);
    }

    @Override
    public void removeEmailCode(String email) {
        redisTemplate.delete(EMAIL_CODE_PREFIX + email);
    }
}