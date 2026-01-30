package top.kscn.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.kscn.entity.User;
import top.kscn.entity.dto.LoginDTO;
import top.kscn.entity.dto.RegisterDTO;
import top.kscn.entity.dto.UserDTO;
import top.kscn.entity.dto.VerifyDTO;

public interface UserService extends IService<User> {
    void registerRequest(RegisterDTO dto);
    void registerVerify(VerifyDTO dto);
    String login(LoginDTO dto);
    User getUser(Long userId);
    User getUserByUsername(String username);
    User getUserByEmail(String email);
    int updateUser(User user);
    Page<User> userList(UserDTO dto);
    int addUser(User user);
    int deleteUser(Long userId);
}
