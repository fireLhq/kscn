import axios from "axios";

const service = axios.create({
    baseURL: "/api/public-files",
    timeout: 300000, // 5分钟
});

// ==================== 文件上传 ====================

/**
 * 检查文件是否可以秒传
 */
export function checkInstantUpload(fileMd5, fileSize, token = null) {
    const config = {
        params: { fileMd5, fileSize }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.post("/upload/check", null, config);
}

/**
 * 上传公共文件（小文件直接上传）
 */
export function uploadFile(parentId, fileMd5, file, category = null, token = null) {
    const formData = new FormData();
    if (parentId !== null && parentId !== undefined) {
        formData.append("parentId", parentId);
    }
    formData.append("fileMd5", fileMd5);
    formData.append("file", file);
    if (category) {
        formData.append("category", category);
    }
    
    const config = {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    };
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    
    return service.post("/upload", formData, config);
}

/**
 * 秒传文件
 */
export function instantUpload(parentId, fileName, storageId, category = null, token = null) {
    const config = {
        params: { fileName, storageId }
    };
    if (parentId !== null && parentId !== undefined) {
        config.params.parentId = parentId;
    }
    if (category) {
        config.params.category = category;
    }
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.post("/upload/instant", null, config);
}

/**
 * 初始化分片上传
 */
export function initChunkedUpload(fileName, fileSize, fileMd5, token = null) {
    const config = {
        params: { fileName, fileSize, fileMd5 }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.post("/upload/init", null, config);
}

/**
 * 上传分片
 */
export function uploadChunk(uploadId, chunkNumber, chunk, token = null) {
    const formData = new FormData();
    formData.append("uploadId", uploadId);
    formData.append("chunkNumber", chunkNumber);
    formData.append("chunk", chunk);
    
    const config = {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    };
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    
    return service.post("/upload/chunk", formData, config);
}

/**
 * 完成分片上传
 */
export function completeChunkedUpload(uploadId, parentId, fileName, category = null, token = null) {
    const config = {
        params: { uploadId, fileName }
    };
    if (parentId !== null && parentId !== undefined) {
        config.params.parentId = parentId;
    }
    if (category) {
        config.params.category = category;
    }
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.post("/upload/complete", null, config);
}

/**
 * 取消分片上传
 */
export function cancelChunkedUpload(uploadId, token = null) {
    const config = {
        params: { uploadId }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.post("/upload/cancel", null, config);
}

/**
 * 获取上传进度
 */
export function getUploadProgress(uploadId, token = null) {
    const config = {
        params: { uploadId }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/upload/progress", config);
}

// ==================== 文件夹操作 ====================

/**
 * 创建文件夹
 */
export function createFolder(parentId, folderName, token = null) {
    const config = {
        params: { folderName }
    };
    if (parentId !== null && parentId !== undefined) {
        config.params.parentId = parentId;
    }
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.post("/folder", null, config);
}

/**
 * 获取文件列表（指定目录）
 */
export function listFiles(parentId = null, token = null) {
    const config = {
        params: {}
    };
    if (parentId !== null && parentId !== undefined) {
        config.params.parentId = parentId;
    }
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/list", config);
}

/**
 * 获取目录树结构
 */
export function getDirectoryTree(token = null) {
    const config = {};
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/tree", config);
}

/**
 * 获取所有文件
 */
export function listAllFiles(token = null) {
    const config = {};
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/list/all", config);
}

/**
 * 根据分类获取公共文件
 */
export function listFilesByCategory(category, token = null) {
    const config = {
        params: { category }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/list/category", config);
}

/**
 * 搜索公共文件
 */
export function searchFiles(keyword, token = null) {
    const config = {
        params: { keyword }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/search", config);
}

/**
 * 根据ID获取公共文件
 */
export function getFileById(fileId, token = null) {
    const config = {};
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get(`/${fileId}`, config);
}

// ==================== 文件操作 ====================

/**
 * 删除文件（移入回收站）
 */
export function deleteFile(fileId, token = null) {
    const config = {
        params: { fileId }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.delete("/delete", config);
}

/**
 * 彻底删除文件
 */
export function permanentDeleteFile(fileId, token = null) {
    const config = {
        params: { fileId }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.delete("/delete/permanent", config);
}

/**
 * 重命名文件
 */
export function renameFile(fileId, newName, token = null) {
    const config = {
        params: { fileId, newName }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.put("/rename", null, config);
}

/**
 * 移动文件
 */
export function moveFile(fileId, targetParentId, token = null) {
    const config = {
        params: { fileId }
    };
    if (targetParentId !== null && targetParentId !== undefined) {
        config.params.targetParentId = targetParentId;
    }
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.put("/move", null, config);
}

// ==================== 文件下载 ====================

/**
 * 下载公共文件（浏览器原生下载）
 */
export function downloadFile(fileId, token = null) {
    const params = new URLSearchParams({ fileId });
    const url = `/api/public-files/download?${params.toString()}`;
    
    // 创建隐藏的a标签触发下载
    const link = document.createElement('a');
    link.href = url;
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * 下载文件夹
 */
export function downloadFolder(folderId, token = null) {
    const url = `/api/public-files/download/folder/${folderId}`;
    
    // 创建隐藏的a标签触发下载
    const link = document.createElement('a');
    link.href = url;
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * 创建批量下载任务
 */
export function createBatchDownloadTask(fileIds, token = null) {
    const config = {
        headers: {}
    };
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return service.post("/download/batch", fileIds, config);
}

/**
 * 下载批量下载任务的ZIP文件
 */
export function downloadBatchTask(taskId, token = null) {
    const url = `/api/public-files/download/batch/${taskId}`;
    
    // 创建隐藏的a标签触发下载
    const link = document.createElement('a');
    link.href = url;
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// ==================== 回收站 ====================

/**
 * 获取回收站文件列表
 */
export function getRecycleBin(token = null) {
    const config = {};
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/recycle", config);
}

/**
 * 恢复文件
 */
export function restoreFile(fileId, token = null) {
    const config = {
        params: { fileId }
    };
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.put("/recycle/restore", null, config);
}

/**
 * 清空回收站
 */
export function emptyRecycleBin(token = null) {
    const config = {};
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.delete("/recycle/empty", config);
}

// ==================== 统计信息 ====================

/**
 * 获取公共文件库统计信息
 */
export function getStatistics(token = null) {
    const config = {};
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    return service.get("/statistics", config);
}

export default {
    checkInstantUpload,
    uploadFile,
    instantUpload,
    initChunkedUpload,
    uploadChunk,
    completeChunkedUpload,
    cancelChunkedUpload,
    getUploadProgress,
    createFolder,
    listFiles,
    getDirectoryTree,
    listAllFiles,
    listFilesByCategory,
    searchFiles,
    getFileById,
    deleteFile,
    permanentDeleteFile,
    renameFile,
    moveFile,
    downloadFile,
    downloadFolder,
    createBatchDownloadTask,
    downloadBatchTask,
    getRecycleBin,
    restoreFile,
    emptyRecycleBin,
    getStatistics
};


