import { createApiClient } from "@/utils/request";

const service = createApiClient("/api/member", 5000);

export function getMemberPage(params = {}) {
    return service.get("", { params });
}

export function getMember(memberId) {
    return service.get(`/${memberId}`);
}

export function getMemberAvatarUrl(filename, timestamp) {
    if (!filename) return null;
    if (filename.startsWith("http://") || filename.startsWith("https://")) return filename;
    const query = timestamp ? `?t=${timestamp}` : "";
    return `/api/member/avatar/${filename}${query}`;
}
