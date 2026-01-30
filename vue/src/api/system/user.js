import axios from "axios";

const service = axios.create({
    baseURL: "/api/user",
    timeout: 5000,
});

// 获取用户信息（带token）
export function getUserInfo(token) {
    return service.get("/regular/getUser", {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 修改用户基本信息（不包括用户名、密码、邮箱、角色、创建日期、修改日期、状态、头像）
export function updateUserInfo(token, userData) {
    return service.put("/regular/updateUser", userData, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 修改用户名
export function updateUsername(token, newUsername) {
    return service.put("/regular/updateUsername", { username: newUsername }, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 修改邮箱
export function updateEmail(token, newEmail, verificationCode) {
    return service.put("/regular/updateEmail", { 
        email: newEmail, 
        code: verificationCode 
    }, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 修改密码
export function updatePassword(token, oldPassword, newPassword) {
    return service.put("/regular/updatePassword", { 
        oldPassword: oldPassword, 
        newPassword: newPassword 
    }, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 发送邮箱验证码
export function sendEmailVerificationCode(token, email) {
    return service.post("/regular/sendEmailCode", { email: email }, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 找回密码 - 发送验证码（无需token）
export function sendPasswordResetCode(email) {
    return service.post("/auth/sendEmailCode", { email: email });
}

// 找回密码 - 重置密码（无需token）
export function resetPassword(email, code, newPassword) {
    return service.put("/auth/updatePassword", {
        email: email,
        code: code,
        newPassword: newPassword
    });
}