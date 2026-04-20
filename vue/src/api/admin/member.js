import { createApiClient } from "@/utils/request";

const service = createApiClient("/api/admin/member", 5000);

export function getAdminMemberPage(params = {}) {
    return service.get("", { params });
}

export function postMember(data) {
    return service.post("", data);
}

export function putMember(memberId, data) {
    return service.put(`/${memberId}`, data);
}

export function deleteMember(memberId) {
    return service.delete(`/${memberId}`);
}

export function postMemberAvatar(memberId, formData) {
    return service.post(`/${memberId}/avatar`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
    });
}

