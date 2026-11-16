<template>
    <el-dialog :title="'预览 — ' + (details ? details.fileName : source ? source.name : '')" :visible.sync="visible" :fullscreen="true" :lock-scroll="true"
        class="file-preview-wrapper" custom-class="file-preview-dialog" :close-on-click-modal="false" append-to-body @closed="reset">
        <div v-loading="loading" class="preview-body">
            <el-alert v-if="error" :title="error" type="warning" :closable="false" show-icon />
            <template v-else-if="!loading && type">
                <img v-if="type.kind === 'image'" :src="objectUrl" class="preview-image" alt="图像预览" @error="mediaError" />
                <video v-else-if="type.kind === 'video'" :src="objectUrl" controls class="preview-video" preload="metadata" @error="mediaError" />
                <audio v-else-if="type.kind === 'audio'" :src="objectUrl" controls class="preview-audio" @error="mediaError" />
                <iframe v-else-if="type.kind === 'pdf'" :src="objectUrl" title="PDF预览" class="preview-pdf" />
                <template v-else-if="type.kind === 'text'">
                    <div class="text-controls">
                        <span>文本编码</span>
                        <el-select v-model="encoding" size="small" @change="decode">
                            <el-option v-for="item in encodings" :key="item.value" :label="item.label" :value="item.value" />
                        </el-select>
                        <el-button v-if="type.renderable" size="small" icon="el-icon-top-right" @click="openRendered">新页面查看渲染效果</el-button>
                    </div>
                    <el-alert v-if="decodeWarning" :title="decodeWarning" type="info" :closable="false" />
                    <p v-if="textPages > 1" class="preview-hint">文本较长，按内容分页显示；可切换页面阅读全部文本。</p>
                    <pre class="text-preview"><code v-html="highlightedText"></code></pre>
                    <el-pagination v-if="textPages > 1" :current-page.sync="textPage" :page-size="1" :pager-count="5" :total="textPages" layout="prev, pager, next" />
                </template>
                <template v-else-if="office">
                    <el-alert :title="office.notice" type="info" :closable="false" />
                    <el-alert v-if="office.truncated" title="文档内容较多，当前展示部分内容，请下载原文件查看完整内容。" type="warning" :closable="false" />
                    <div v-if="type.kind === 'word'" class="word-preview">
                        <article class="word-paper" :style="office.pageStyle"><WordPreviewBlock v-for="(block, index) in office.blocks" :key="index" :block="block" /></article>
                    </div>
                    <template v-else-if="type.kind === 'excel'">
                        <el-select v-model="sheetIndex" size="small" class="sheet-selector"><el-option v-for="(sheet, index) in office.sheets" :key="index" :label="sheet.name" :value="index" /></el-select>
                        <ExcelSheetPreview :sheet="currentSheet" />
                    </template>
                    <div v-else class="slides-preview"><figure v-for="(slide, index) in office.slides" :key="index"><figcaption>第 {{ index + 1 }} 页</figcaption><img :src="slide" :alt="'幻灯片 ' + (index + 1)" loading="lazy" /></figure></div>
                </template>
            </template>
        </div>
        <span slot="footer"><el-button @click="visible = false">关闭</el-button><el-button type="primary" icon="el-icon-download" @click="download">下载原文件</el-button></span>
    </el-dialog>
</template>

<script>
import { createApiClient } from '@/utils/request';
import { PREVIEW_LIMIT, TEXT_PAGE_SIZE, previewType, escapeHtml, renderedPage } from '@/utils/file-preview';
import 'highlight.js/styles/github.css';
import WordPreviewBlock from '@/components/WordPreviewBlock.vue';
import ExcelSheetPreview from '@/components/ExcelSheetPreview.vue';

export default {
    name: 'FilePreviewDialog',
    components: { WordPreviewBlock, ExcelSheetPreview },
    props: { publicSpace: Boolean },
    data() {
        return { visible: false, source: null, details: null, loading: false, error: '', type: null, office: null,
            buffer: null, objectUrl: '', text: '', textPage: 1, encoding: 'utf-8', decodeWarning: '', sheetIndex: 0,
            highlighter: null, controller: null, version: 0,
            encodings: [{ label: 'UTF-8', value: 'utf-8' }, { label: 'GB18030 / GBK', value: 'gb18030' }, { label: 'Big5', value: 'big5' },
                { label: 'UTF-16 LE', value: 'utf-16le' }, { label: 'UTF-16 BE', value: 'utf-16be' }, { label: 'Windows-1252', value: 'windows-1252' }, { label: 'Shift-JIS', value: 'shift_jis' }] };
    },
    computed: {
        api() { return createApiClient(this.publicSpace ? '/api/public-files' : '/api/user-files', 120000); },
        textPages() { return Math.max(1, Math.ceil(this.text.length / TEXT_PAGE_SIZE)); },
        highlightedText() {
            const text = this.text.slice((this.textPage - 1) * TEXT_PAGE_SIZE, this.textPage * TEXT_PAGE_SIZE);
            if (this.highlighter && this.type && this.type.language !== 'plaintext' && this.highlighter.getLanguage(this.type.language)) {
                try { return this.highlighter.highlight(text, { language: this.type.language, ignoreIllegals: true }).value; } catch (_) { /* 按纯文本显示 */ }
            }
            return escapeHtml(text);
        },
        currentSheet() { return this.office && this.office.sheets[this.sheetIndex]; },
    },
    watch: { visible(value) {
        document.documentElement.classList.toggle('file-preview-open', value);
        if (!value) this.cancel();
    } },
    beforeDestroy() { document.documentElement.classList.remove('file-preview-open'); this.cancel(); },
    methods: {
        checked(response) { if (response.data.code !== 200) throw new Error(response.data.message || '预览失败'); return response.data.data; },
        cancel() {
            this.version++; if (this.controller) this.controller.abort(); this.controller = null; this.loading = false;
            if (this.objectUrl) URL.revokeObjectURL(this.objectUrl); this.objectUrl = '';
        },
        reset() { this.cancel(); this.buffer = null; this.text = ''; this.office = null; this.details = null; },
        async open(file) {
            this.reset(); const version = this.version; this.controller = new AbortController();
            const config = { signal: this.controller.signal };
            this.source = file; this.error = ''; this.type = null; this.encoding = 'utf-8'; this.textPage = 1; this.sheetIndex = 0;
            this.visible = true; this.loading = true;
            try {
                const details = this.checked(await this.api.get(`/${file.id}/properties`, config));
                if (version !== this.version) return;
                this.details = details;
                if (details.fileSize >= PREVIEW_LIMIT) throw new Error('文件过大，仅支持小于10MB的文件，请下载后预览');
                const type = previewType(details.fileName); this.type = type;
                if (!type.kind) throw new Error('暂不支持此文件格式的预览，请下载后查看');
                if (['word', 'excel', 'ppt'].includes(type.kind)) {
                    const office = this.checked(await this.api.get(`/${file.id}/preview/office`, config));
                    if (version === this.version) this.office = office;
                } else {
                    const response = await this.api.get(`/${file.id}/preview`, { ...config, responseType: 'arraybuffer' });
                    if (version !== this.version) return;
                    if (response.data.byteLength >= PREVIEW_LIMIT) throw new Error('文件过大，请下载后预览');
                    if (type.kind === 'text') {
                        this.buffer = response.data; this.decode();
                        import('highlight.js/lib/common').then(module => { if (version === this.version) this.highlighter = module.default || module; }).catch(() => {});
                    } else this.objectUrl = URL.createObjectURL(new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' }));
                }
            } catch (error) {
                if (version === this.version) {
                    let message = error.response?.data?.message;
                    if (!message && error.response?.data instanceof ArrayBuffer) {
                        try { message = JSON.parse(new TextDecoder().decode(error.response.data)).message; } catch (_) { /* 非JSON响应 */ }
                    }
                    this.error = message || error.message || '无法预览，请下载后查看';
                }
            } finally { if (version === this.version) this.loading = false; }
        },
        decode() {
            try {
                this.text = new TextDecoder(this.encoding).decode(this.buffer); this.textPage = 1;
                this.decodeWarning = this.text.includes('\ufffd') ? '部分字符无法正确解码，可切换其他编码重试。' : '';
            } catch (_) { this.decodeWarning = '浏览器不支持此编码，请选择其他编码。'; }
        },
        mediaError() { if (this.visible) this.error = '浏览器无法解码此文件，可能是媒体编码不受支持或文件已损坏，请下载后查看'; },
        openRendered() {
            if (!this.type?.renderable) return;
            const url = URL.createObjectURL(new Blob([renderedPage(this.text, this.type.extension, this.details.fileName)], { type: 'text/html;charset=utf-8' }));
            window.open(url, '_blank', 'noopener,noreferrer');
            setTimeout(() => URL.revokeObjectURL(url), 60000);
        },
        download() { if (this.source) this.$emit('download', this.source); },
    },
};
</script>

<style scoped>
.preview-body { flex: 1 1 0; display: flex; flex-direction: column; min-height: 0; min-width: 0; overflow: hidden; }
.preview-body > .el-alert { flex: 0 0 auto; margin-bottom: 8px; }
.preview-image, .preview-video { display: block; flex: 1 1 0; min-height: 0; min-width: 0; width: 100%; height: 100%; object-fit: contain; }
.preview-audio { width: 100%; flex: 0 0 auto; margin: auto 0; }
.preview-pdf { flex: 1 1 0; min-height: 0; width: 100%; height: 100%; border: 0; }
.text-controls { display: flex; flex: 0 0 auto; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 12px; }
.text-controls .el-select { width: 180px; }
.text-preview { flex: 1 1 0; min-height: 0; min-width: 0; margin: 0; overflow: auto; padding: 16px; box-sizing: border-box; background: #f8fafc; line-height: 1.6; tab-size: 4; }
.preview-body > .el-pagination { flex: 0 0 auto; padding: 10px 0 0; }
.preview-hint { flex: 0 0 auto; color: #909399; font-size: 12px; margin: 4px 0 8px; }
.word-preview { flex: 1 1 0; min-height: 0; min-width: 0; padding: 20px; box-sizing: border-box; background: #e9edf2; overflow: auto; }
.word-paper { box-sizing: border-box; width: 595pt; min-height: 842pt; padding: 72pt; margin: 0 auto; background: #fff; color: #000; box-shadow: 0 1px 8px #0002; }
.sheet-selector { flex: 0 0 auto; width: 240px; max-width: 100%; margin: 4px 0 12px; }
.slides-preview { flex: 1 1 0; min-height: 0; min-width: 0; overflow: auto; }
.slides-preview figure { margin: 16px 0; }
.slides-preview img { max-width: 100%; display: block; margin: auto; }
</style>
<style>
html.file-preview-open { overflow: hidden; }
.file-preview-wrapper.el-dialog__wrapper { overflow: hidden; }
.file-preview-dialog.el-dialog.is-fullscreen { display: flex; flex-direction: column; width: 100%; height: 100%; max-width: none; margin: 0; border-radius: 0; overflow: hidden; box-sizing: border-box; }
.file-preview-dialog .el-dialog__header { flex: 0 0 auto; padding: 16px 48px 16px 20px; }
.file-preview-dialog .el-dialog__title { display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.file-preview-dialog .el-dialog__headerbtn { top: 20px; }
.file-preview-dialog .el-dialog__body { flex: 1 1 0; display: flex; min-height: 0; min-width: 0; max-height: none; padding: 12px 20px; overflow: hidden; }
.file-preview-dialog .el-dialog__footer { flex: 0 0 auto; padding: 12px 20px; border-top: 1px solid #ebeef5; }
@media (max-width: 767px) {
    .file-preview-dialog .el-dialog__header { padding-left: 12px; }
    .file-preview-dialog .el-dialog__body { padding: 8px 12px; }
    .file-preview-dialog .el-dialog__footer { padding: 10px 12px; }
}
</style>
