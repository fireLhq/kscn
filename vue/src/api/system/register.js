import axios from "axios";

const service = axios.create({
    baseURL: "/api/user/auth",
    timeout: 5000,
});

/**
 * 发送注册请求
 * @param {Object} data { email, password }
 * @returns Promise
 */
export function registerRequest(data) {
    return service.post("/registerRequest", data);
}

/**
 * 发送注册验证请求
 * @param {Object} data { email, code }
 * @returns Promise
 */
export function registerVerify(data) {
    return service.post("/registerVerify", data);
}
