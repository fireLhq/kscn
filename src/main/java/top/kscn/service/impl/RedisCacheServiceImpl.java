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

    private static final String TEMP_USER_PREFIX = "temp:user:";

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
        redisTemplate.opsForValue().set(
                TEMP_USER_PREFIX + email,
                tempUser,
                5,
                TimeUnit.MINUTES
        );
    }

    @Override
    public void saveEmailCode(String email, String code) {
        redisTemplate.opsForValue().set(
                TEMP_USER_PREFIX + email,
                code,
                5,
                TimeUnit.MINUTES
        );
    }

    /**
     * 获取临时用户信息
     *
     * @param email 邮箱
     * @return 临时用户信息
     */
    @Override
    public Map<String, String> getTempUser(String email) {
        return (Map<String, String>) redisTemplate.opsForValue().get(TEMP_USER_PREFIX + email);
    }

    @Override
    public String getEmailCode(String email) {
        return (String) redisTemplate.opsForValue().get(TEMP_USER_PREFIX + email);
    }

    /**
     * 移除临时用户信息
     *
     * @param email 邮箱
     */
    @Override
    public void removeTempUser(String email) {
        redisTemplate.delete(TEMP_USER_PREFIX + email);
    }

    @Override
    public void removeEmailCode(String email) {
        redisTemplate.delete(TEMP_USER_PREFIX + email);
    }
}