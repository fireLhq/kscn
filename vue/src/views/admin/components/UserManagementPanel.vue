<template>
    <div class="user-panel">
        <section class="panel-card filter-card">
            <div class="panel-header compact">
                <div>
                    <h3>条件查询</h3>
                </div>
                <div class="header-actions">
                    <el-button size="small" @click="$emit('reset-filter')">重置</el-button>
                    <el-button type="primary" size="small" @click="$emit('search')">查询</el-button>
                    <el-button type="success" size="small" @click="$emit('open-create')">新增用户</el-button>
                </div>
            </div>

            <el-form :inline="false" :model="filters" label-position="top" class="filter-form" @submit.native.prevent>
                <div class="filter-grid">
                    <el-form-item label="用户ID">
                        <el-input :value="filters.userId" placeholder="精确查询" @input="updateFilter('userId', $event)" clearable />
                    </el-form-item>
                    <el-form-item label="用户名">
                        <el-input :value="filters.username" placeholder="支持模糊查询" @input="updateFilter('username', $event)" clearable />
                    </el-form-item>
                    <el-form-item label="邮箱">
                        <el-input :value="filters.email" placeholder="支持模糊查询" @input="updateFilter('email', $event)" clearable />
                    </el-form-item>
                    <el-form-item label="昵称">
                        <el-input :value="filters.nickname" placeholder="支持模糊查询" @input="updateFilter('nickname', $event)" clearable />
                    </el-form-item>
                    <el-form-item label="角色">
                        <el-select :value="filters.role" placeholder="全部角色" clearable @change="updateFilter('role', $event)">
                            <el-option label="管理员" :value="0" />
                            <el-option label="普通用户" :value="1" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="性别">
                        <el-select :value="filters.sex" placeholder="全部性别" clearable @change="updateFilter('sex', $event)">
                            <el-option label="男" :value="0" />
                            <el-option label="女" :value="1" />
                            <el-option label="不愿透露" :value="2" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="状态">
                        <el-select :value="filters.status" placeholder="全部状态" clearable @change="updateFilter('status', $event)">
                            <el-option label="正常" :value="1" />
                            <el-option label="禁用" :value="0" />
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
                    <h3>用户管理</h3>
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
                class="user-table"
            >
                <el-table-column prop="userId" label="ID" width="84" />
                <el-table-column label="头像" width="92">
                    <template slot-scope="scope">
                        <img :src="avatarUrl(scope.row.avatar)" :alt="scope.row.username" class="user-avatar" />
                    </template>
                </el-table-column>
                <el-table-column prop="username" label="用户名" min-width="130" show-overflow-tooltip />
                <el-table-column prop="email" label="邮箱" min-width="220" show-overflow-tooltip />
                <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip>
                    <template slot-scope="scope">{{ scope.row.nickname || '-' }}</template>
                </el-table-column>
                <el-table-column label="角色" width="100">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.role === 0 ? 'warning' : 'info'" size="mini">
                            {{ roleText(scope.row.role) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="性别" width="100">
                    <template slot-scope="scope">{{ sexText(scope.row.sex) }}</template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="mini">
                            {{ scope.row.status === 1 ? '正常' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="生日" min-width="120">
                    <template slot-scope="scope">{{ formatDate(scope.row.birthday) }}</template>
                </el-table-column>
                <el-table-column label="空间使用" min-width="140">
                    <template slot-scope="scope">
                        {{ formatFileSize(scope.row.usedSpace) }} / {{ formatFileSize(scope.row.totalSpace) }}
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" min-width="170">
                    <template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template>
                </el-table-column>
                <el-table-column label="更新时间" min-width="170">
                    <template slot-scope="scope">{{ formatDateTime(scope.row.updateTime) }}</template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="170">
                    <template slot-scope="scope">
                        <div class="action-group">
                            <el-button type="text" size="mini" @click="$emit('open-edit', scope.row)">编辑</el-button>
                            <el-button
                                type="text"
                                size="mini"
                                class="danger-btn"
                                :disabled="currentUserId === scope.row.userId"
                                @click="$emit('remove', scope.row)"
                            >
                                删除
                            </el-button>
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
import { getUserAvatarUrl } from "@/api/system/user";

export default {
    name: "AdminUserManagementPanel",
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
        currentUserId: {
            type: Number,
            default: null,
        },
    },
    methods: {
        updateFilter(key, value) {
            this.$emit('update-filter', { key, value });
        },
        roleText(value) {
            return value === 0 ? '管理员' : '普通用户';
        },
        sexText(value) {
            if (value === 0) return '男';
            if (value === 1) return '女';
            if (value === 2) return '不愿透露';
            return '-';
        },
        formatDate(value) {
            if (!value) return '--';
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return '--';
            const pad = (num) => String(num).padStart(2, '0');
            return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
        },
        formatDateTime(value) {
            if (!value) return '--';
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return '--';
            const pad = (num) => String(num).padStart(2, '0');
            return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
        },
        formatFileSize(value) {
            const size = Number(value || 0);
            if (!size) return '0 B';
            const units = ['B', 'KB', 'MB', 'GB', 'TB'];
            let index = 0;
            let temp = size;
            while (temp >= 1024 && index < units.length - 1) {
                temp /= 1024;
                index += 1;
            }
            return `${temp.toFixed(temp >= 100 || index === 0 ? 0 : temp >= 10 ? 1 : 2)} ${units[index]}`;
        },
        avatarUrl(filename) {
            return getUserAvatarUrl(filename, Date.now()) || require("@/assets/images/components/avatar/user.png");
        },
        headerCellStyle() {
            return {
                background: '#f8fafc',
                color: '#334155',
                fontWeight: 600,
            };
        },
    },
};
</script>

<style scoped>
.user-panel {
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

.panel-header.compact {
    margin-bottom: 12px;
}

.panel-header h3 {
    font-size: 18px;
    color: #111827;
    margin-bottom: 4px;
}

.panel-header p {
    font-size: 13px;
    color: #64748b;
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
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 0 14px;
}

.user-table {
    width: 100%;
}

.user-avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    object-fit: cover;
    display: block;
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.12);
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
    margin-top: 14px;
    display: flex;
    justify-content: flex-end;
    overflow-x: auto;
    overflow-y: hidden;
    padding-bottom: 2px;
}

@media (max-width: 1360px) {
    .filter-grid {
        grid-template-columns: repeat(3, minmax(0, 1fr));
    }
}

@media (max-width: 992px) {
    .filter-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .panel-header {
        flex-direction: column;
    }

    .header-actions {
        justify-content: flex-start;
    }
}

@media (max-width: 640px) {
    .filter-grid {
        grid-template-columns: minmax(0, 1fr);
    }

    .panel-card {
        border-radius: 16px;
        padding: 14px;
    }
}
</style>

