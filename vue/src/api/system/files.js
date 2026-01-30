import axios from "axios";

const service = axios.create({
    baseURL: "/api/files",
    timeout: 10000,
});

// 获取文件列表
export function getFileList(path = "/", token = null) {
    const config = {
        params: { path }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/list", config);
}

// 创建文件夹
export function createFolder(path, name, token = null) {
    const config = {
        params: { path, name }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.post("/folder", null, config);
}


// 下载文件
export function downloadFile(path, token = null) {
    const config = {
        params: { path },
        responseType: "blob"
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/download", config);
}

// 删除文件/文件夹
export function deleteFile(path, token = null) {
    const config = {
        params: { path }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.delete("/delete", config);
}

// 批量上传文件
export function uploadBatch(path, files, token = null) {
    const formData = new FormData();
    formData.append("path", path);
    files.forEach(file => {
        formData.append("files", file);
    });
    
    const config = {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    };
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    
    return service.post("/upload/batch", formData, config);
}

// 上传文件夹
export function uploadFolder(path, folder, token = null) {
    const formData = new FormData();
    formData.append("path", path);
    formData.append("folder", folder);
    
    const config = {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    };
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    
    return service.post("/upload/folder", formData, config);
}

// 批量下载
export function downloadBatch(paths, token = null) {
    const config = {
        responseType: "blob"
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.post("/download/batch", paths, config);
}

// 批量删除
export function deleteBatch(paths, token = null) {
    const config = {};
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.delete("/delete/batch", { data: paths, ...config });
}

// 重命名
export function renameFile(path, newName, token = null) {
    const config = {
        params: { path, newName }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.put("/rename", null, config);
}

// 搜索文件
export function searchFiles(keyword, basePath = "/", token = null) {
    const config = {
        params: { keyword, basePath }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/search", config);
}

// 获取文件树
export function getFileTree(basePath = "/", token = null) {
    const config = {
        params: { basePath }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/tree", config);
}

// 下载文件夹
export function downloadFolder(path, token = null) {
    const config = {
        params: { path },
        responseType: "blob"
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/download/folder", config);
}