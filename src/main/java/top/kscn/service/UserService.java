package top.kscn.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.User;
import top.kscn.entity.dto.auth.AuthLoginDTO;
import top.kscn.entity.dto.auth.AuthPasswordCodeDTO;
import top.kscn.entity.dto.auth.AuthPasswordDTO;
import top.kscn.entity.dto.auth.AuthRegisterCodeDTO;
import top.kscn.entity.dto.auth.AuthRegisterDTO;
import top.kscn.entity.dto.user.UserDTO;
import top.kscn.entity.dto.user.UserProfileDTO;
import top.kscn.entity.dto.user.UserProfileEmailCodeDTO;
import top.kscn.entity.dto.user.UserProfileEmailDTO;
import top.kscn.entity.dto.user.UserProfilePasswordDTO;
import top.kscn.entity.dto.user.UserProfileUsernameDTO;

import java.util.Map;

public interface UserService extends IService<User> {

    Boolean postAuthCaptchaVerify(String captchaVerifyParam) throws Exception;

    void postAuthRegisterCode(AuthRegisterCodeDTO dto);

    void postAuthRegister(AuthRegisterDTO dto);

    String postAuthLogin(AuthLoginDTO dto);

    void postAuthPasswordCode(AuthPasswordCodeDTO dto);

    void putAuthPassword(AuthPasswordDTO dto);

    User getUserProfile(Long userId);

    void putUserProfile(Long userId, UserProfileDTO dto);

    void putUserProfileUsername(Long userId, UserProfileUsernameDTO dto);

    void postUserProfileEmailCode(UserProfileEmailCodeDTO dto);

    void putUserProfileEmail(Long userId, UserProfileEmailDTO dto);

    void putUserProfilePassword(Long userId, UserProfilePasswordDTO dto);

    String postUserProfileAvatar(Long userId, MultipartFile file) throws Exception;

    void getUserAvatar(String filename, HttpServletResponse response);

    Page<User> getAdminUserPage(UserDTO dto, Long page, Long size);

    Map<String, Object> getAdminUserStats();

    long countRegisteredUsers();

    void postAdminUser(UserDTO dto);

    void putAdminUser(Long userId, UserDTO dto, Long currentUserId);

    void putAdminUserDefaultAvatar(Long userId);

    void putAdminUserRecalculateUsedSpace(Long userId);

    void deleteAdminUser(Long userId);
}
