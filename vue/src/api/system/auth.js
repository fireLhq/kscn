import { createApiClient } from "@/utils/request";

const service = createApiClient("/api/auth", 5000);

export function postAuthLogin(data) {
    return service.post("/login", data);
}

export function postAuthLogout() {
    return service.post("/logout");
}

export function postAuthRegisterCode(data) {
    return service.post("/register/code", data);
}

export function postAuthRegister(data) {
    return service.post("/register", data);
}

export function postAuthPasswordCode(data) {
    return service.post("/password/code", data);
}

export function putAuthPassword(data) {
    return service.put("/password", data);
}

export async function postAuthCaptchaVerify(captchaVerifyParam) {
    try {
        const res = await fetch("/api/auth/captcha/verify", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: captchaVerifyParam,
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        return { captchaResult: data.data };
    } catch (e) {
        console.error("验证码请求失败:", e);
        return { captchaResult: false };
    }
}
