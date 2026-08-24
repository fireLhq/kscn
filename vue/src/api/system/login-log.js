import { createApiClient } from "@/utils/request";

const service = createApiClient("/api/login-log", 5000);

export function getLoginStats() {
    return service.get("/stats");
}

export function getVisitorCount() {
    return service.get("/visitor-count");
}

export function getRecentLogins(params = {}) {
    return service.get("/recent-logins", { params });
}

export function getAdminLogs(params = {}) {
    return service.get("/admin-logs", { params });
}

export function getAdminLogDetail(logId) {
    return service.get(`/admin-logs/${logId}`);
}

