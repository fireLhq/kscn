<template>
    <el-dialog :title="service ? service.name : ''" :visible.sync="visible" fullscreen append-to-body :close-on-click-modal="false" :before-close="close" class="local-tool-wrapper" :class="{ 'server-picker-open': pickerOpen }" custom-class="local-tool-dialog">
        <el-alert title="本地处理：内容只在当前页面内存中，不会自动保存。请下载结果或主动保存到服务器；关闭页面将丢失未保存内容。" type="info" :closable="false" />
        <div class="tool-actions">
            <el-button :disabled="busy" @click="$refs.input.click()">选择本地文件</el-button>
            <input ref="input" type="file" hidden :multiple="isBatch" :accept="isImage ? 'image/png,image/jpeg,image/webp,image/bmp' : isBatch ? '*' : '.txt,.md,.html,.json,.csv,.js,.css,.xml,.py,.java,.c,.cpp'" @change="localFiles" />
            <el-button :disabled="busy || !loggedIn" @click="$refs.picker.open()">从云盘 / 公共文件导入</el-button>
            <span v-if="!loggedIn">未登录也能使用本地工具；服务器导入、保存需登录。</span>
        </div>
        <p v-if="files.length">已导入：{{ files.map(f => f.name).join('、') }}</p>
        <template v-if="isImage">
            <p>支持 PNG、JPEG、WebP、BMP 图片，逐张处理；仅输出静态图，不保留动画，SVG 暂不支持。JPEG 的透明区域填充为白色。</p>
            <div class="tool-actions">
                <label>宽 <el-input-number v-model="width" :disabled="busy" :min="1" :max="12000" @change="edited" /></label>
                <label>高 <el-input-number v-model="height" :disabled="busy" :min="1" :max="12000" @change="edited" /></label>
                <label>旋转 <el-select v-model="rotation" :disabled="busy" @change="edited"><el-option v-for="r in [0,90,180,270]" :key="r" :label="r + '°'" :value="r" /></el-select></label>
                <label>格式 <el-select v-model="format" :disabled="busy" @change="edited"><el-option v-for="f in ['jpeg','png','webp']" :key="f" :label="f.toUpperCase()" :value="'image/' + f" /></el-select></label>
            </div>
            <p>宽高可分别调整；保持原比例时请按比例填写。JPEG / WebP 质量（PNG 为无损输出，不使用质量参数）：</p>
            <el-slider v-model="quality" :disabled="busy || format === 'image/png'" :min="10" :max="100" @change="edited" />
            <p v-if="files[0]">原始大小：{{ (files[0].size / 1024).toFixed(1) }} KB；压缩结果可能比原图更大，请比较后选择。</p>
        </template>
        <template v-else-if="isBatch">
            <p>只修改导出副本的名称，不会修改原文件；多文件结果打包为 ZIP。位数不足时自动扩展。</p>
            <div class="tool-actions">
                <el-input v-model="baseName" :disabled="busy" placeholder="新名称" @input="edited" />
                <el-select v-model="nameStyle" :disabled="busy" @change="edited"><el-option label="名称1" value="plain" /><el-option label="名称(1)" value="paren" /><el-option label="名称-1" value="dash" /><el-option label="名称_1" value="underscore" /></el-select>
                <label>序号位数 <el-input-number v-model="digits" :disabled="busy" :min="1" :max="8" @change="edited" /></label>
            </div>
        </template>
        <template v-else-if="id === 12">
            <p>填写学习任务，支持导入本工具导出的 JSON，导出时保留完成状态。</p>
            <el-button :disabled="busy || tasks.length >= 200" @click="addTask">添加任务</el-button>
            <div v-for="(task, index) in tasks" :key="index" class="plan-row">
                <el-checkbox v-model="task.done" @change="edited">完成</el-checkbox>
                <el-input v-model="task.title" placeholder="学习内容" maxlength="200" @input="edited" />
                <input v-model="task.date" type="date" aria-label="计划日期" @input="edited" />
                <el-button @click="tasks.splice(index, 1); edited()">移除</el-button>
            </div>
        </template>
        <template v-else>
            <p>{{ textHelp }}</p>
            <el-select v-if="id === 3" v-model="conversion" :disabled="busy" @change="edited">
                <el-option label="Markdown / 文本 → HTML" value="html" /><el-option label="文本 → UTF-8 TXT" value="text" /><el-option label="JSON 美化" value="json" /><el-option label="UTF-8 → Base64" value="base64" /><el-option label="Base64 → UTF-8" value="unbase64" />
            </el-select>
            <el-input v-model="text" type="textarea" :rows="14" :disabled="busy" placeholder="输入内容，或导入 UTF-8 文本文件" @input="edited" />
        </template>
        <div class="tool-actions">
            <el-button type="primary" :loading="busy" @click="process">{{ busy ? '正在处理' : '生成结果' }}</el-button>
            <span v-if="busy">请等待当前处理完成，结果生成后可离线下载。</span>
        </div>
        <el-alert v-if="error" :title="error" type="error" :closable="false" />
        <section v-if="result" class="tool-result">
            <h3>处理结果</h3>
            <el-input v-model="result.name" :disabled="busy" aria-label="结果文件名" @input="dirty = true" />
            <p>{{ (result.blob.size / 1024).toFixed(1) }} KB{{ resultStale ? ' · 输入已修改，请重新生成以更新结果' : '' }}</p>
            <div class="tool-actions">
                <el-button :disabled="busy || resultStale" @click="download">下载结果</el-button>
                <el-button :disabled="busy || resultStale || !loggedIn" @click="save">保存到云盘 / 公共文件</el-button>
            </div>
            <img v-if="isImage" :src="resultUrl" class="image-result" alt="处理结果" />
            <iframe v-else-if="id === 1 || (id === 3 && conversion === 'html')" :src="resultUrl" sandbox="" title="文档结果预览" class="document-result"></iframe>
            <pre v-else-if="resultText">{{ resultText }}</pre>
            <ul v-else-if="isBatch"><li v-for="name in renamed" :key="name">{{ name }}</li></ul>
        </section>
        <span slot="footer"><el-button @click="close()">关闭工具</el-button></span>
        <ServiceFilePicker ref="picker" @import="importFile" @visibility-change="pickerOpen = $event" />
    </el-dialog>
</template>
<script>
import ServiceFilePicker from './ServiceFilePicker.vue';
import { convertText, renderDocument, renameFiles, zipFiles, validName, processImage, downloadResult } from '@/utils/local-tools';
export default {
    name: 'LocalToolWorkbench', components: { ServiceFilePicker },
    data: () => ({ visible: false, pickerOpen: false, service: null, files: [], text: '', tasks: [], dirty: false, busy: false, error: '', result: null, resultUrl: '', resultText: '', resultStale: false, renamed: [], width: 1, height: 1, rotation: 0, format: 'image/jpeg', quality: 80, conversion: 'html', baseName: '文件', digits: 1, nameStyle: 'underscore' }),
    computed: {
        id() { return this.service?.id; }, isImage() { return [4, 5].includes(this.id); }, isBatch() { return this.id === 6; },
        loggedIn() { return this.$store.getters.isAuthenticated; },
        textHelp() { return this.id === 7 ? '当前支持 JSON 格式化与语法检查；其他语言暂不支持。' : this.id === 3 ? '支持文本、Markdown、JSON 与 Base64 的本地转换，不支持 Office 文档互转。' : '使用纯文本 / Markdown 编辑，支持标题、列表、表格。文档导出为 HTML，笔记导出为 Markdown；不支持多人协作或自动保存。'; }
    },
    mounted() { window.addEventListener('beforeunload', this.unload); },
    beforeDestroy() { window.removeEventListener('beforeunload', this.unload); this.release(); },
    methods: {
        open(service) { this.service = service; this.files = []; this.text = ''; this.tasks = []; this.error = ''; this.result = null; this.resultText = ''; this.renamed = []; this.dirty = false; this.resultStale = false; this.release(); this.visible = true; },
        edited() { this.dirty = true; if (this.result) this.resultStale = true; },
        unload(event) { if (!this.visible || (!this.dirty && !this.busy && !this.$refs?.picker?.busy)) return; event.preventDefault(); event.returnValue = ''; },
        async canLeave() {
            if (this.busy || this.$refs.picker?.busy) { this.$message.warning('请等待处理、文件导入或保存完成'); return false; }
            if (!this.visible || !this.dirty) return true;
            try { await this.$confirm('内容仅保存在当前页面。请先下载或保存需要的结果；继续离开将丢失内容。本系统不会自动保存。', '离开前请确认', { confirmButtonText: '放弃并离开', cancelButtonText: '继续编辑', type: 'warning', closeOnClickModal: false }); return true; } catch (_) { return false; }
        },
        async close(done) { if (!(await this.canLeave())) return; this.visible = false; this.dirty = false; this.files = []; this.text = ''; this.tasks = []; this.result = null; this.resultText = ''; this.release(); if (typeof done === 'function') done(); },
        release() { if (this.resultUrl) URL.revokeObjectURL(this.resultUrl); this.resultUrl = ''; },
        async localFiles(event) { const files = [...event.target.files]; event.target.value = ''; await this.importFiles(files); },
        importFile(file) { return this.importFiles([file]); },
        async importFiles(incoming) {
            if (!incoming.length || this.busy) return;
            this.busy = true; this.error = '';
            try {
                const selected = this.isBatch ? [...this.files, ...incoming] : [incoming[0]];
                if (selected.length > 100 || selected.some(f => f.size > 50 * 1024 * 1024) || selected.reduce((s,f) => s + f.size,0) > 100 * 1024 * 1024) throw new Error('最多 100 个文件，单个不超过 50 MB，合计不超过 100 MB');
                if (this.isImage) {
                    if (!/\.(png|jpe?g|webp|bmp)$/i.test(selected[0].name)) throw new Error('请选择 PNG、JPEG、WebP 或 BMP 静态图片');
                    const url = URL.createObjectURL(selected[0]);
                    try { const image = new Image(); image.src = url; await image.decode(); this.width = image.naturalWidth; this.height = image.naturalHeight; this.rotation = 0; } finally { URL.revokeObjectURL(url); }
                } else if (!this.isBatch) {
                    if (selected[0].size > 2 * 1024 * 1024) throw new Error('文本输入最多 2 MB');
                    const text = new TextDecoder('utf-8', { fatal: true }).decode(await selected[0].arrayBuffer());
                    if (this.id === 12) {
                        const tasks = JSON.parse(text);
                        if (!Array.isArray(tasks) || tasks.length > 200 || tasks.some(t => !t || typeof t.title !== 'string' || t.title.length > 200 || typeof t.date !== 'string' || !/^$|^\d{4}-\d{2}-\d{2}$/.test(t.date) || typeof t.done !== 'boolean')) throw new Error('请选择本工具导出的学习计划 JSON（最多 200 项）');
                        this.tasks = tasks.map(t => ({ title: t.title, date: t.date, done: t.done }));
                    } else this.text = text;
                }
                this.files = selected; this.edited();
            } catch (e) { this.error = e.message || '导入失败'; } finally { this.busy = false; }
        },
        addTask() { this.tasks.push({ title: '', date: '', done: false }); this.edited(); },
        async process() {
            if (this.busy) return;
            this.busy = true; this.error = '';
            try {
                // Allow the browser to paint its processing state before local work.
                await new Promise(resolve => setTimeout(resolve, 0));
                let name, blob, output = '', renamed = [];
                if (this.isImage) {
                    if (!this.files.length) throw new Error('请先导入图片');
                    blob = await processImage(this.files[0], { width: this.width, height: this.height, rotation: this.rotation, format: this.format, quality: this.quality / 100 });
                    name = this.files[0].name.replace(/\.[^.]+$/, '') + '-处理.' + this.format.split('/')[1].replace('jpeg','jpg');
                } else if (this.isBatch) {
                    if (!this.files.length) throw new Error('请先选择文件');
                    const entries = renameFiles(this.files, this.baseName, this.nameStyle, this.digits);
                    renamed = entries.map(e => e.name); blob = await zipFiles(entries); name = '重命名结果.zip';
                } else if (this.id === 12) {
                    if (!this.tasks.length || this.tasks.some(t => !t.title.trim())) throw new Error('请添加任务并填写学习内容');
                    output = JSON.stringify(this.tasks, null, 2); name = '学习计划.json';
                } else {
                    if (!this.text.trim()) throw new Error('请先输入或导入内容');
                    if (new TextEncoder().encode(this.text).length > 2 * 1024 * 1024) throw new Error('文本内容最多 2 MB');
                    if (this.id === 1) { output = renderDocument(this.text); name = '文档.html'; }
                    else if (this.id === 10) { output = this.text; name = '笔记.md'; }
                    else { output = convertText(this.text, this.id === 7 ? 'json' : this.conversion); name = this.id === 7 || this.conversion === 'json' ? '结果.json' : this.conversion === 'html' ? '结果.html' : '结果.txt'; }
                }
                if (!blob) blob = new Blob([output], { type: name.endsWith('.html') ? 'text/html;charset=utf-8' : 'text/plain;charset=utf-8' });
                this.release(); this.result = { name, blob }; this.resultUrl = URL.createObjectURL(blob); this.resultText = output; this.renamed = renamed; this.resultStale = false; this.dirty = true;
            } catch (e) { this.error = e.message || '处理失败'; } finally { this.busy = false; }
        },
        download() { try { validName(this.result.name); downloadResult(this.result); } catch (e) { this.error = e.message; } },
        save() { try { validName(this.result.name); this.$refs.picker.open({ ...this.result }); } catch (e) { this.error = e.message; } }
    }
};
</script>
<style>
.local-tool-wrapper.el-dialog__wrapper { overflow: hidden; }
.local-tool-dialog.el-dialog.is-fullscreen { display: flex; flex-direction: column; overflow: hidden; }
.local-tool-dialog .el-dialog__header, .local-tool-dialog .el-dialog__footer { flex: 0 0 auto; }
.local-tool-dialog .el-dialog__body { padding: 12px 24px; flex: 1 1 auto; min-height: 0; overflow: auto; overscroll-behavior: contain; }
.local-tool-wrapper.server-picker-open .local-tool-dialog .el-dialog__body { overflow: hidden; }
.local-tool-dialog .tool-actions { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; margin: 16px 0; }
.local-tool-dialog .tool-actions > .el-input { width: 220px; }
.local-tool-dialog .tool-actions .el-select { width: 180px; }
.local-tool-dialog .tool-actions .el-button { margin-left: 0; }
.local-tool-dialog p { margin: 12px 0; overflow-wrap: anywhere; }
.local-tool-dialog .tool-result { margin-top: 20px; padding: 20px; border: 1px solid #dce4f2; border-radius: 12px; }
.local-tool-dialog pre { white-space: pre-wrap; overflow-wrap: anywhere; max-height: 400px; overflow: auto; }
.local-tool-dialog .image-result { max-width: 100%; max-height: 500px; object-fit: contain; }
.local-tool-dialog .document-result { width: 100%; height: 500px; border: 1px solid #e5e7eb; background: white; }
.local-tool-dialog .plan-row { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; margin: 12px 0; }
.local-tool-dialog .plan-row .el-input { flex: 1; min-width: 160px; }
</style>
