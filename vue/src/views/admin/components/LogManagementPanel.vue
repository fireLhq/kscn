<template>
    <div class="log-panel">
        <section class="panel-card filter-card">
            <div class="panel-header compact">
                <div><h3>日志筛选</h3></div>
                <div class="header-actions">
                    <el-button size="small" @click="resetFilter">重置</el-button>
                    <el-button type="primary" size="small" @click="search">查询</el-button>
                </div>
            </div>
            <el-form :inline="false" :model="filters" label-position="top" class="filter-form" @submit.native.prevent>
                <div class="filter-grid">
                    <el-form-item label="事件类型">
                        <el-select :value="filters.eventType" clearable placeholder="全部类型" @change="updateFilter('eventType', $event)">
                            <el-option label="登录" value="LOGIN" />
                            <el-option label="用户" value="USER" />
                            <el-option label="文件" value="FILE" />
                            <el-option label="管理员" value="ADMIN" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="操作动作">
                        <el-input :value="filters.action" clearable placeholder="如 LOGIN、UPLOAD" @input="updateFilter('action', $event)" />
                    </el-form-item>
                    <el-form-item label="结果">
                        <el-select :value="filters.status" clearable placeholder="全部结果" @change="updateFilter('status', $event)">
                            <el-option label="成功" :value="1" />
                            <el-option label="失败" :value="0" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="关键词">
                        <el-input :value="filters.keyword" clearable placeholder="IP、资源、路径或备注" @input="updateFilter('keyword', $event)" />
                    </el-form-item>
                    <el-form-item label="每页条数">
                        <el-select :value="pageSize" @change="changePageSize">
                            <el-option :value="10" label="10 / 页" />
                            <el-option :value="20" label="20 / 页" />
                            <el-option :value="50" label="50 / 页" />
                        </el-select>
                    </el-form-item>
                </div>
            </el-form>
        </section>

        <section class="panel-card table-card">
            <div class="panel-header compact">
                <div><h3>日志管理</h3></div>
                <el-tag size="small" effect="plain">共 {{ total }} 条</el-tag>
            </div>
            <el-table v-loading="loading" :data="logs" stripe border size="small" :header-cell-style="headerCellStyle" class="log-table">
                <el-table-column prop="logId" label="日志ID" width="84" />
                <el-table-column prop="eventTime" label="时间" min-width="170" show-overflow-tooltip>
                    <template slot-scope="scope">{{ formatDateTime(scope.row.eventTime) }}</template>
                </el-table-column>
                <el-table-column prop="eventType" label="类型" width="110" />
                <el-table-column prop="action" label="动作" min-width="160" show-overflow-tooltip />
                <el-table-column prop="userId" label="用户ID" width="90">
                    <template slot-scope="scope">{{ scope.row.userId || '匿名' }}</template>
                </el-table-column>
                <el-table-column prop="ip" label="IP" min-width="145" show-overflow-tooltip />
                <el-table-column prop="resourceName" label="资源" min-width="180" show-overflow-tooltip />
                <el-table-column label="结果" width="90">
                    <template slot-scope="scope"><el-tag size="mini" :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '成功' : '失败' }}</el-tag></template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="170">
                    <template slot-scope="scope">
                        <div class="action-group">
                            <el-button type="text" size="mini" @click="openDetail(scope.row.logId)">详情</el-button>
                        </div>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination-wrap">
                <el-pagination background layout="total, sizes, prev, pager, next, jumper" :current-page="page" :page-sizes="[10, 20, 50]" :page-size="pageSize" :total="total" @current-change="changePage" @size-change="changePageSize" />
            </div>
        </section>

        <el-dialog title="日志详情" :visible.sync="detailVisible" width="760px" :close-on-click-modal="false">
            <div v-loading="detailLoading" class="detail-content" v-if="detail">
                <el-descriptions :column="2" border size="small">
                    <el-descriptions-item v-for="item in detailItems" :key="item.label" :label="item.label">{{ item.value }}</el-descriptions-item>
                    <el-descriptions-item v-if="remarkItem" :label="remarkItem.label" :span="2">
                        <span class="user-agent-value">{{ remarkItem.value }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item v-if="userAgentItem" :label="userAgentItem.label" :span="2">
                        <span class="user-agent-value">{{ userAgentItem.value }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item v-if="detailsJsonItem" :label="detailsJsonItem.label" :span="2">
                        <span class="user-agent-value">{{ detailsJsonItem.value }}</span>
                    </el-descriptions-item>
                </el-descriptions>
                <h4 v-if="detail.user">关联用户</h4>
                <pre v-if="detail.user" class="json-block">{{ prettyJson(detail.user) }}</pre>
                <h4 v-if="detail.resource">关联资源</h4>
                <pre v-if="detail.resource" class="json-block">{{ prettyJson(detail.resource) }}</pre>
            </div>
            <el-empty v-else-if="!detailLoading" description="暂无日志详情" />
        </el-dialog>
    </div>
</template>

<script>
import { getAdminLogs, getAdminLogDetail } from "@/api/system/login-log";

export default {
    name: "AdminLogManagementPanel",
    data() {
        return {
            filters: { eventType: "", action: "", status: null, keyword: "" },
            logs: [],
            loading: false,
            page: 1,
            pageSize: 10,
            total: 0,
            detailVisible: false,
            detailLoading: false,
            detail: null,
        };
    },
    computed: {
        detailItems() {
            const log = this.detail && this.detail.log;
            if (!log) return [];
            return [
                ["日志ID", log.logId], ["事件时间", this.formatDateTime(log.eventTime)], ["事件类型", log.eventType],
                ["操作动作", log.action], ["用户ID", log.userId || "匿名"],
                ["IP", log.ip], ["设备", log.deviceType],
                ["操作系统", log.os], ["浏览器", log.browser], ["HTTP方法", log.httpMethod], ["请求路径", log.requestPath],
                ["HTTP状态", log.httpStatus], ["资源类型", log.resourceType], ["资源ID", log.resourceId],
                ["资源名称", log.resourceName], ["结果", log.status === 1 ? "成功" : "失败"],
            ].map(([label, value]) => ({ label, value: value === null || value === undefined || value === "" ? "-" : value }));
        },
        remarkItem() {
            const log = this.detail && this.detail.log;
            if (!log) return null;
            const value = log.remark === null || log.remark === undefined || log.remark === "" ? "-" : log.remark;
            return { label: "备注", value };
        },
        userAgentItem() {
            const log = this.detail && this.detail.log;
            if (!log) return null;
            const value = log.userAgent === null || log.userAgent === undefined || log.userAgent === "" ? "-" : log.userAgent;
            return { label: "User-Agent", value };
        },
        detailsJsonItem() {
            const log = this.detail && this.detail.log;
            if (!log) return null;
            const value = log.detailsJson === null || log.detailsJson === undefined || log.detailsJson === "" ? "-" : log.detailsJson;
            return { label: "扩展详情 JSON", value };
        },
    },
    created() { this.fetchLogs(); },
    methods: {
        async fetchLogs() {
            this.loading = true;
            try {
                const res = await getAdminLogs({ page: this.page, size: this.pageSize, ...this.filters });
                if (res.data.code === 200) {
                    const data = res.data.data || {};
                    this.logs = data.records || [];
                    this.total = Number(data.total || 0);
                }
            } catch (error) { this.$message.error(error.response?.data?.message || "加载日志失败"); }
            finally { this.loading = false; }
        },
        search() { this.page = 1; this.fetchLogs(); },
        updateFilter(key, value) { this.filters = { ...this.filters, [key]: value }; },
        resetFilter() { this.filters = { eventType: "", action: "", status: null, keyword: "" }; this.page = 1; this.fetchLogs(); },
        changePage(page) { this.page = page; this.fetchLogs(); },
        changePageSize(size) { this.pageSize = Number(size); this.page = 1; this.fetchLogs(); },
        async openDetail(logId) {
            this.detailVisible = true; this.detailLoading = true; this.detail = null;
            try { const res = await getAdminLogDetail(logId); if (res.data.code === 200) this.detail = res.data.data; }
            catch (error) { this.$message.error(error.response?.data?.message || "加载日志详情失败"); }
            finally { this.detailLoading = false; }
        },
        formatDateTime(value) {
            if (!value) return "-";
            const date = new Date(value); if (Number.isNaN(date.getTime())) return "-";
            const pad = (num) => String(num).padStart(2, "0");
            return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
        },
        prettyJson(value) { return JSON.stringify(value, null, 2); },
        headerCellStyle() { return { background: '#f8fafc', color: '#334155', fontWeight: 600 }; },
    },
};
</script>

<style scoped>
.log-panel { display: flex; flex-direction: column; gap: 18px; min-width: 0; }
.panel-card { background: rgba(255, 255, 255, 0.96); border: 1px solid rgba(148, 163, 184, 0.18); border-radius: 20px; box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08); padding: 18px; min-width: 0; }
.panel-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; margin-bottom: 14px; }
.panel-header.compact { margin-bottom: 12px; }
.panel-header h3 { font-size: 18px; color: #111827; margin-bottom: 4px; }
.panel-subtitle { font-size: 13px; color: #64748b; }
.header-actions { display: flex; gap: 8px; flex-wrap: wrap; justify-content: flex-end; }
.filter-form, .filter-grid, .el-select { width: 100%; }
.filter-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0 14px; }
.pagination-wrap { margin-top: 18px; display: flex; justify-content: flex-end; }
.detail-content h4 { color:#334155; margin:18px 0 8px; }
.user-agent-value { display:block; white-space:normal; overflow-wrap:anywhere; word-break:break-word; line-height:1.5; }
.json-block { margin:0; padding:12px; background:#f8fafc; border-radius:8px; white-space:pre-wrap; word-break:break-all; max-height:240px; overflow:auto; font-size:12px; }
@media (max-width: 1080px) { .filter-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 720px) {
    .filter-grid { grid-template-columns: minmax(0, 1fr); }
    .panel-header { flex-direction: column; }
    .header-actions { justify-content: flex-start; }
    .pagination-wrap { justify-content: flex-start; overflow-x: auto; }
    .panel-card { border-radius: 16px; padding: 14px; }
}
</style>