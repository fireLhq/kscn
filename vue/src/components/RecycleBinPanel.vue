<template>
    <section class="recycle-panel" v-loading="loading || busy">
        <div class="recycle-header">
            <h3>回收站</h3>
        </div>
        <el-alert :closable="false" type="info" show-icon class="recycle-tip"
            title="删除后保留7天，回收站文件仍占用空间。原目录可用时恢复到原位置，否则放入根目录的“恢复文件夹”；重名时自动添加“恢复”“恢复2”等后缀。恢复文件夹会同时恢复本次删除的子项。" />
        <el-table :data="files" @selection-change="selection = $event" row-key="fileId" empty-text="回收站为空">
            <el-table-column type="selection" width="45" />
            <el-table-column prop="fileName" label="名称" min-width="180" show-overflow-tooltip>
                <template slot-scope="scope"><i :class="scope.row.isFolder === 1 ? 'el-icon-folder' : 'el-icon-document'"></i> {{ scope.row.fileName }}</template>
            </el-table-column>
            <el-table-column label="原目录" min-width="110">
                <template slot-scope="scope">
                    <el-popover v-if="scope.row.originalParentPath" placement="top" trigger="hover"
                        v-model="scope.row.parentPathVisible" title="原目录路径" width="280">
                        <div class="original-parent-path">{{ scope.row.originalParentPath }}</div>
                        <el-button slot="reference" type="text" class="parent-exists"
                            @click.stop="scope.row.parentPathVisible = true">存在</el-button>
                    </el-popover>
                    <span v-else class="parent-missing">不存在</span>
                </template>
            </el-table-column>
            <el-table-column label="删除时间" min-width="160">
                <template slot-scope="scope">{{ formatTime(scope.row.deleteTime) }}</template>
            </el-table-column>
            <el-table-column label="剩余保留时间" min-width="125">
                <template slot-scope="scope">{{ remaining(scope.row) }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="170">
                <template slot-scope="scope">
                    <el-button type="text" @click="restoreSelected([scope.row])" :disabled="busy || expired(scope.row)">恢复</el-button>
                    <el-button type="text" class="delete-action" @click="remove(scope.row)" :disabled="busy">永久删除</el-button>
                </template>
            </el-table-column>
        </el-table>
    </section>
</template>

<script>
import * as publicFiles from '@/api/system/public-files';
import * as userFiles from '@/api/system/user-files';

export default {
    name: 'RecycleBinPanel',
    props: { publicSpace: Boolean },
    data() { return { files: [], selection: [], loading: false, busy: false, now: Date.now(), timer: null }; },
    computed: {
        api() { return this.publicSpace ? publicFiles : userFiles; },
        token() { return this.$store.state.token; },
        toolbarState() { return { busy: this.busy || this.loading, selected: this.selection.length, total: this.files.length }; },
    },
    watch: {
        toolbarState: { immediate: true, handler(state) { this.$emit('toolbar-state', state); } },
    },
    mounted() {
        this.load();
        this.timer = setInterval(() => { this.now = Date.now(); }, 60000);
    },
    beforeDestroy() { clearInterval(this.timer); },
    methods: {
        checked(response) {
            if (response.data.code !== 200) throw new Error(response.data.message || '操作失败');
            return response.data.data;
        },
        async load() {
            this.loading = true;
            try {
                const [recycleResponse, treeResponse] = await Promise.all([
                    this.api.getRecycleBin(this.token),
                    this.api.getDirectoryTree(this.token),
                ]);
                const files = this.checked(recycleResponse) || [];
                const tree = this.checked(treeResponse) || [];
                const paths = this.directoryPaths(tree);
                this.files = files.map(file => ({
                    ...file,
                    originalParentPath: file.parentId == null ? '根目录' : (paths.get(String(file.parentId)) || null),
                    parentPathVisible: false,
                }));
                this.selection = [];
            }
            catch (error) { this.$message.error(error.response?.data?.message || error.message || '加载失败'); }
            finally { this.loading = false; }
        },
        directoryPaths(tree) {
            const paths = new Map();
            const visit = (nodes, parentPath) => {
                for (const node of nodes) {
                    const path = `${parentPath}/${node.label}`;
                    paths.set(String(node.id), path);
                    visit(node.children || [], path);
                }
            };
            visit(tree, '根目录');
            return paths;
        },
        formatTime(value) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '-'; },
        expiresAt(file) { return new Date(file.deleteTime).getTime() + 7 * 86400000; },
        expired(file) { return !file.deleteTime || this.expiresAt(file) <= this.now; },
        remaining(file) {
            if (this.expired(file)) return '已到期，等待清理';
            const hours = Math.ceil((this.expiresAt(file) - this.now) / 3600000);
            return hours > 24 ? `约${Math.ceil(hours / 24)}天` : `约${hours}小时`;
        },
        async restoreSelected(rows) {
            if (this.busy) return;
            const selected = Array.isArray(rows) ? rows : [...this.selection];
            if (!selected.length) return;
            this.busy = true;
            let count = 0;
            try {
                // 父目录先恢复；后续重新查询，跳过已随父目录恢复的子项。
                const byId = new Map(this.files.map(file => [file.fileId, file]));
                const depth = file => {
                    let value = 0;
                    const seen = new Set();
                    while (byId.has(file.parentId) && !seen.has(file.fileId)) {
                        seen.add(file.fileId); file = byId.get(file.parentId); value++;
                    }
                    return value;
                };
                selected.sort((a, b) => depth(a) - depth(b));
                for (const file of selected) {
                    const current = this.checked(await this.api.getRecycleBin(this.token)) || [];
                    if (!current.some(item => item.fileId === file.fileId)) continue;
                    this.checked(await this.api.restoreFile(file.fileId, this.token));
                    count++;
                }
                this.$message.success(`恢复完成（${count}个所选项目，包含其本次删除的子项）`);
            } catch (error) { this.$message.error(error.response?.data?.message || error.message || '恢复失败'); }
            finally { this.busy = false; await this.load(); this.$emit('changed'); }
        },
        async remove(file) {
            try { await this.$confirm(`永久删除“${file.fileName}”及其回收站子项？此操作无法恢复。`, '永久删除', { type: 'warning' }); }
            catch (_) { return; }
            await this.run(() => this.api.permanentDeleteFile(file.fileId, this.token));
        },
        async empty() {
            try { await this.$confirm('清空回收站内所有文件？此操作无法恢复。', '清空回收站', { type: 'warning' }); }
            catch (_) { return; }
            await this.run(() => this.api.emptyRecycleBin(this.token));
        },
        async run(action) {
            if (this.busy) return;
            this.busy = true;
            try { this.checked(await action()); this.$message.success('删除完成'); }
            catch (error) { this.$message.error(error.response?.data?.message || error.message || '删除失败'); }
            finally { this.busy = false; await this.load(); this.$emit('changed'); }
        },
    },
};
</script>

<style scoped>
.recycle-panel { min-width: 0; padding: 1.2rem; }
.recycle-header { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 16px; }
.recycle-header h3 { margin: 0; }
.recycle-tip { margin-bottom: 16px; }
.delete-action { color: #f56c6c; }
.parent-exists, .parent-exists:hover, .parent-exists:focus { color: #67c23a; }
.parent-missing { color: #f56c6c; }
.original-parent-path { white-space: pre-wrap; overflow-wrap: anywhere; }
</style>
