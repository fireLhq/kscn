import { createApiClient } from "@/utils/request";

const service = createApiClient("/api/admin/user", 5000);

export function getAdminUserPage(params = {}) {
    return service.get("", { params });
}

export function getAdminUserStats() {
    return service.get("/stats");
}

export function postAdminUser(data) {
    return service.post("", data);
}

export function putAdminUser(userId, data) {
    return service.put(`/${userId}`, data);
}

export function putAdminUserDefaultAvatar(userId) {
    return service.put(`/${userId}/default-avatar`);
}

export function putAdminUserRecalculateUsedSpace(userId) {
    return service.put(`/${userId}/recalculate-used-space`);
}

export function deleteAdminUser(userId) {
    return service.delete(`/${userId}`);
}
