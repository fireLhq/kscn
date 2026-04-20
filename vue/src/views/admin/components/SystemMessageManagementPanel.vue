<template>
    <div class="system-message-panel">
        <section class="panel-card filter-card">
            <div class="panel-header compact">
                <div>
                    <h3>条件查询</h3>
                </div>
                <div class="header-actions">
                    <el-button size="small" @click="$emit('reset-filter')">重置</el-button>
                    <el-button type="primary" size="small" @click="$emit('search')">查询</el-button>
                    <el-button type="success" size="small" @click="$emit('open-create')">发布消息</el-button>
                </div>
            </div>

            <el-form :inline="false" :model="filters" label-position="top" class="filter-form" @submit.native.prevent>
                <div class="filter-grid">
                    <el-form-item label="关键词">
                        <el-input :value="filters.keyword" placeholder="按标题模糊查询" clearable @input="updateFilter('keyword', $event)" />
                    </el-form-item>
                    <el-form-item label="弹窗提醒">
                        <el-select :value="filters.isPopup" placeholder="全部" clearable @change="updateFilter('isPopup', $event)">
                            <el-option label="开启弹窗" :value="1" />
                            <el-option label="仅列表展示" :value="0" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="每页条数">
                        <el-select :value="pageSize" @change="$emit('update:page-size', $event)">
                            <el-option :value="10" label="10 / 页" />
                            <el-option :value="20" label="20 / 页" />
                            <el-option :value="30" label="30 / 页" />
                            <el-option :value="50" label="50 / 页" />
                        </el-select>
                    </el-form-item>
                </div>
            </el-form>
        </section>

        <section class="panel-card table-card">
            <div class="panel-header compact">
                <div>
                    <h3>系统消息</h3>
                </div>
                <el-tag size="small" effect="plain">共 {{ total }} 条</el-tag>
            </div>

            <el-table
                v-loading="loading"
                :data="tableData"
                stripe
                border
                size="small"
                :header-cell-style="headerCellStyle"
                class="message-table"
            >
                <el-table-column prop="id" label="ID" width="84" />
                <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
                <el-table-column label="内容" min-width="280" show-overflow-tooltip>
                    <template slot-scope="scope">
                        <div class="content-preview">{{ scope.row.content }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="弹窗" width="110">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.isPopup === 1 ? 'warning' : 'info'" size="mini">
                            {{ scope.row.isPopup === 1 ? '开启' : '关闭' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="弹窗时间" min-width="220">
                    <template slot-scope="scope">
                        <div class="popup-time-cell">
                            <div>开始：{{ formatDateTime(scope.row.popupStartTime) }}</div>
                            <div>结束：{{ formatDateTime(scope.row.popupEndTime) }}</div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="发布时间" min-width="170">
                    <template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template>
                </el-table-column>
                <el-table-column label="更新时间" min-width="170">
                    <template slot-scope="scope">{{ formatDateTime(scope.row.updateTime) }}</template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="170">
                    <template slot-scope="scope">
                        <div class="action-group">
                            <el-button type="text" size="mini" @click="$emit('open-edit', scope.row)">编辑</el-button>
                            <el-button type="text" size="mini" class="danger-btn" @click="$emit('remove', scope.row)">删除</el-button>
                        </div>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-wrap">
                <el-pagination
                    background
                    layout="total, sizes, prev, pager, next, jumper"
                    :current-page="currentPage"
                    :page-sizes="[10, 20, 30, 50]"
                    :page-size="pageSize"
                    :total="total"
                    @current-change="$emit('page-change', $event)"
                    @size-change="$emit('page-size-change', $event)"
                />
            </div>
        </section>
    </div>
</template>

<script>
export default {
    name: "SystemMessageManagementPanel",
    props: {
        filters: {
            type: Object,
            required: true,
        },
        tableData: {
            type: Array,
            default: () => [],
        },
        loading: {
            type: Boolean,
            default: false,
        },
        total: {
            type: Number,
            default: 0,
        },
        currentPage: {
            type: Number,
            default: 1,
        },
        pageSize: {
            type: Number,
            default: 10,
        },
    },
    methods: {
        updateFilter(key, value) {
            this.$emit("update-filter", { key, value });
        },
        formatDateTime(value) {
            if (!value) return "--";
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return "--";
            const pad = (num) => String(num).padStart(2, "0");
            return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
        },
        headerCellStyle() {
            return {
                background: "#f8fafc",
                color: "#334155",
                fontWeight: 600,
            };
        },
    },
};
</script>

<style scoped>
.system-message-panel {
    display: flex;
    flex-direction: column;
    gap: 18px;
    min-width: 0;
}

.panel-card {
    background: rgba(255, 255, 255, 0.96);
    border: 1px solid rgba(148, 163, 184, 0.18);
    border-radius: 20px;
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
    padding: 18px;
    min-width: 0;
}

.panel-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 14px;
}

.panel-header h3 {
    font-size: 18px;
    color: #111827;
    margin-bottom: 4px;
}

.header-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    justify-content: flex-end;
}

.filter-form,
.filter-grid,
.el-select {
    width: 100%;
}

.filter-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 0 14px;
}

.content-preview {
    line-height: 1.5;
    color: #334155;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    white-space: pre-wrap;
}

.popup-time-cell {
    line-height: 1.6;
    color: #475569;
}

.action-group {
    display: flex;
    align-items: center;
    gap: 8px;
}

.danger-btn {
    color: #dc2626;
}

.pagination-wrap {
    margin-top: 18px;
    display: flex;
    justify-content: flex-end;
}

@media (max-width: 1080px) {
    .filter-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

@media (max-width: 720px) {
    .filter-grid {
        grid-template-columns: 1fr;
    }

    .panel-header {
        flex-direction: column;
    }

    .pagination-wrap {
        justify-content: flex-start;
        overflow-x: auto;
    }
}
</style>

