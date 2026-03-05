import SparkMD5 from 'spark-md5';
import axios from 'axios';

/**
 * 分片上传工具类
 * 支持大文件上传、断点续传、秒传、进度监控、并发控制
 */
class ChunkedUploader {
    constructor(file, options = {}) {
        this.file = file;
        this.fileName = file.name;
        this.fileSize = file.size;
        this.chunkSize = options.chunkSize || 1024 * 1024; // 默认1MB
        this.path = options.path || '/';
        this.token = options.token || null;
        this.onProgress = options.onProgress || (() => {});
        this.onSuccess = options.onSuccess || (() => {});
        this.onError = options.onError || (() => {});
        this.concurrentLimit = options.concurrentLimit || 3; // 单文件并发限制
        
        this.uploadId = null;
        this.fileMd5 = null;
        this.totalChunks = 0;
        this.uploadedChunks = new Set();
        this.isUploading = false;
        this.isPaused = false;
        this.isCancelled = false;
        this.uploadingChunks = new Set(); // 正在上传的分片
    }

    /**
     * 计算文件MD5
     */
    async calculateMD5() {
        return new Promise((resolve, reject) => {
            const spark = new SparkMD5.ArrayBuffer();
            const fileReader = new FileReader();
            const chunks = Math.ceil(this.fileSize / this.chunkSize);
            let currentChunk = 0;

            fileReader.onload = (e) => {
                spark.append(e.target.result);
                currentChunk++;

                if (currentChunk < chunks) {
                    loadNext();
                } else {
                    resolve(spark.end());
                }
            };

            fileReader.onerror = () => {
                reject(new Error('文件读取失败'));
            };

            const loadNext = () => {
                const start = currentChunk * this.chunkSize;
                const end = Math.min(start + this.chunkSize, this.fileSize);
                fileReader.readAsArrayBuffer(this.file.slice(start, end));
            };

            loadNext();
        });
    }

    /**
     * 校验上传（秒传+断点续传）
     */
    async checkUpload() {
        try {
            const config = {
                params: {
                    fileName: this.fileName,
                    fileSize: this.fileSize,
                    fileMd5: this.fileMd5,
                    path: this.path
                }
            };
            
            if (this.token) {
                config.headers = {
                    Authorization: `Bearer ${this.token}`
                };
            }

            const response = await axios.post('/api/files/upload/check', null, config);
            
            if (response.data.code === 200) {
                const data = response.data.data;
                
                // 秒传
                if (data.exists) {
                    this.onSuccess('秒传成功');
                    return { canSkip: true, message: data.message };
                }
                
                // 断点续传
                if (data.uploadedChunks && data.uploadedChunks.length > 0) {
                    data.uploadedChunks.forEach(chunk => this.uploadedChunks.add(chunk));
                    return { canSkip: false, uploadedChunks: data.uploadedChunks };
                }
                
                return { canSkip: false, uploadedChunks: [] };
            } else {
                throw new Error(response.data.message || '校验失败');
            }
        } catch (error) {
            console.error('校验上传失败:', error);
            return { canSkip: false, uploadedChunks: [] };
        }
    }

    /**
     * 初始化上传
     */
    async init() {
        try {
            // 计算文件MD5
            if (!this.fileMd5) {
                this.fileMd5 = await this.calculateMD5();
            }
            
            // 校验上传（秒传+断点续传）
            const checkResult = await this.checkUpload();
            if (checkResult.canSkip) {
                return { skip: true };
            }
            
            // 初始化分片上传
            const config = {
                params: {
                    fileName: this.fileName,
                    fileSize: this.fileSize,
                    fileMd5: this.fileMd5,
                    path: this.path
                }
            };
            
            if (this.token) {
                config.headers = {
                    Authorization: `Bearer ${this.token}`
                };
            }

            const response = await axios.post('/api/files/upload/init', null, config);
            
            if (response.data.code === 200) {
                this.uploadId = response.data.data.uploadId;
                this.totalChunks = response.data.data.totalChunks;
                this.chunkSize = response.data.data.chunkSize;
                return { skip: false };
            } else {
                throw new Error(response.data.message || '初始化上传失败');
            }
        } catch (error) {
            this.onError(error);
            throw error;
        }
    }

    /**
     * 上传单个分片（带重试机制）
     */
    async uploadChunk(chunkNumber, retryCount = 0) {
        const maxRetries = 3;
        
        try {
            const start = chunkNumber * this.chunkSize;
            const end = Math.min(start + this.chunkSize, this.fileSize);
            const chunk = this.file.slice(start, end);

            const formData = new FormData();
            formData.append('uploadId', this.uploadId);
            formData.append('chunkNumber', chunkNumber);
            formData.append('chunk', chunk);

            const config = {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            };

            if (this.token) {
                config.headers.Authorization = `Bearer ${this.token}`;
            }

            const response = await axios.post('/api/files/upload/chunk', formData, config);
            
            if (response.data.code === 200) {
                this.uploadedChunks.add(chunkNumber);
                this.uploadingChunks.delete(chunkNumber);
                
                // 保存上传进度到localStorage（用于刷新恢复）
                this.saveProgress();
                
                this.onProgress({
                    uploadedChunks: this.uploadedChunks.size,
                    totalChunks: this.totalChunks,
                    progress: (this.uploadedChunks.size / this.totalChunks * 100).toFixed(2),
                    fileName: this.fileName,
                    uploadedSize: this.uploadedChunks.size * this.chunkSize,
                    totalSize: this.fileSize
                });
            } else {
                throw new Error(response.data.message || '分片上传失败');
            }
        } catch (error) {
            this.uploadingChunks.delete(chunkNumber);
            
            // 重试机制
            if (retryCount < maxRetries) {
                console.warn(`分片 ${chunkNumber} 上传失败，正在重试 (${retryCount + 1}/${maxRetries})...`);
                await new Promise(resolve => setTimeout(resolve, 1000 * (retryCount + 1))); // 指数退避
                return this.uploadChunk(chunkNumber, retryCount + 1);
            } else {
                throw new Error(`分片 ${chunkNumber} 上传失败，已重试 ${maxRetries} 次: ${error.message}`);
            }
        }
    }

    /**
     * 开始上传（支持并发控制）
     */
    async start() {
        if (this.isUploading) {
            return;
        }

        try {
            this.isUploading = true;
            this.isPaused = false;
            this.isCancelled = false;

            // 初始化上传
            if (!this.uploadId) {
                const initResult = await this.init();
                if (initResult.skip) {
                    return; // 秒传成功
                }
            }

            // 获取待上传的分片列表
            const chunksToUpload = [];
            for (let i = 0; i < this.totalChunks; i++) {
                if (!this.uploadedChunks.has(i)) {
                    chunksToUpload.push(i);
                }
            }

            // 并发上传分片
            await this.uploadChunksConcurrently(chunksToUpload);

            // 完成上传
            if (!this.isCancelled && !this.isPaused) {
                await this.complete();
            }
        } catch (error) {
            this.isUploading = false;
            this.onError(error);
            throw error;
        }
    }

    /**
     * 并发上传分片
     */
    async uploadChunksConcurrently(chunks) {
        const uploadPromises = [];
        let index = 0;

        const uploadNext = async () => {
            while (index < chunks.length) {
                if (this.isCancelled || this.isPaused) {
                    return;
                }

                const chunkNumber = chunks[index++];
                
                // 跳过已上传或正在上传的分片
                if (this.uploadedChunks.has(chunkNumber) || this.uploadingChunks.has(chunkNumber)) {
                    continue;
                }

                this.uploadingChunks.add(chunkNumber);

                try {
                    await this.uploadChunk(chunkNumber);
                } catch (error) {
                    this.uploadingChunks.delete(chunkNumber);
                    throw error;
                }
            }
        };

        // 创建并发上传任务
        for (let i = 0; i < this.concurrentLimit; i++) {
            uploadPromises.push(uploadNext());
        }

        await Promise.all(uploadPromises);
    }

    /**
     * 暂停上传
     */
    pause() {
        this.isPaused = true;
    }

    /**
     * 恢复上传
     */
    async resume() {
        if (!this.isPaused) {
            return;
        }
        await this.start();
    }

    /**
     * 取消上传
     */
    async cancel() {
        this.isCancelled = true;
        this.isPaused = false;
        this.isUploading = false;

        if (this.uploadId) {
            try {
                const config = {
                    params: { uploadId: this.uploadId }
                };

                if (this.token) {
                    config.headers = {
                        Authorization: `Bearer ${this.token}`
                    };
                }

                await axios.post('/api/files/upload/cancel', null, config);
            } catch (error) {
                console.error('取消上传失败:', error);
            }
        }
    }

    /**
     * 完成上传
     */
    async complete() {
        try {
            const config = {
                params: { uploadId: this.uploadId }
            };

            if (this.token) {
                config.headers = {
                    Authorization: `Bearer ${this.token}`
                };
            }

            const response = await axios.post('/api/files/upload/complete', null, config);
            
            if (response.data.code === 200) {
                this.isUploading = false;
                // 清除保存的进度
                this.clearProgress();
                this.onSuccess(response.data.data);
            } else {
                throw new Error(response.data.message || '完成上传失败');
            }
        } catch (error) {
            this.isUploading = false;
            this.onError(error);
            throw error;
        }
    }

    /**
     * 获取上传进度
     */
    async getProgress() {
        if (!this.uploadId) {
            return null;
        }

        try {
            const config = {
                params: { uploadId: this.uploadId }
            };

            if (this.token) {
                config.headers = {
                    Authorization: `Bearer ${this.token}`
                };
            }

            const response = await axios.get('/api/files/upload/progress', config);
            
            if (response.data.code === 200) {
                return response.data.data;
            }
        } catch (error) {
            console.error('获取进度失败:', error);
        }
        return null;
    }

    /**
     * 保存上传进度到localStorage（用于刷新恢复）
     */
    saveProgress() {
        const progressKey = `upload_progress_${this.fileMd5}`;
        const progressData = {
            uploadId: this.uploadId,
            fileName: this.fileName,
            fileSize: this.fileSize,
            fileMd5: this.fileMd5,
            path: this.path,
            totalChunks: this.totalChunks,
            uploadedChunks: Array.from(this.uploadedChunks),
            timestamp: Date.now()
        };
        localStorage.setItem(progressKey, JSON.stringify(progressData));
    }

    /**
     * 从localStorage恢复上传进度
     */
    static loadProgress(fileMd5) {
        const progressKey = `upload_progress_${fileMd5}`;
        const progressData = localStorage.getItem(progressKey);
        
        if (!progressData) {
            return null;
        }
        
        try {
            const data = JSON.parse(progressData);
            
            // 检查是否过期（24小时）
            if (Date.now() - data.timestamp > 24 * 60 * 60 * 1000) {
                localStorage.removeItem(progressKey);
                return null;
            }
            
            return data;
        } catch (error) {
            console.error('解析进度数据失败:', error);
            localStorage.removeItem(progressKey);
            return null;
        }
    }

    /**
     * 清除保存的进度
     */
    clearProgress() {
        const progressKey = `upload_progress_${this.fileMd5}`;
        localStorage.removeItem(progressKey);
    }

    /**
     * 从已保存的进度恢复上传
     */
    static async resumeFromProgress(file, progressData, options = {}) {
        const uploader = new ChunkedUploader(file, {
            ...options,
            path: progressData.path
        });
        
        // 恢复上传状态
        uploader.uploadId = progressData.uploadId;
        uploader.fileMd5 = progressData.fileMd5;
        uploader.totalChunks = progressData.totalChunks;
        uploader.uploadedChunks = new Set(progressData.uploadedChunks);
        
        // 继续上传
        await uploader.start();
        
        return uploader;
    }
}

export default ChunkedUploader;

