<template>
    <el-dialog :title="saving ? '选择保存目录' : '选择服务器文件'" :visible.sync="visible" width="840px" class="service-picker-wrapper" custom-class="service-picker-dialog" append-to-body :close-on-click-modal="false">
        <el-radio-group v-model="space" :disabled="busy" @change="reset"><el-radio-button label="user">我的云盘</el-radio-button><el-radio-button label="public">公共文件</el-radio-button></el-radio-group>
        <div class="picker-navigation">
            <el-button size="small" :disabled="busy || path.length === 1" @click="up">返回上级</el-button>
            <p class="picker-path" :title="path.map(p => p.name).join(' / ')">{{ path.map(p => p.name).join(' / ') }}</p>
        </div>
        <el-alert v-if="error" :title="error" type="error" :closable="false" />
        <div v-loading="busy" class="picker-files">
            <el-button v-for="file in items" :key="file.fileId" class="picker-file" :disabled="busy || (saving && !file.isFolder)" @click="choose(file)">
                <i :class="file.isFolder ? 'el-icon-folder' : 'el-icon-document'"></i> <span>{{ file.fileName }}</span>
            </el-button>
            <p v-if="!busy && !items.length">此目录为空</p>
        </div>
        <span slot="footer"><el-button :disabled="busy" @click="visible = false">关闭</el-button><el-button v-if="saving" type="primary" :disabled="busy || !!error" @click="save">保存到此目录</el-button></span>
    </el-dialog>
</template>
<script>
import * as userFiles from '@/api/system/user-files';
import * as publicFiles from '@/api/system/public-files';
import { createApiClient } from '@/utils/request';
import SparkMD5 from 'spark-md5';
export default {
    name: 'ServiceFilePicker',
    data: () => ({ visible: false, saving: false, space: 'user', path: [{ id: null, name: '根目录' }], items: [], busy: false, error: '', result: null }),
    computed: { api() { return this.space === 'user' ? userFiles : publicFiles; }, parentId() { return this.path[this.path.length - 1].id; } },
    watch: { visible(value) { this.$emit('visibility-change', value); } },
    methods: {
        checked(res) { if (res.data.code !== 200) throw new Error(res.data.message || '操作失败'); return res.data.data; },
        open(result = null) { this.result = result; this.saving = !!result; this.visible = true; this.reset(); },
        reset() { this.path = [{ id: null, name: '根目录' }]; this.load(); },
        async load() { this.busy = true; this.error = ''; this.items = []; try { this.items = this.checked(await this.api.listFiles(this.parentId)) || []; } catch (e) { this.error = e.response?.data?.message || e.message; } finally { this.busy = false; } },
        up() { this.path.pop(); this.load(); },
        async choose(file) {
            if (file.isFolder) { this.path.push({ id: file.fileId, name: file.fileName }); this.load(); return; }
            if (this.saving) return;
            if (file.fileSize > 50 * 1024 * 1024) { this.error = '本地工具单个输入文件最多 50 MB'; return; }
            this.busy = true; this.error = '';
            try {
                const response = await createApiClient(`/api/${this.space}-files`).get('/download', { params: { fileId: file.fileId }, responseType: 'blob' });
                if (!response.headers['content-disposition']) {
                    const body = await response.data.text();
                    let message = '文件下载失败，请确认登录状态和文件权限';
                    try { const payload = JSON.parse(body); if (payload.code && payload.code !== 200) message = payload.message || message; } catch (_) { /* Do not import an unrecognized API response as a file. */ }
                    throw new Error(message);
                }
                if (response.data.size > 50 * 1024 * 1024) throw new Error('本地工具单个输入文件最多 50 MB');
                this.$emit('import', new File([response.data], file.fileName, { type: response.data.type })); this.visible = false;
            } catch (e) { this.error = e.response?.data?.message || e.message || '导入失败'; } finally { this.busy = false; }
        },
        async save() {
            this.busy = true; this.error = '';
            try {
                const existing = this.checked(await this.api.listFiles(this.parentId)) || [];
                if (existing.some(f => f.fileName.toLowerCase() === this.result.name.toLowerCase())) throw new Error('此目录已有同名项目，请关闭后修改结果文件名或选择其他目录');
                const file = new File([this.result.blob], this.result.name, { type: this.result.blob.type });
                const hash = SparkMD5.ArrayBuffer.hash(await file.arrayBuffer());
                this.checked(await this.api.uploadFile(this.parentId, hash, file));
                this.$message.success('已保存到服务器'); this.visible = false; this.$emit('saved');
            } catch (e) { this.error = e.response?.data?.message || e.message || '保存失败，仍可下载到本地'; } finally { this.busy = false; }
        }
    }
};
</script>
<style>
.service-picker-wrapper.el-dialog__wrapper { display: flex; align-items: center; justify-content: center; overflow: hidden; }
.service-picker-dialog.el-dialog {
    display: flex; flex-direction: column; min-height: 0;
    height: 640px;
    max-width: calc(100% - 32px); max-height: calc(100vh - 32px); max-height: calc(100dvh - 32px);
    margin: 0 !important; overflow: hidden; border-radius: 12px;
}
.service-picker-dialog .el-dialog__header { flex: 0 0 auto; padding: 20px 48px 16px 20px; }
.service-picker-dialog .el-dialog__body { display: flex; flex-direction: column; flex: 1 1 0; min-height: 0; overflow: hidden; padding: 0 20px; gap: 12px; }
.service-picker-dialog .el-dialog__body > .el-radio-group,
.service-picker-dialog .picker-navigation,
.service-picker-dialog .el-alert { flex: 0 0 auto; }
.service-picker-dialog .picker-navigation { display: flex; align-items: center; gap: 12px; min-width: 0; }
.service-picker-dialog .picker-path { margin: 0; min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.service-picker-dialog .picker-files {
    display: flex; flex-direction: column; gap: 8px; min-height: 0;
    flex: 1 1 0; overflow-x: hidden; overflow-y: auto; overscroll-behavior: contain;
    scrollbar-gutter: stable;
}
.service-picker-dialog .picker-file { flex: 0 0 auto; margin: 0 !important; width: 100%; min-width: 0; min-height: 42px; white-space: normal; text-align: left; padding: 10px 12px; }
.service-picker-dialog .picker-file > span { display: flex; align-items: flex-start; gap: 6px; min-width: 0; }
.service-picker-dialog .picker-file > span > span { overflow-wrap: anywhere; line-height: 1.4; }
.service-picker-dialog .picker-files > p { margin: 16px 0; text-align: center; color: #909399; }
.service-picker-dialog .el-dialog__footer { flex: 0 0 auto; padding: 16px 20px; }
@media (max-width: 480px) {
    .service-picker-dialog.el-dialog { max-width: calc(100% - 16px); max-height: calc(100dvh - 16px); }
    .service-picker-dialog .el-dialog__footer > span { display: flex; justify-content: flex-end; flex-wrap: wrap; gap: 8px; }
    .service-picker-dialog .el-dialog__footer .el-button { margin: 0; }
}
</style>
