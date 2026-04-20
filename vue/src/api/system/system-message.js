import { createApiClient } from "@/utils/request";

const service = createApiClient("/api/system-message", 5000);

export function getHomeSystemMessageList(params = {}) {
    return service.get("/home/list", { params });
}

export function getActivePopupMessages(params = {}) {
    return service.get("/popup/active", { params });
}

export function dismissPopupMessage(systemMessageId, data) {
    return service.post(`/popup/${systemMessageId}/dismiss`, data);
}

