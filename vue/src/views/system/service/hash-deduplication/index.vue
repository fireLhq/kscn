<template>
    <main class="algorithm-page hash-dedup-page">
        <div class="algorithm-container">
        <header class="hash-page-header">
            <button type="button" class="back-link" aria-label="返回上一页" @click="$router.back()"><i class="el-icon-arrow-left" /> 返回</button>
            <div><h1>Hash 文件去重</h1><p>在浏览器中比较文件内容，按规则整理并导出保留副本。</p></div>
        </header>
        <section class="hash-control-panel" aria-label="去重操作栏">
        <el-alert title="文件仅在浏览器本地处理，不上传服务器。移除和保留规则仅影响当前清单及导出副本，不会删除或修改原文件。离开页面后清单将丢失。" type="info" :closable="false" />
        <div class="hash-toolbar">
            <el-button size="mini" :disabled="busy" @click="$refs.files.click()">追加文件</el-button>
            <el-button size="mini" :disabled="busy" @click="$refs.folder.click()">追加文件夹</el-button>
            <input ref="files" type="file" multiple hidden @change="importFiles" />
            <input ref="folder" type="file" multiple webkitdirectory hidden @change="importFiles" />
            <el-button size="mini" :disabled="busy || !selected.length" @click="removeSelected">从清单移除选中</el-button>
            <el-button size="mini" :disabled="busy || !rows.length" @click="clear">清空清单</el-button>
            <span class="hash-action-divider" aria-hidden="true" />
            <el-button size="mini" type="primary" :disabled="busy || !rows.length" @click="calculate">计算 / 继续</el-button>
            <el-button size="mini" :disabled="!busy || exporting" @click="stop = true">停止</el-button>
            <el-button size="mini" :disabled="busy || !rows.length" @click="resetHashes">清除 Hash 后重算</el-button>
            <el-button size="mini" :disabled="busy || !rows.length" @click="exportReport">导出报告 JSON</el-button>
            <el-button size="mini" :disabled="busy || !canExport" @click="exportFiles">下载保留副本 ZIP</el-button>
        </div>
        <div class="hash-toolbar">
            <label>算法 <el-select size="mini" class="hash-algorithm-select" v-model="algorithm" :disabled="busy" @change="resetHashes"><el-option v-for="a in ['MD5','SHA-1','SHA-256']" :key="a" :label="a" :value="a" /></el-select></label>
            <el-checkbox v-model="recursive" :disabled="busy">导入时包含子文件夹</el-checkbox>
            <el-input size="mini" v-model="extensions" :disabled="busy" placeholder="扩展名：jpg,png（空为全部）" aria-label="扩展名过滤" class="hash-filter" />
            <el-checkbox v-model="sizeBucket" :disabled="busy">先按大小分桶</el-checkbox>
            <el-checkbox v-model="highlight">高亮当前文件的重复项</el-checkbox>
        </div>
        <details class="hash-help"><summary>使用说明与限制</summary><p class="hash-note">支持多次追加文件夹；过滤选项作用于下一次导入。同路径、大小和修改时间的文件自动跳过。浏览器不提供绝对路径和创建时间，时间规则使用文件修改时间。MD5 分块读取，SHA 单文件上限 256 MB。</p></details>
        <el-progress :percentage="progress" />
        <p class="hash-status" role="status">{{ status }} · {{ rows.length }} 个文件 · {{ groups.length }} 组重复 · 多余 {{ duplicateCount }} 份（{{ size(duplicateBytes) }}） · 已排除 {{ excludedCount }} 份</p>
        <el-alert v-if="error" :title="error" type="error" :closable="false" />
        </section>
        <section class="hash-result-panel" aria-label="文件比较结果">
        <el-tabs v-model="tab">
            <el-tab-pane label="文件清单" name="files">
                <el-table size="small" :data="rows" row-key="id" height="400" :row-class-name="rowClass" @selection-change="selected = $event" @row-click="active = $event" @row-dblclick="findDuplicates">
                    <el-table-column type="selection" width="48" :selectable="() => !busy" />
                    <el-table-column prop="path" label="文件 / 相对路径" min-width="260" show-overflow-tooltip sortable />
                    <el-table-column label="大小" width="110"><template slot-scope="s">{{ size(s.row.file.size) }}</template></el-table-column>
                    <el-table-column label="Hash" min-width="220" show-overflow-tooltip><template slot-scope="s">{{ s.row.hash || '—' }}</template></el-table-column>
                    <el-table-column label="修改时间" width="180"><template slot-scope="s">{{ time(s.row.modified) }}</template></el-table-column>
                    <el-table-column label="状态" min-width="140"><template slot-scope="s">{{ s.row.status }}{{ s.row.excluded ? ' · 已排除' : '' }}</template></el-table-column>
                </el-table>
                <section v-if="active" class="hash-detail">
                    <h3>文件详情</h3><p>{{ active.path }}</p><p>{{ size(active.file.size) }} · 修改时间：{{ time(active.modified) }}</p>
                    <p class="hash-value">{{ algorithm }}：{{ active.hash || '尚未计算' }}</p><p v-if="active.error">读取失败：{{ active.error }}</p>
                    <el-button size="mini" :disabled="busy || !active.hash" @click="findDuplicates(active)">查找此文件的重复项</el-button>
                </section>
            </el-tab-pane>
            <el-tab-pane :label="'重复分组（' + groups.length + '）'" name="groups">
                <div class="hash-toolbar">
                    <el-select size="mini" v-model="rule" :disabled="busy" aria-label="保留规则">
                        <el-option label="按自然文件名保留一个" value="path" /><el-option label="保留修改时间最早" value="earliest" /><el-option label="保留修改时间最晚" value="latest" />
                        <el-option label="保留路径包含关键字" value="keyword" /><el-option label="优先保留路径不含关键字" value="exclude" />
                    </el-select>
                    <el-input size="mini" v-if="keywordRule" v-model="keyword" :disabled="busy" placeholder="路径关键字（忽略大小写）" class="hash-filter" />
                    <el-select size="mini" v-if="keywordRule" v-model="tie" :disabled="busy" aria-label="多项匹配时的决定依据"><el-option label="多项匹配：自然路径序" value="path" /><el-option label="多项匹配：修改时间最早" value="earliest" /><el-option label="多项匹配：修改时间最晚" value="latest" /></el-select>
                    <el-button size="mini" :disabled="busy || !currentGroup || (keywordRule && !keyword.trim())" @click="applyRule(false)">应用当前组</el-button>
                    <el-button size="mini" :disabled="busy || !groups.length || (keywordRule && !keyword.trim())" @click="applyRule(true)">应用所有组</el-button>
                    <el-button size="mini" :disabled="busy || !excludedCount" @click="restore">重置所有排除标记</el-button>
                </div>
                <p>规则每组保留一个，无法匹配时跳过该组。应用规则后可手动调整；下载前会检查每组至少保留一份。</p>
                <div class="hash-groups">
                    <el-table size="small" :data="groups" height="360" highlight-current-row @row-click="groupKey = $event.key">
                        <el-table-column label="Hash 分组" min-width="180" show-overflow-tooltip><template slot-scope="s">{{ s.row.hash }}</template></el-table-column>
                        <el-table-column label="份数" width="70"><template slot-scope="s">{{ s.row.files.length }}</template></el-table-column>
                    </el-table>
                    <el-table size="small" :data="currentGroup ? currentGroup.files : []" height="360" :row-class-name="rowClass">
                        <el-table-column label="保留" width="70"><template slot-scope="s"><el-checkbox :value="!s.row.excluded" :disabled="busy" @change="toggleKeep(s.row, $event)" /></template></el-table-column>
                        <el-table-column prop="path" label="文件 / 相对路径" min-width="220" show-overflow-tooltip />
                        <el-table-column label="修改时间" width="180"><template slot-scope="s">{{ time(s.row.modified) }}</template></el-table-column>
                    </el-table>
                </div>
                <p v-if="!groups.length">暂无重复分组，请先导入并计算文件。分桶模式会跳过大小唯一的文件。</p>
            </el-tab-pane>
        </el-tabs>
        </section>
        </div>
    </main>
</template>
<script>
import '../algorithm/algorithm.css';
import { filterFiles, duplicateGroups, chooseKeep, hashFile } from './engine';
import { zipFiles, downloadResult } from '@/utils/local-tools';
export default {
    name: 'HashDeduplicationIndex',
    data: () => ({ rows: [], selected: [], active: null, serial: 0, algorithm: 'SHA-256', recursive: true, extensions: '', sizeBucket: true, highlight: true, busy: false, exporting: false, stop: false, progress: 0, status: '就绪', error: '', tab: 'files', groupKey: '', rule: 'path', keyword: '', tie: 'path' }),
    computed: {
        groups() { return duplicateGroups(this.rows); },
        currentGroup() { return this.groups.find(g => g.key === this.groupKey) || this.groups[0]; },
        duplicateCount() { return this.groups.reduce((n, g) => n + g.files.length - 1, 0); },
        duplicateBytes() { return this.groups.reduce((n, g) => n + (g.files.length - 1) * g.files[0].file.size, 0); },
        excludedCount() { return this.rows.filter(r => r.excluded).length; },
        keywordRule() { return ['keyword', 'exclude'].includes(this.rule); },
        canExport() { return this.rows.length > 0 && this.rows.every(r => r.hash || r.status === '大小唯一'); }
    },
    async beforeRouteLeave(to, from, next) { next(await this.canLeave()); },
    mounted() { window.addEventListener('beforeunload', this.unload); },
    beforeDestroy() { this.stop = true; window.removeEventListener('beforeunload', this.unload); },
    methods: {
        unload(event) { if (this.rows.length) { event.preventDefault(); event.returnValue = ''; } },
        async canLeave() {
            if (this.busy) { this.$message.warning('请先停止计算或等待导出完成'); return false; }
            if (!this.rows.length) return true;
            try { await this.$confirm('离开页面后将丢失当前文件清单、Hash 和保留标记，请先导出需要的结果。', '离开页面', { type: 'warning', closeOnClickModal: false, confirmButtonText: '离开并清空', cancelButtonText: '继续整理' }); return true; } catch (_) { return false; }
        },
        size(bytes) { if (bytes < 1024) return bytes + ' B'; const i = Math.min(3, Math.floor(Math.log(bytes) / Math.log(1024))); return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + ['B','KB','MB','GB'][i]; },
        time(value) { return Number.isFinite(value) ? new Date(value).toLocaleString() : '不可用'; },
        importFiles(event) {
            const incoming = [...event.target.files]; event.target.value = ''; if (this.busy) return;
            const files = filterFiles(incoming, this.recursive, this.extensions);
            const key = f => JSON.stringify([f.webkitRelativePath || f.name, f.size, f.lastModified]);
            const seen = new Set(this.rows.map(r => key(r.file))); let added = 0;
            for (const file of files) {
                if (seen.has(key(file))) continue;
                if (this.rows.length >= 10000) { this.error = '清单最多支持 10000 个文件，请分批整理'; break; }
                seen.add(key(file)); added++;
                this.rows.push({ id: ++this.serial, file, path: file.webkitRelativePath || file.name, modified: Number.isFinite(file.lastModified) ? file.lastModified : null, hash: '', status: '未计算', error: '', excluded: false });
            }
            this.restore(); this.progress = 0; this.status = `追加 ${added} 个文件，跳过 ${files.length - added} 个，过滤 ${incoming.length - files.length} 个`;
        },
        clear() { if (this.busy) return; this.rows = []; this.selected = []; this.active = null; this.groupKey = ''; this.error = ''; this.progress = 0; this.status = '已清空清单'; },
        removeSelected() { if (this.busy) return; const ids = new Set(this.selected.map(r => r.id)); this.rows = this.rows.filter(r => !ids.has(r.id)); this.selected = []; if (this.active && ids.has(this.active.id)) this.active = null; this.restore(); },
        restore() { this.rows.forEach(r => { r.excluded = false; }); },
        resetHashes() { if (this.busy) return; this.rows.forEach(r => { r.hash = ''; r.status = '未计算'; r.error = ''; r.excluded = false; }); this.progress = 0; this.status = 'Hash 已清除，可重新计算'; },
        rowClass({ row }) { if (row.excluded) return 'hash-excluded'; return this.highlight && this.active?.hash && row.hash === this.active.hash && row.file.size === this.active.file.size ? 'hash-match' : ''; },
        findDuplicates(row) { this.active = row; const group = this.groups.find(g => g.files.includes(row)); if (!group) { this.$message.info('此文件暂无已计算的重复项'); return; } this.groupKey = group.key; this.tab = 'groups'; },
        toggleKeep(row, keep) {
            const group = this.groups.find(g => g.files.includes(row));
            if (!keep && group && !group.files.some(r => r !== row && !r.excluded)) { this.$message.warning('每组至少保留一份文件'); return; }
            row.excluded = !keep;
        },
        applyRule(all) {
            if (this.busy) return; let applied = 0, skipped = 0;
            for (const group of all ? this.groups : [this.currentGroup].filter(Boolean)) {
                const keep = chooseKeep(group.files, this.rule, this.keyword, this.tie);
                if (!keep) { skipped++; continue; }
                group.files.forEach(r => { r.excluded = r !== keep; }); applied++;
            }
            this.status = `已应用 ${applied} 组，跳过 ${skipped} 组；请检查保留标记后下载`;
        },
        async calculate() {
            if (this.busy) return; this.busy = true; this.stop = false; this.error = ''; this.restore();
            const counts = new Map(); this.rows.forEach(r => counts.set(r.file.size, (counts.get(r.file.size) || 0) + 1));
            let completed = 0, failures = 0;
            try {
                for (const row of this.rows) {
                    if (this.stop) break;
                    this.status = `正在计算 ${completed + 1}/${this.rows.length}：${row.path}`;
                    if (!row.hash && this.sizeBucket && counts.get(row.file.size) === 1) row.status = '大小唯一';
                    else if (!row.hash) {
                        row.status = '计算中'; row.error = '';
                        try { row.hash = await hashFile(row.file, this.algorithm, { cancelled: () => this.stop, progress: bytes => { this.progress = Math.min(99, Math.floor((completed + (row.file.size ? bytes / row.file.size : 1)) / this.rows.length * 100)); } }); row.status = '已计算'; }
                        catch (e) { if (e.name === 'AbortError') { row.status = '已停止'; break; } row.status = '读取失败'; row.error = e.message; failures++; }
                    }
                    completed++; this.progress = Math.floor(completed / this.rows.length * 100);
                    await new Promise(resolve => setTimeout(resolve, 0));
                }
                this.status = this.stop ? '已停止，已完成的 Hash 保留，可继续计算' : `计算完成，失败 ${failures} 个`;
            } finally { this.busy = false; }
        },
        exportReport() {
            const report = { algorithm: this.algorithm, generatedAt: new Date().toISOString(), files: this.rows.map(r => ({ path: r.path, size: r.file.size, lastModified: r.modified, hash: r.hash, status: r.status, error: r.error, keep: !r.excluded })), groups: this.groups.map(g => ({ hash: g.hash, files: g.files.map(r => r.id) })) };
            // Include stable row IDs so duplicate groups can be resolved in the report.
            report.files.forEach((r, i) => { r.id = this.rows[i].id; });
            downloadResult({ name: 'Hash去重报告.json', blob: new Blob([JSON.stringify(report, null, 2)], { type: 'application/json' }) });
        },
        async exportFiles() {
            if (this.busy || !this.canExport) return;
            if (this.groups.some(g => g.files.every(r => r.excluded))) { this.error = '每个重复组至少保留一份'; return; }
            const kept = this.rows.filter(r => !r.excluded);
            if (kept.reduce((n, r) => n + r.file.size, 0) > 256 * 1024 * 1024) { this.error = 'ZIP 导出总大小上限 256 MB，请分批整理；仍可导出完整报告'; return; }
            this.busy = true; this.exporting = true; this.error = ''; this.status = '正在打包保留副本';
            try {
                // Flat names with a unique prefix prevent same-name files overwriting one another.
                const blob = await zipFiles(kept.map((r, i) => ({ name: String(i + 1).padStart(5, '0') + '_' + r.file.name, blob: r.file })));
                downloadResult({ name: 'Hash去重保留副本.zip', blob }); this.status = `已导出 ${kept.length} 份副本，原文件未改动；ZIP 为平铺目录，序号对应报告中保留文件的顺序`;
            } catch (e) { this.error = e.message || '导出失败'; } finally { this.busy = false; this.exporting = false; }
        }
    }
};
</script>
<style scoped>
.hash-page-header { display: flex; align-items: center; flex-wrap: wrap; gap: 16px; margin-bottom: 18px; }
.hash-page-header .back-link { flex-shrink: 0; }
.hash-page-header h1 { margin: 0 0 6px; font-size: 22px; line-height: 1.3; }
.hash-page-header p { margin: 0; color: #8794a6; font-size: 12px; line-height: 1.6; }
.hash-control-panel, .hash-result-panel { min-width: 0; padding: 14px 16px; border: 1px solid #dfe6f0; border-radius: 18px; background: #fff; box-shadow: 0 12px 36px #1f29370a; }
.hash-result-panel { margin-top: 16px; }
.hash-toolbar { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin: 10px 0; font-size: 12px; color: #52657c; }
.hash-toolbar > label { display: inline-flex; align-items: center; gap: 6px; }
.hash-toolbar .el-button { margin-left: 0; border-radius: 6px; }
.hash-toolbar .el-select { width: 220px; max-width: 100%; }
.hash-toolbar .hash-algorithm-select { width: 110px; }
.hash-filter { width: 230px; max-width: 100%; }
.hash-action-divider { width: 1px; height: 20px; margin: 0 2px; background: #e5eaf3; }
.hash-help { margin: 8px 0 10px; color: #8794a6; font-size: 12px; line-height: 1.7; }
.hash-help summary { width: fit-content; cursor: pointer; }
.hash-note { margin: 6px 0; }
.hash-status { margin: 8px 0 0; color: #6b7d92; font-size: 12px; line-height: 1.7; overflow-wrap: anywhere; }
.hash-groups { display: grid; grid-template-columns: minmax(220px, 1fr) minmax(400px, 2fr); gap: 16px; }
.hash-detail { padding: 14px; margin-top: 14px; background: #f6f8fc; border-radius: 12px; overflow-wrap: anywhere; font-size: 13px; }
.hash-detail h3 { margin-top: 0; font-size: 15px; }
.hash-value { font-family: monospace; }
.hash-result-panel > .el-tabs ::v-deep .el-tabs__header { margin-bottom: 10px; }
.hash-result-panel p { color: #6b7d92; font-size: 12px; line-height: 1.7; }
.hash-dedup-page ::v-deep .el-checkbox { margin-right: 8px; }
.hash-dedup-page ::v-deep .el-checkbox__label { padding-left: 6px; font-size: 12px; }
.hash-control-panel ::v-deep .el-alert { padding: 6px 10px; border-radius: 8px; }
.hash-control-panel ::v-deep .el-alert__title { font-size: 12px; line-height: 1.6; }
.hash-dedup-page ::v-deep .el-table .hash-match td { background: #fff3cd; }
.hash-dedup-page ::v-deep .el-table .hash-excluded td { color: #8590a3; background: #f3f4f6; }
@media (max-width: 768px) {
    .hash-page-header { gap: 12px; }
    .hash-page-header h1 { font-size: 18px; }
    .hash-control-panel, .hash-result-panel { padding: 10px; border-radius: 14px; }
    .hash-groups { grid-template-columns: minmax(0, 1fr); }
    .hash-action-divider { display: none; }
}
</style>
