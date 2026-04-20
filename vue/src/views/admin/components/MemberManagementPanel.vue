<template>
    <div class="member-panel">
        <section class="panel-card filter-card">
            <div class="panel-header compact">
                <div>
                    <h3>条件查询</h3>
                </div>
                <div class="header-actions">
                    <el-button size="small" @click="$emit('reset-filter')">重置</el-button>
                    <el-button type="primary" size="small" @click="$emit('search')">查询</el-button>
                    <el-button type="success" size="small" @click="$emit('open-create')">新增成员</el-button>
                </div>
            </div>

            <el-form :inline="false" :model="filters" label-position="top" class="filter-form" @submit.native.prevent>
                <div class="filter-grid">
                    <el-form-item label="姓名">
                        <el-input :value="filters.name" placeholder="支持模糊查询" @input="updateFilter('name', $event)" clearable />
                    </el-form-item>
                    <el-form-item label="成员类型">
                        <el-select :value="filters.memberType" placeholder="全部类型" clearable @change="updateFilter('memberType', $event)">
                            <el-option label="管理员" :value="1" />
                            <el-option label="开发人员" :value="2" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="角色名称">
                        <el-input :value="filters.roleName" placeholder="支持模糊查询" @input="updateFilter('roleName', $event)" clearable />
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
                    <h3>成员管理</h3>
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
                class="member-table"
            >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column label="头像" width="96">
                    <template slot-scope="scope">
                        <div class="avatar-cell">
                            <img :src="memberAvatarUrl(scope.row.avatar)" :alt="scope.row.name" class="member-avatar" />
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="name" label="姓名" min-width="120" show-overflow-tooltip />
                <el-table-column label="成员类型" width="110">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.memberType === 1 ? 'warning' : 'success'" size="mini">
                            {{ memberTypeText(scope.row.memberType) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="roleName" label="角色名称" min-width="160" show-overflow-tooltip />
                <el-table-column prop="website" label="个人主页" min-width="220" show-overflow-tooltip>
                    <template slot-scope="scope">
                        <a v-if="scope.row.website" :href="scope.row.website" target="_blank" rel="noopener noreferrer" class="link-text">
                            {{ scope.row.website }}
                        </a>
                        <span v-else>-</span>
                    </template>
                </el-table-column>
                <el-table-column prop="sort" label="排序" width="90" />
                <el-table-column prop="intro" label="成员介绍" min-width="240" show-overflow-tooltip>
                    <template slot-scope="scope">{{ scope.row.intro || '-' }}</template>
                </el-table-column>
                <el-table-column label="更新时间" min-width="170">
                    <template slot-scope="scope">{{ formatDateTime(scope.row.updateTime) }}</template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="220">
                    <template slot-scope="scope">
                        <div class="action-group">
                            <el-button type="text" size="mini" @click="$emit('open-avatar', scope.row)">头像</el-button>
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
import { getMemberAvatarUrl } from "@/api/system/member";

export default {
    name: "AdminMemberManagementPanel",
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
        memberTypeText(value) {
            return value === 1 ? "管理员" : "开发人员";
        },
        memberAvatarUrl(filename) {
            return getMemberAvatarUrl(filename, Date.now()) || require("@/assets/images/components/avatar/user.png");
        },
        formatDateTime(value) {
            if (!value) return "--";
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return "--";
            const pad = (num) => String(num).padStart(2, "0");
            return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
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
.member-panel {
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

.avatar-cell {
    display: flex;
    justify-content: center;
}

.member-avatar {
    width: 52px;
    height: 52px;
    border-radius: 50%;
    object-fit: cover;
    box-shadow: 0 6px 18px rgba(15, 23, 42, 0.14);
}

.action-group {
    display: flex;
    align-items: center;
    gap: 8px;
}

.link-text {
    color: #2563eb;
    text-decoration: none;
}

.link-text:hover {
    text-decoration: underline;
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

