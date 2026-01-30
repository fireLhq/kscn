import axios from "axios";

const service = axios.create({
    baseURL: "/api/user/admin",
    timeout: 5000,
});

// 获取用户列表
export function getUserList(token, params = {}) {
    return service.get("/userList", {
        headers: {
            Authorization: `Bearer ${token}`,
        },
        params: params
    });
}

// 添加用户
export function addUser(token, userData) {
    return service.post("/addUser", userData, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 删除用户
export function deleteUser(token, userId) {
    return service.delete(`/deleteUser/${userId}`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 更新用户信息
export function updateUser(token, userData) {
    return service.put("/updateUser", userData, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 获取单个用户信息
export function getUser(token, userId) {
    return service.get(`/getUser/${userId}`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}
