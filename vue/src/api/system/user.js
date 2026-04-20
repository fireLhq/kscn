import { createApiClient } from "@/utils/request";

const service = createApiClient("/api/user", 5000);

export function getUserProfile() {
    return service.get("/profile");
}

export function putUserProfile(data) {
    return service.put("/profile", data);
}

export function putUserProfileUsername(data) {
    return service.put("/profile/username", data);
}

export function postUserProfileEmailCode(data) {
    return service.post("/profile/email/code", data);
}

export function putUserProfileEmail(data) {
    return service.put("/profile/email", data);
}

export function putUserProfilePassword(data) {
    return service.put("/profile/password", data);
}

export function postUserProfileAvatar(formData) {
    return service.post("/profile/avatar", formData, {
        headers: { "Content-Type": "multipart/form-data" },
    });
}

export function getUserAvatarUrl(filename, timestamp) {
    if (!filename || filename === "default_avatar.png") return null;
    if (filename.startsWith("http://") || filename.startsWith("https://")) return filename;
    const query = timestamp ? `?t=${timestamp}` : "";
    return `/api/user/avatar/${filename}${query}`;
}

export function getRegisteredUserCount() {
    return service.get("/count");
}
