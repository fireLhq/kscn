import axios from "axios";

const service = axios.create({
    baseURL: "/api/projectMember",
    timeout: 5000,
});

// 获取项目成员列表
export function getProjectMemberList(token, params = {}) {
    const config = {
        params: params
    };
    
    // 如果有token，添加认证头
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    
    return service.get("/auth/projectMemberList", config);
}

// 添加项目成员
export function addProjectMember(token, memberData) {
    return service.post("/admin/addProjectMember", memberData, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 更新项目成员
export function updateProjectMember(token, memberData) {
    return service.put("/admin/updateProjectMember", memberData, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 删除项目成员
export function deleteProjectMember(token, memberId) {
    return service.delete(`/admin/deleteProjectMember/${memberId}`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}

// 获取单个项目成员信息
export function getProjectMember(token, memberId) {
    return service.get(`/auth/getProjectMember/${memberId}`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
}
