// utils/isTokenExpired.js
export function isTokenExpired(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1])); // 解析 Base64 payload
        const exp = payload.exp;
        if (!exp) return true;
        const currentTime = Math.floor(Date.now() / 1000); // 当前时间（单位：秒）
        return exp < currentTime;
    } catch (e) {
        return true; // 解码失败也视为已过期
    }
}
