package top.kscn.service;

import java.util.Map;

public interface RedisCacheService {
    void saveRegisterInfo(String email, String password, String code);
    void saveEmailCode(String email, String code);
    Map<String, String> getTempUser(String email);
    String getEmailCode(String email);
    void removeTempUser(String email);
    void removeEmailCode(String email);
}