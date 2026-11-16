<template>
    <div class="network-file-upload">
        <el-alert :title="mode === 'LINK' ? '只保存链接，不占云盘容量。链接失效后文件将无法使用；不支持哈希校验和ZIP打包。' : '服务器获取直链文件后保存为普通文件，计入云盘容量。'" type="info" :closable="false" />
        <el-form label-position="top" size="small">
            <el-form-item label="文件直链"><el-input v-model="url" :disabled="busy" placeholder="https://example.com/file.pdf" maxlength="8192" @input="invalidate" /></el-form-item>
            <el-button :loading="checking" :disabled="busy || !url.trim()" @click="check">检查链接</el-button>
            <p v-if="probe" class="network-probe">可下载 · {{ probe.sizeKnown ? formatSize(probe.size) : '大小未知' }} · {{ probe.contentType || '未知类型' }}</p>
            <el-form-item label="保存名称"><el-input v-model="fileName" :disabled="busy" maxlength="255" placeholder="检查链接后自动填写，可修改" /></el-form-item>
            <p>仅支持无需登录的公开 HTTP/HTTPS 文件直链（80/443 端口），不支持网盘分享页面、网页地址或需要 Cookie 的链接。链接检查只能确认当前可用，不能保证长期有效。</p>
            <el-alert v-if="error" :title="error" type="error" :closable="false" />
            <el-button type="primary" :loading="submitting" :disabled="busy || !probe || !fileName.trim()" @click="submit">{{ mode === 'LINK' ? '添加远程链接' : '开始网络导入' }}</el-button>
        </el-form>
    </div>
</template>
<script>
import { createApiClient } from '@/utils/request';
export default {
    name: 'NetworkFileUpload',
    props: { publicSpace: Boolean, parentId: { default: null }, mode: String },
    data: () => ({ url: '', fileName: '', probe: null, checking: false, submitting: false, error: '' }),
    computed: { api() { return createApiClient(this.publicSpace ? '/api/public-files/network' : '/api/user-files/network', 660000); }, busy() { return this.checking || this.submitting; } },
    watch: { busy(value) { this.$emit('busy', value); }, mode() { this.invalidate(); } },
    methods: {
        invalidate() { this.probe = null; this.error = ''; },
        checked(response) { if (response.data.code !== 200) throw new Error(response.data.message || '操作失败'); return response.data.data; },
        formatSize(size) { return size >= 1048576 ? (size / 1048576).toFixed(2) + ' MB' : (size / 1024).toFixed(2) + ' KB'; },
        async check() {
            if (this.busy) return;
            this.checking = true; this.error = ''; this.probe = null;
            try { this.probe = this.checked(await this.api.post('/check', { url: this.url.trim() })); if (!this.fileName.trim()) this.fileName = this.probe.name; }
            catch (e) { this.error = e.response?.data?.message || e.message || '链接检查失败'; }
            finally { this.checking = false; }
        },
        async submit() {
            if (this.busy || !this.probe || !this.fileName.trim()) return;
            this.submitting = true; this.error = '';
            try {
                const result = this.checked(await this.api.post('', { mode: this.mode, url: this.url.trim(), fileName: this.fileName, parentId: this.parentId }));
                this.$message.success(this.mode === 'LINK' ? '远程链接已添加' : '网络文件已导入'); this.$emit('changed', result);
            } catch (e) { this.error = e.response?.data?.message || e.message || '添加失败'; }
            finally { this.submitting = false; }
        }
    }
};
</script>
<style scoped>
.network-file-upload p { font-size: 12px; line-height: 1.7; color: #909399; margin: 12px 0; }
.network-file-upload .el-form { margin-top: 16px; }
.network-file-upload .el-alert { margin-bottom: 12px; }
.network-file-upload .network-probe { color: #32854a; }
</style>
