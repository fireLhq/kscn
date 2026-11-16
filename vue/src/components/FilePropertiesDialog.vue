<template>
    <el-dialog :title="details && details.folder ? '文件夹属性' : '文件属性'" :visible.sync="visible" width="640px" custom-class="file-properties-dialog"
        :close-on-click-modal="false" append-to-body @closed="close">
        <div v-loading="loading">
            <el-alert v-if="error" :title="error" type="error" :closable="false" class="property-notice" />
            <el-descriptions v-if="details" :column="1" border size="small">
                <el-descriptions-item label="名称"><span class="property-value">{{ details.fileName }}</span></el-descriptions-item>
                <el-descriptions-item label="类型">{{ details.folder ? '文件夹' : details.fileType ? details.fileType.toUpperCase() + ' 文件' : '无扩展名文件' }}</el-descriptions-item>
                <el-descriptions-item label="大小">{{ details.remote && !details.sizeKnown ? '未知（远程服务器未提供）' : formatSize(details.fileSize) + '（' + Number(details.fileSize).toLocaleString('zh-CN') + ' 字节）' }}</el-descriptions-item>
                <el-descriptions-item v-if="!details.folder" label="存储方式">{{ details.remote ? '远程链接（不占云盘容量，链接可能失效）' : '本地服务器文件' }}</el-descriptions-item>
                <el-descriptions-item v-if="details.folder" label="包含">{{ details.fileCount }} 个文件，{{ details.folderCount }} 个子文件夹（不含回收站）</el-descriptions-item>
                <el-descriptions-item label="所在目录"><span class="property-value">{{ details.parentPath }}</span></el-descriptions-item>
                <el-descriptions-item label="完整路径"><span class="property-value">{{ details.fullPath }}</span></el-descriptions-item>
                <el-descriptions-item label="上传/创建时间">{{ formatTime(details.createTime) }}</el-descriptions-item>
                <el-descriptions-item label="修改时间">{{ formatTime(details.updateTime) }}</el-descriptions-item>
                <el-descriptions-item v-if="details.category" label="分类">{{ details.category }}</el-descriptions-item>
                <el-descriptions-item v-if="details.downloadCount != null" label="下载次数">{{ details.downloadCount }}</el-descriptions-item>
            </el-descriptions>
            <el-alert v-if="details && details.remote" title="此文件仅保存远程链接，不占云盘容量；暂不支持哈希校验及ZIP打包，链接失效后无法读取。" type="info" :closable="false" class="property-notice" />
            <div v-if="details && !details.folder && !details.remote" class="checksum-section">
                <el-button v-if="!checksumVisible" type="primary" plain icon="el-icon-finished" @click="checksumVisible = true">校验</el-button>
                <template v-else>
                    <div class="checksum-controls">
                        <span>校验算法</span>
                        <el-select v-model="algorithm" size="small" :disabled="hashing" @change="clearHash">
                            <el-option v-for="item in algorithms" :key="item" :label="item" :value="item" />
                        </el-select>
                        <el-button type="primary" size="small" :loading="hashing" @click="calculate">{{ hashing ? '计算中' : '计算校验值' }}</el-button>
                    </div>
                    <p class="checksum-hint">读取服务器文件内容计算，较大文件可能需要一些时间。</p>
                    <el-alert v-if="hashError" :title="hashError" type="error" :closable="false" class="property-notice" />
                    <div v-if="hashResult">
                        <p>{{ hashResult.algorithm }} 校验值</p>
                        <el-input type="textarea" :rows="3" :value="hashResult.value" readonly aria-label="文件校验值" />
                        <div class="checksum-result-footer">
                            <span>校验完成：{{ formatTime(hashResult.calculatedAt) }}</span>
                            <el-button size="small" icon="el-icon-document-copy" @click="copyHash">复制校验值</el-button>
                        </div>
                    </div>
                </template>
            </div>
        </div>
        <span slot="footer"><el-button @click="visible = false">关闭</el-button></span>
    </el-dialog>
</template>

<script>
import { createApiClient } from '@/utils/request';

export default {
    name: 'FilePropertiesDialog',
    props: { publicSpace: Boolean },
    data() {
        return { visible: false, loading: false, details: null, error: '', checksumVisible: false,
            algorithm: 'SHA-256', algorithms: ['MD5', 'SHA-1', 'SHA-256', 'SHA-384', 'SHA-512'],
            hashing: false, hashResult: null, hashError: '', requestVersion: 0, controller: null };
    },
    computed: { api() { return createApiClient(this.publicSpace ? '/api/public-files' : '/api/user-files', 600000); } },
    watch: { visible(value) { if (!value) this.cancel(); } },
    beforeDestroy() { this.cancel(); },
    methods: {
        checked(response) {
            if (response.data.code !== 200) throw new Error(response.data.message || '请求失败');
            return response.data.data;
        },
        cancel() { this.requestVersion++; if (this.controller) this.controller.abort(); this.controller = null; this.hashing = false; this.loading = false; },
        close() { this.cancel(); this.details = null; this.hashResult = null; },
        async open(file) {
            this.cancel();
            const version = this.requestVersion;
            this.controller = new AbortController();
            this.details = null; this.error = ''; this.hashError = ''; this.hashResult = null;
            this.checksumVisible = false; this.algorithm = 'SHA-256'; this.visible = true; this.loading = true;
            try {
                const details = this.checked(await this.api.get(`/${file.id}/properties`, { signal: this.controller.signal }));
                if (version === this.requestVersion) this.details = details;
            } catch (error) { if (version === this.requestVersion) this.error = error.response?.data?.message || error.message || '无法获取文件属性'; }
            finally { if (version === this.requestVersion) this.loading = false; }
        },
        clearHash() { this.hashResult = null; this.hashError = ''; },
        async calculate() {
            if (this.hashing || !this.details || this.details.folder) return;
            const version = this.requestVersion;
            const algorithm = this.algorithm;
            this.hashing = true; this.clearHash();
            try {
                const result = this.checked(await this.api.get(`/${this.details.fileId}/hash`, { params: { algorithm }, signal: this.controller.signal }));
                if (version === this.requestVersion) this.hashResult = result;
            } catch (error) { if (version === this.requestVersion) this.hashError = error.response?.data?.message || error.message || '校验失败，请重试'; }
            finally { if (version === this.requestVersion) this.hashing = false; }
        },
        async copyHash() {
            if (!this.hashResult) return;
            const value = this.hashResult.value;
            try {
                if (navigator.clipboard?.writeText) {
                    try { await navigator.clipboard.writeText(value); this.$message.success('校验值已复制'); return; }
                    catch (_) { /* 权限或非安全上下文限制时尝试选区复制。 */ }
                }
                const textarea = document.createElement('textarea');
                textarea.value = value; textarea.readOnly = true;
                textarea.style.cssText = 'position:fixed;left:-9999px;top:0';
                document.body.appendChild(textarea);
                try { textarea.select(); if (!document.execCommand('copy')) throw new Error('copy failed'); }
                finally { textarea.remove(); }
                this.$message.success('校验值已复制');
            } catch (_) { this.$message.warning('自动复制不可用，请选中校验值手动复制'); }
        },
        formatTime(value) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '-'; },
        formatSize(value) {
            const size = Number(value);
            if (!size) return '0 B';
            const units = ['B', 'KB', 'MB', 'GB', 'TB'];
            const index = Math.min(Math.floor(Math.log(size) / Math.log(1024)), units.length - 1);
            return `${(size / Math.pow(1024, index)).toFixed(index ? 2 : 0)} ${units[index]}`;
        },
    },
};
</script>

<style scoped>
.property-value { white-space: pre-wrap; overflow-wrap: anywhere; word-break: break-word; }
.property-notice { margin-bottom: 12px; }
.checksum-section { margin-top: 20px; }
.checksum-controls, .checksum-result-footer { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.checksum-controls .el-select { width: 145px; }
.checksum-hint, .checksum-result-footer span { color: #909399; font-size: 12px; }
.checksum-result-footer { justify-content: space-between; margin-top: 10px; }
</style>
<style>
.file-properties-dialog { max-width: calc(100vw - 24px); }
.file-properties-dialog .el-dialog__body { max-height: 65vh; overflow-y: auto; }
</style>
