<template>
    <div class="dashboard-panel">
        <section class="stats-grid">
            <article v-for="item in statCards" :key="item.key" class="stat-card">
                <div class="stat-icon" :class="item.className">
                    <i :class="item.icon"></i>
                </div>
                <div class="stat-content">
                    <p class="stat-label">{{ item.label }}</p>
                    <h3 class="stat-value">{{ item.formatter(stats[item.key]) }}</h3>
                    <span class="stat-hint">{{ item.hint }}</span>
                </div>
            </article>
        </section>

        <section class="panel-card recent-card">
            <div class="panel-header">
                <div>
                    <h3>最近登录用户</h3>
                </div>
                <el-tag size="small" effect="dark">最近 {{ recentLogs.length }} 条</el-tag>
            </div>

            <el-table
                :data="recentLogs"
                stripe
                size="small"
                :header-cell-style="headerCellStyle"
                :cell-style="cellStyle"
                class="recent-table"
            >
                <el-table-column label="头像" width="90">
                    <template slot-scope="scope">
                        <img :src="avatarUrl(scope.row.avatar)" :alt="scope.row.username" class="user-avatar" />
                    </template>
                </el-table-column>
                <el-table-column prop="username" label="用户名" min-width="130" show-overflow-tooltip />
                <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip>
                    <template slot-scope="scope">
                        {{ scope.row.nickname || "-" }}
                    </template>
                </el-table-column>
                <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
                <el-table-column prop="ip" label="IP" min-width="140" show-overflow-tooltip />
                <el-table-column prop="deviceType" label="设备" width="90" />
                <el-table-column prop="os" label="系统" min-width="110" show-overflow-tooltip />
                <el-table-column prop="browser" label="浏览器" min-width="110" show-overflow-tooltip />
                <el-table-column label="登录时间" min-width="170">
                    <template slot-scope="scope">
                        {{ formatDateTime(scope.row.loginTime) }}
                    </template>
                </el-table-column>
            </el-table>
        </section>
    </div>
</template>

<script>
import { getUserAvatarUrl } from "@/api/system/user";

export default {
    name: "AdminDashboardPanel",
    props: {
        stats: {
            type: Object,
            default: () => ({}),
        },
        recentLogs: {
            type: Array,
            default: () => [],
        },
    },
    computed: {
        statCards() {
            return [
                {
                    key: "totalUsers",
                    label: "用户总数",
                    icon: "el-icon-user-solid",
                    className: "slate",
                    hint: "当前未删除用户",
                    formatter: (value) => value ?? 0,
                },
                {
                    key: "adminUsers",
                    label: "管理员数量",
                    icon: "el-icon-s-custom",
                    className: "amber",
                    hint: "具备后台权限账号",
                    formatter: (value) => value ?? 0,
                },
                {
                    key: "activeUsers",
                    label: "正常状态用户",
                    icon: "el-icon-success",
                    className: "emerald",
                    hint: "可正常登录的账号",
                    formatter: (value) => value ?? 0,
                },
                {
                    key: "disabledUsers",
                    label: "禁用用户",
                    icon: "el-icon-warning",
                    className: "rose",
                    hint: "当前已停用账号",
                    formatter: (value) => value ?? 0,
                },
                {
                    key: "usedSpace",
                    label: "已用空间",
                    icon: "el-icon-folder-opened",
                    className: "blue",
                    hint: "全部用户空间占用",
                    formatter: this.formatFileSize,
                },
                {
                    key: "totalSpace",
                    label: "总空间",
                    icon: "el-icon-coin",
                    className: "violet",
                    hint: "用户总分配空间",
                    formatter: this.formatFileSize,
                },
            ];
        },
    },
    methods: {
        formatDateTime(value) {
            if (!value) return "--";
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return "--";
            const pad = (num) => String(num).padStart(2, "0");
            return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
        },
        formatFileSize(value) {
            const size = Number(value || 0);
            if (!size) return "0 B";
            const units = ["B", "KB", "MB", "GB", "TB"];
            let index = 0;
            let temp = size;
            while (temp >= 1024 && index < units.length - 1) {
                temp /= 1024;
                index += 1;
            }
            return `${temp.toFixed(temp >= 100 || index === 0 ? 0 : temp >= 10 ? 1 : 2)} ${units[index]}`;
        },
        headerCellStyle() {
            return {
                background: "#eef2ff",
                color: "#374151",
                fontWeight: 600,
            };
        },
        cellStyle() {
            return {
                color: "#475569",
            };
        },
        avatarUrl(filename) {
            return getUserAvatarUrl(filename, Date.now()) || require("@/assets/images/components/avatar/user.png");
        },
    },
};
</script>

<style scoped>
.dashboard-panel {
    display: flex;
    flex-direction: column;
    gap: 20px;
    min-width: 0;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
}

.stat-card,
.panel-card {
    background: rgba(255, 255, 255, 0.94);
    border: 1px solid rgba(148, 163, 184, 0.18);
    border-radius: 20px;
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
}

.stat-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 18px;
    min-width: 0;
}

.stat-icon {
    width: 52px;
    height: 52px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 22px;
    flex-shrink: 0;
}

.stat-icon.slate { background: linear-gradient(135deg, #0f172a, #334155); }
.stat-icon.amber { background: linear-gradient(135deg, #d97706, #f59e0b); }
.stat-icon.emerald { background: linear-gradient(135deg, #047857, #10b981); }
.stat-icon.rose { background: linear-gradient(135deg, #be123c, #f43f5e); }
.stat-icon.blue { background: linear-gradient(135deg, #1d4ed8, #38bdf8); }
.stat-icon.violet { background: linear-gradient(135deg, #6d28d9, #8b5cf6); }

.stat-content {
    min-width: 0;
}

.stat-label {
    font-size: 13px;
    color: #64748b;
    margin-bottom: 6px;
}

.stat-value {
    font-size: 28px;
    line-height: 1.1;
    color: #111827;
    margin-bottom: 4px;
    word-break: break-all;
}

.stat-hint {
    font-size: 12px;
    color: #94a3b8;
}

.panel-card {
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
    color: #64748b;
    font-size: 13px;
}

.recent-table {
    width: 100%;
}

.user-avatar {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    object-fit: cover;
    display: block;
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.12);
}

@media (max-width: 1200px) {
    .stats-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

@media (max-width: 768px) {
    .stats-grid {
        grid-template-columns: minmax(0, 1fr);
    }

    .panel-card,
    .stat-card {
        border-radius: 16px;
    }

    .panel-header {
        flex-direction: column;
    }
}
</style>

