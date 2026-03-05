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

// ==================== 头像管理 ====================

/**
 * 上传头像
 */
export function uploadAvatar(token, formData) {
    return service.post("/regular/uploadAvatar", formData, {
        headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data",
        },
    });
}

/**
 * 获取头像URL
 * @param {string} filename - 头像文件名
 * @returns {string} 头像URL
 */
export function getAvatarUrl(filename) {
    if (!filename || filename === "default_avatar.png") {
        // 返回默认头像占位图
        return "https://via.placeholder.com/40x40/409EFF/FFFFFF?text=U";
    }
    return `/api/user/auth/avatar/${filename}`;
}

export default {
    getUserInfo,
    updateUserInfo,
    updateUsername,
    updateEmail,
    updatePassword,
    sendEmailVerificationCode,
    sendPasswordResetCode,
    resetPassword,
    uploadAvatar,
    getAvatarUrl
};