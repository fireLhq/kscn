<template>
    <div class="file-operations">
        <div v-if="menu.visible" class="file-context-menu" role="menu" :style="{ left: menu.x + 'px', top: menu.y + 'px' }" @contextmenu.prevent>
            <button role="menuitem" @click="selectAll">全选</button>
            <button role="menuitem" @click="clearSelection">取消全选</button>
            <div class="context-divider"></div>
            <template v-if="selectedItems.length === 1">
                <button role="menuitem" :title="singleFile ? '预览文件' : '打开文件夹'" @click="requestItemAction('PREVIEW')">预览</button>
                <button role="menuitem" @click="requestItemAction('COPY')">复制</button>
                <button role="menuitem" @click="requestItemAction('MOVE')">移动</button>
                <button role="menuitem" @click="requestItemAction('TRANSFER')">{{ transferLabel }}</button>
                <button role="menuitem" @click="requestItemAction('DOWNLOAD')">下载</button>
                <button role="menuitem" @click="requestItemAction('RENAME')">重命名</button>
                <button role="menuitem" @click="requestItemAction('DELETE')">删除</button>
                <button role="menuitem" @click="showProperties">属性</button>
            </template>
            <template v-else>
                <button v-for="item in bulkActions" :key="item.value" role="menuitem" :disabled="!selectedItems.length" @click="request(item.value)">{{ item.label }}</button>
            </template>
        </div>

        <el-dialog :title="title" :visible.sync="visible" width="720px" custom-class="file-operation-dialog"
            :close-on-click-modal="false" :close-on-press-escape="!busy" :show-close="!busy" :before-close="beforeClose" append-to-body>
            <div v-loading="loading || busy">
                <p class="source-summary">已选择 {{ sources.length }} 个项目：{{ sources.map(item => item.name).join('、') }}</p>
                <template v-if="action !== 'RENAME'">
                    <p>选择目标目录：<strong>{{ destinationPath }}</strong></p>
                    <el-tree class="destination-tree" :data="directoryTree" node-key="key" :props="{ label: 'label', children: 'children' }"
                        :default-expanded-keys="['root']" :current-node-key="targetParentId == null ? 'root' : 'dir-' + targetParentId"
                        highlight-current :expand-on-click-node="false" @node-click="chooseDirectory">
                        <span slot-scope="{ data }" :class="{ 'unavailable-directory': data.unavailable }"><i class="el-icon-folder"></i> {{ data.label }}</span>
                    </el-tree>
                    <p class="operation-hint">文件夹连同子项一起操作；同时选中父目录和子项时，子项仅随父目录操作一次。文件夹重名不会合并。</p>
                </template>
                <el-form v-else label-width="90px" size="small">
                    <el-form-item label="新名称"><el-input v-model="baseName" maxlength="200" placeholder="输入名称，不包含文件扩展名" @input="resetGeneratedNames" /></el-form-item>
                    <el-form-item label="序号样式">
                        <el-select v-model="nameStyle" @change="resetGeneratedNames">
                            <el-option v-for="style in styles" :key="style.value" :label="style.label" :value="style.value" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="序号位数">
                        <el-input-number v-model="digits" :min="1" :max="8" @change="resetGeneratedNames" />
                    </el-form-item>
                    <p class="operation-hint">四种样式均支持补零位数。位数是最少位数：设为1位时，第10项使用10；设为2位时，第100项使用100，不截断编号。按选择顺序从1编号，保留文件扩展名。预览会同时检查目录内其他项目及本批项目之间的重名，也可逐项修改完整名称。</p>
                </el-form>
                <el-form label-width="90px" size="small">
                    <el-form-item label="重名处理">
                        <el-select v-model="conflictPolicy" @change="invalidate">
                            <el-option label="提示冲突，改名后再执行" value="ERROR" />
                            <el-option label="跳过重名项" value="SKIP" />
                            <el-option label="自动追加序号，如 名称(1).txt" value="AUTO" />
                        </el-select>
                    </el-form-item>
                </el-form>
                <el-alert v-if="error" :title="error" type="error" :closable="false" class="preview-notice" />
                <el-alert v-if="preview && !preview.canExecute" title="有重名项：请修改预览中的名称，或更换重名处理方式，再重新检查。" type="warning" :closable="false" class="preview-notice" />
                <el-alert v-if="dirty && preview" title="设置已改变，请重新检查并预览后执行。" type="info" :closable="false" class="preview-notice" />
                <el-table v-if="preview" :data="preview.entries" size="small" max-height="270">
                    <el-table-column prop="originalName" label="原名称" min-width="140" show-overflow-tooltip />
                    <el-table-column label="操作后名称" min-width="190">
                        <template slot-scope="scope"><el-input size="mini" :disabled="loading || busy" :value="customNames[scope.row.fileId] !== undefined ? customNames[scope.row.fileId] : scope.row.newName" @input="editName(scope.row.fileId, $event)" /></template>
                    </el-table-column>
                    <el-table-column label="状态" width="95">
                        <template slot-scope="scope"><span :class="{ 'conflict-text': scope.row.conflict }">{{ scope.row.skipped ? '跳过重名' : scope.row.conflict ? '重名冲突' : scope.row.unchanged ? '保持不变' : '可执行' }}</span></template>
                    </el-table-column>
                </el-table>
            </div>
            <span slot="footer">
                <el-button :disabled="busy" @click="visible = false">取消</el-button>
                <el-button :loading="loading" :disabled="busy || !canPreview" @click="loadPreview">检查并预览</el-button>
                <el-button type="primary" :loading="busy" :disabled="loading || dirty || !preview || !preview.canExecute || !effectiveCount" @click="execute">{{ actionLabel }}</el-button>
            </span>
        </el-dialog>
        <FilePropertiesDialog ref="properties" :public-space="publicSpace" />
        <FilePreviewDialog ref="preview" :public-space="publicSpace" @download="downloadPreviewSource" />
    </div>
</template>

<script>
import { createApiClient } from '@/utils/request';
import FilePropertiesDialog from '@/components/FilePropertiesDialog.vue';
import FilePreviewDialog from '@/components/FilePreviewDialog.vue';

export default {
    name: 'FileOperations',
    components: { FilePropertiesDialog, FilePreviewDialog },
    props: { publicSpace: Boolean, selectedItems: { type: Array, default: () => [] }, currentParentId: { default: null } },
    data() {
        return {
            menu: { visible: false, x: 0, y: 0 }, visible: false, loading: false, busy: false,
            sources: [], action: 'COPY', directoryTree: [], targetParentId: null, destinationPath: '根目录',
            baseName: '', nameStyle: 'PLAIN', digits: 1, conflictPolicy: 'ERROR', customNames: {},
            preview: null, dirty: true, error: '', confirming: false,
        };
    },
    computed: {
        bulkActions() { return [
            { value: 'COPY', label: '复制' }, { value: 'MOVE', label: '移动' },
            { value: 'TRANSFER', label: this.transferLabel },
            { value: 'DOWNLOAD', label: '批量下载' }, { value: 'RENAME', label: '批量重命名' },
            { value: 'DELETE', label: '批量删除' },
        ]; },
        singleFile() { return this.selectedItems.length === 1 && this.selectedItems[0].type === 'file'; },
        isTransfer() { return this.action === 'TRANSFER'; },
        transferLabel() { return this.publicSpace ? '添加到我的云盘' : '添加到公共资源'; },
        targetPublicSpace() { return this.isTransfer ? !this.publicSpace : this.publicSpace; },
        api() { return createApiClient(this.publicSpace ? '/api/public-files' : '/api/user-files', 120000); },
        destinationApi() { return this.isTransfer ? createApiClient(this.targetPublicSpace ? '/api/public-files' : '/api/user-files', 120000) : this.api; },
        actionLabel() { return this.isTransfer ? '复制' : { COPY: '复制', MOVE: '移动', RENAME: '重命名' }[this.action]; },
        title() { return this.isTransfer ? this.transferLabel : this.action === 'RENAME' ? '批量重命名' : this.actionLabel + '到…'; },
        canPreview() { return this.sources.length > 0 && (this.action !== 'RENAME' || this.baseName.trim()); },
        effectiveCount() { return this.preview ? this.preview.entries.filter(item => !item.skipped && !item.unchanged).length : 0; },
        styles() {
            const name = this.baseName || '名称';
            const first = '1'.padStart(this.digits, '0');
            const second = '2'.padStart(this.digits, '0');
            return [
                { value: 'PLAIN', label: `${name}${first}、${name}${second}` },
                { value: 'PARENTHESES', label: `${name}(${first})、${name}(${second})` },
                { value: 'DASH', label: `${name}-${first}、${name}-${second}` },
                { value: 'UNDERSCORE', label: `${name}_${first}、${name}_${second}` },
            ];
        },
    },
    mounted() {
        document.addEventListener('mousedown', this.closeMenuOutside);
        document.addEventListener('keydown', this.handleEscape);
        window.addEventListener('resize', this.closeMenu);
        window.addEventListener('scroll', this.closeMenu, true);
    },
    beforeDestroy() {
        document.removeEventListener('mousedown', this.closeMenuOutside);
        document.removeEventListener('keydown', this.handleEscape);
        window.removeEventListener('resize', this.closeMenu);
        window.removeEventListener('scroll', this.closeMenu, true);
    },
    methods: {
        checked(response) {
            if (response.data.code !== 200) throw new Error(response.data.message || '操作失败');
            return response.data.data;
        },
        showMenu(event) {
            this.menu = { visible: true, x: Math.max(8, Math.min(event.clientX, window.innerWidth - 176)), y: Math.max(8, Math.min(event.clientY, window.innerHeight - (this.selectedItems.length === 1 ? 390 : 320))) };
        },
        showProperties() {
            if (this.selectedItems.length !== 1) return;
            this.closeMenu();
            this.openProperties(this.selectedItems[0]);
        },
        openProperties(item) { this.$refs.properties.open(item); },
        openPreview(item) { this.closeMenu(); this.$refs.preview.open(item); },
        downloadPreviewSource(item) { this.$emit('item-action', 'DOWNLOAD', item); },
        requestItemAction(action) {
            if (this.selectedItems.length !== 1) return;
            this.closeMenu(); this.$emit('item-action', action, this.selectedItems[0]);
        },
        closeMenu() { this.menu.visible = false; },
        closeMenuOutside(event) { if (!event.target.closest('.file-context-menu')) this.closeMenu(); },
        handleEscape(event) { if (event.key === 'Escape') this.closeMenu(); },
        selectAll() { this.$emit('select-all'); this.closeMenu(); },
        clearSelection() { this.$emit('clear-selection'); this.closeMenu(); },
        request(action) { if (!this.selectedItems.length) return; this.closeMenu(); this.$emit('request', action, [...this.selectedItems]); },
        invalidate() { this.dirty = true; this.error = ''; },
        resetGeneratedNames() { this.customNames = {}; this.invalidate(); },
        editName(id, name) { this.$set(this.customNames, id, name); this.invalidate(); },
        payload() {
            return { action: this.isTransfer ? 'COPY' : this.action, targetSpace: this.isTransfer ? (this.targetPublicSpace ? 'PUBLIC' : 'USER') : undefined,
                fileIds: this.sources.map(item => item.id), targetParentId: this.targetParentId,
                conflictPolicy: this.conflictPolicy, baseName: this.baseName, nameStyle: this.nameStyle,
                digits: this.digits, customNames: this.customNames };
        },
        async open(action, sources) {
            if (this.busy || this.loading || !sources.length) return;
            if (sources.length > 200) { this.$message.warning('每批最多操作200个所选项目'); return; }
            this.closeMenu(); this.action = action; this.sources = [...sources]; this.visible = true;
            this.preview = null; this.dirty = true; this.error = ''; this.customNames = {}; this.conflictPolicy = 'ERROR';
            this.baseName = ''; this.nameStyle = 'PLAIN'; this.digits = 1;
            this.targetParentId = this.isTransfer ? null : this.currentParentId; this.destinationPath = '根目录';
            if (action === 'RENAME') return;
            this.loading = true;
            try {
                const tree = this.checked(await this.destinationApi.get('/tree')) || [];
                const selectedFolders = new Set(this.isTransfer ? [] : sources.filter(item => item.type === 'folder').map(item => String(item.id)));
                let selected;
                const convert = (nodes, parentPath, blocked = false) => nodes.map(node => {
                    const path = `${parentPath}/${node.label}`;
                    const unavailable = blocked || selectedFolders.has(String(node.id));
                    const result = { ...node, key: 'dir-' + node.id, fullPath: path, unavailable, children: convert(node.children || [], path, unavailable) };
                    if (node.id === this.targetParentId) selected = result;
                    return result;
                });
                this.directoryTree = [{ id: null, key: 'root', label: '根目录', fullPath: '根目录', children: convert(tree, '根目录') }];
                if (selected && !selected.unavailable) this.destinationPath = selected.fullPath;
                else this.targetParentId = null;
            } catch (error) { this.error = error.response?.data?.message || error.message || '目录加载失败'; }
            finally { this.loading = false; }
        },
        chooseDirectory(node) {
            if (this.loading || this.busy) return;
            if (node.unavailable) { this.$message.warning('不能选择源文件夹或其子目录'); return; }
            this.targetParentId = node.id; this.destinationPath = node.fullPath; this.invalidate();
        },
        async loadPreview() {
            if (this.loading || this.busy || !this.canPreview) return;
            this.loading = true; this.error = '';
            const payload = this.payload();
            const signature = JSON.stringify(payload);
            try {
                this.preview = this.checked(await this.api.post('/operations/preview', payload));
                this.dirty = signature !== JSON.stringify(this.payload());
            } catch (error) { this.error = error.response?.data?.message || error.message || '预览失败'; this.dirty = true; }
            finally { this.loading = false; }
        },
        beforeClose(done) { if (!this.busy) done(); },
        async execute() {
            if (this.busy || this.confirming || this.loading || this.dirty || !this.preview?.canExecute) return;
            const skipped = this.preview.entries.filter(item => item.skipped).length;
            this.confirming = true;
            try { await this.$confirm(`确认${this.actionLabel} ${this.effectiveCount} 个项目${skipped ? `，跳过 ${skipped} 个重名项目` : ''}？请核对预览中的最终名称。`, '确认操作', { type: 'warning' }); }
            catch (_) { this.confirming = false; return; }
            this.confirming = false;
            if (this.dirty || !this.visible) return;
            this.busy = true;
            try {
                const result = this.checked(await this.api.post('/operations/execute', { ...this.payload(), previewToken: this.preview.previewToken }));
                this.$message.success(`完成 ${result.processed} 项，跳过 ${result.skipped} 项，保持不变 ${result.unchanged} 项`);
                this.visible = false; this.$emit('changed');
            } catch (error) { this.error = error.response?.data?.message || error.message || '操作失败'; this.dirty = true; }
            finally { this.busy = false; }
        },
    },
};
</script>

<style scoped>
.file-context-menu { position: fixed; z-index: 2100; width: 160px; max-height: calc(100vh - 16px); overflow-y: auto; padding: 6px; border: 1px solid #e4e7ed; border-radius: 6px; background: #fff; box-shadow: 0 4px 18px #0002; }
.file-context-menu button { display: block; width: 100%; border: 0; border-radius: 4px; padding: 8px 12px; text-align: left; background: #fff; color: #303133; cursor: pointer; }
.file-context-menu button:hover, .file-context-menu button:focus { background: #ecf5ff; color: #409eff; }
.file-context-menu button:disabled { color: #c0c4cc; cursor: default; background: #fff; }
.context-divider { border-top: 1px solid #ebeef5; margin: 4px 0; }
.source-summary { overflow-wrap: anywhere; max-height: 70px; overflow: auto; }
.destination-tree { max-height: 240px; overflow: auto; border: 1px solid #ebeef5; padding: 8px; }
.unavailable-directory { color: #c0c4cc; cursor: not-allowed; }
.operation-hint { color: #909399; font-size: 12px; line-height: 1.6; }
.preview-notice { margin-bottom: 10px; }
.conflict-text { color: #f56c6c; }
</style>
<style>
.file-operation-dialog { max-width: calc(100vw - 24px); }
.file-operation-dialog .el-dialog__body { max-height: 65vh; overflow: auto; }
@media (max-width: 767px) { .file-operation-dialog .el-dialog__footer { padding: 12px; } .file-operation-dialog .el-dialog__footer .el-button { padding: 10px; } }
</style>
