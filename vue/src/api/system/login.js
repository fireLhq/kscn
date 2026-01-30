import axios from "axios";

const service = axios.create({
    baseURL: "/api/user/auth",
    timeout: 5000,
});

/**
 * 发送登录请求
 * @param {Object} data { email, password }
 * @returns Promise
 */
export function login(data) {
    return service.post("/login", data);
}
