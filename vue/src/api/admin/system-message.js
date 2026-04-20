import { createApiClient } from "@/utils/request";

const service = createApiClient("/api/admin/system-message", 5000);

export function getAdminSystemMessagePage(params = {}) {
    return service.get("", { params });
}

export function postSystemMessage(data) {
    return service.post("", data);
}

export function putSystemMessage(systemMessageId, data) {
    return service.put(`/${systemMessageId}`, data);
}

export function deleteSystemMessage(systemMessageId) {
    return service.delete(`/${systemMessageId}`);
}

