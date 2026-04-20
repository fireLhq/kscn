<template>
    <div class="admin-view">
        <div class="admin-shell">
            <aside class="admin-sidebar" :class="{ collapsed: isMobile && !drawerVisible }">
                <div class="sidebar-brand">
                    <div>
                        <span class="brand-tag">ADMIN</span>
                        <h1>管理中心</h1>
                    </div>
                    <el-button
                        v-if="isMobile"
                        type="text"
                        class="sidebar-close"
                        @click="drawerVisible = false"
                    >
                        <i class="el-icon-close"></i>
                    </el-button>
                </div>

                <nav class="sidebar-nav">
                    <button
                        v-for="item in menus"
                        :key="item.key"
                        class="nav-item"
                        :class="{ active: activeMenu === item.key }"
                        @click="switchMenu(item.key)"
                    >
                        <i :class="item.icon"></i>
                        <span>{{ item.label }}</span>
                    </button>
                </nav>
            </aside>

            <transition name="admin-mask-fade">
                <div v-if="isMobile && drawerVisible" class="admin-mask" @click="drawerVisible = false"></div>
            </transition>

            <main class="admin-content">
                <section class="admin-header-card">
                    <div class="header-left">
                        <el-button
                            v-if="isMobile"
                            circle
                            size="small"
                            class="mobile-menu-btn"
                            @click="drawerVisible = true"
                        >
                            <i class="el-icon-menu"></i>
                        </el-button>
                        <div>
                            <div class="location-text">后台管理 / {{ activeMenuLabel }}</div>
                            <h2>{{ pageTitle }}</h2>
                        </div>
                    </div>
                    <div class="header-right">
                        <el-button size="small" icon="el-icon-refresh" @click="refreshCurrent">刷新当前页</el-button>
                    </div>
                </section>

                <section class="admin-main-panel">
                    <DashboardPanel
                        v-if="activeMenu === 'dashboard'"
                        :stats="stats"
                        :recent-logs="recentLogs"
                    />

                    <UserManagementPanel
                        v-else-if="activeMenu === 'users'"
                        :filters="filters"
                        :table-data="tableData"
                        :loading="tableLoading"
                        :total="pagination.total"
                        :current-page="pagination.page"
                        :page-size="pagination.size"
                        :current-user-id="currentUserId"
                        @update-filter="updateFilter"
                        @update:page-size="handleQuickPageSize"
                        @search="handleSearch"
                        @reset-filter="resetFilter"
                        @open-create="openCreateDialog"
                        @open-edit="openEditDialog"
                        @remove="handleDelete"
                        @page-change="handlePageChange"
                        @page-size-change="handleSizeChange"
                    />

                    <MemberManagementPanel
                        v-else-if="activeMenu === 'members'"
                        :filters="memberFilters"
                        :table-data="memberTableData"
                        :loading="memberLoading"
                        :total="memberPagination.total"
                        :current-page="memberPagination.page"
                        :page-size="memberPagination.size"
                        @update-filter="updateMemberFilter"
                        @update:page-size="handleMemberQuickPageSize"
                        @search="handleMemberSearch"
                        @reset-filter="resetMemberFilter"
                        @open-create="openCreateMemberDialog"
                        @open-edit="openEditMemberDialog"
                        @open-avatar="openAvatarDialog"
                        @remove="handleDeleteMember"
                        @page-change="handleMemberPageChange"
                        @page-size-change="handleMemberSizeChange"
                    />

                    <SystemMessageManagementPanel
                        v-else
                        :filters="systemMessageFilters"
                        :table-data="systemMessageTableData"
                        :loading="systemMessageLoading"
                        :total="systemMessagePagination.total"
                        :current-page="systemMessagePagination.page"
                        :page-size="systemMessagePagination.size"
                        @update-filter="updateSystemMessageFilter"
                        @update:page-size="handleSystemMessageQuickPageSize"
                        @search="handleSystemMessageSearch"
                        @reset-filter="resetSystemMessageFilter"
                        @open-create="openCreateSystemMessageDialog"
                        @open-edit="openEditSystemMessageDialog"
                        @remove="handleDeleteSystemMessage"
                        @page-change="handleSystemMessagePageChange"
                        @page-size-change="handleSystemMessageSizeChange"
                    />
                </section>
            </main>
        </div>

        <el-dialog
            :title="dialogMode === 'create' ? '新增用户' : '编辑用户'"
            :visible.sync="dialogVisible"
            width="560px"
            :close-on-click-modal="false"
        >
            <el-form ref="userForm" :model="userForm" :rules="formRules" label-width="90px">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model.trim="userForm.username" maxlength="16" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model.trim="userForm.email" />
                </el-form-item>
                <el-form-item v-if="dialogMode === 'create'" label="密码" prop="password">
                    <el-input v-model="userForm.password" type="password" show-password />
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                    <el-input v-model.trim="userForm.nickname" maxlength="10" />
                </el-form-item>
                <el-form-item v-if="dialogMode === 'edit'" label="头像">
                    <div class="user-avatar-setting">
                        <img :src="userFormAvatarUrl" alt="用户头像" class="dialog-user-avatar" />
                        <el-button size="small" @click="handleResetDefaultAvatar">设为默认头像</el-button>
                    </div>
                </el-form-item>
                <el-form-item v-if="dialogMode === 'edit'" label="空间修正">
                    <el-button
                        size="small"
                        type="warning"
                        plain
                        :loading="recalculateUsedSpaceLoading"
                        @click="handleRecalculateUsedSpace"
                    >
                        重新计算已用空间
                    </el-button>
                    <div class="form-tip">用于修正历史累加异常导致的空间统计错误，执行后会按该用户当前全部文件重新统计。</div>
                </el-form-item>
                <el-form-item label="角色" prop="role">
                    <el-select v-model="userForm.role" style="width: 100%">
                        <el-option label="管理员" :value="0" />
                        <el-option label="普通用户" :value="1" />
                    </el-select>
                </el-form-item>
                <el-form-item label="性别" prop="sex">
                    <el-select v-model="userForm.sex" clearable style="width: 100%">
                        <el-option label="男" :value="0" />
                        <el-option label="女" :value="1" />
                        <el-option label="不愿透露" :value="2" />
                    </el-select>
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-select v-model="userForm.status" style="width: 100%">
                        <el-option label="正常" :value="1" />
                        <el-option label="禁用" :value="0" />
                    </el-select>
                </el-form-item>
                <el-form-item label="生日" prop="birthday">
                    <el-date-picker
                        v-model="userForm.birthday"
                        type="date"
                        value-format="yyyy-MM-dd"
                        format="yyyy-MM-dd"
                        style="width: 100%"
                        placeholder="选择生日"
                    />
                </el-form-item>
                <el-form-item label="个性签名" prop="synopsis">
                    <el-input v-model.trim="userForm.synopsis" type="textarea" :rows="3" maxlength="125" show-word-limit />
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitLoading" @click="submitUserForm">保存</el-button>
            </span>
        </el-dialog>

        <el-dialog
            :title="memberDialogMode === 'create' ? '新增成员' : '编辑成员'"
            :visible.sync="memberDialogVisible"
            width="620px"
            :close-on-click-modal="false"
        >
            <el-form ref="memberForm" :model="memberForm" :rules="memberRules" label-width="90px">
                <el-form-item label="姓名" prop="name">
                    <el-input v-model.trim="memberForm.name" maxlength="50" />
                </el-form-item>
                <el-form-item label="成员类型" prop="memberType">
                    <el-select v-model="memberForm.memberType" style="width: 100%">
                        <el-option label="管理员" :value="1" />
                        <el-option label="开发人员" :value="2" />
                    </el-select>
                </el-form-item>
                <el-form-item label="角色名称" prop="roleName">
                    <el-input v-model.trim="memberForm.roleName" maxlength="100" />
                </el-form-item>
                <el-form-item label="个人主页" prop="website">
                    <el-input v-model.trim="memberForm.website" maxlength="500" />
                </el-form-item>
                <el-form-item label="排序" prop="sort">
                    <el-input-number v-model="memberForm.sort" :min="0" :max="9999" controls-position="right" style="width: 100%" />
                </el-form-item>
                <el-form-item label="成员介绍" prop="intro">
                    <el-input v-model.trim="memberForm.intro" type="textarea" :rows="4" maxlength="500" show-word-limit />
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="memberDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="memberSubmitLoading" @click="submitMemberForm">保存</el-button>
            </span>
        </el-dialog>

        <el-dialog
            title="系统消息"
            :visible.sync="systemMessageDialogVisible"
            width="720px"
            :close-on-click-modal="false"
        >
            <el-form ref="systemMessageForm" :model="systemMessageForm" :rules="systemMessageRules" label-width="100px">
                <el-form-item label="消息标题" prop="title">
                    <el-input v-model.trim="systemMessageForm.title" maxlength="100" />
                </el-form-item>
                <el-form-item label="消息内容" prop="content">
                    <el-input v-model.trim="systemMessageForm.content" type="textarea" :rows="8" maxlength="5000" show-word-limit />
                </el-form-item>
                <el-form-item label="弹窗提醒" prop="isPopup">
                    <el-switch
                        v-model="systemMessageForm.isPopup"
                        :active-value="1"
                        :inactive-value="0"
                    />
                </el-form-item>
                <template v-if="systemMessageForm.isPopup === 1">
                    <el-form-item label="开始时间" prop="popupStartTime">
                        <el-date-picker
                            v-model="systemMessageForm.popupStartTime"
                            type="datetime"
                            value-format="yyyy-MM-dd HH:mm:ss"
                            format="yyyy-MM-dd HH:mm:ss"
                            style="width: 100%"
                            placeholder="不填则立即生效"
                        />
                    </el-form-item>
                    <el-form-item label="结束时间" prop="popupEndTime">
                        <el-date-picker
                            v-model="systemMessageForm.popupEndTime"
                            type="datetime"
                            value-format="yyyy-MM-dd HH:mm:ss"
                            format="yyyy-MM-dd HH:mm:ss"
                            style="width: 100%"
                            placeholder="不填则长期有效"
                        />
                    </el-form-item>
                </template>
            </el-form>
            <span slot="footer">
                <el-button @click="systemMessageDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="systemMessageSubmitLoading" @click="submitSystemMessageForm">保存</el-button>
            </span>
        </el-dialog>

        <el-dialog
            title="成员头像裁剪"
            :visible.sync="avatarDialogVisible"
            width="720px"
            :close-on-click-modal="false"
            @close="cleanupMemberCropImage"
        >
            <div class="crop-wrapper" v-if="memberCropImageUrl">
                <vue-cropper
                    ref="memberAvatarCropper"
                    :img="memberCropImageUrl"
                    :autoCrop="true"
                    :fixed="true"
                    :fixedNumber="[1, 1]"
                    :autoCropWidth="280"
                    :autoCropHeight="280"
                    :centerBox="true"
                    :canMove="true"
                    :canMoveBox="true"
                    outputType="png"
                />
            </div>
            <div class="upload-trigger">
                <el-upload
                    :show-file-list="false"
                    :before-upload="handleMemberAvatarBeforeUpload"
                    accept="image/*"
                    action="#"
                >
                    <el-button type="primary">选择头像</el-button>
                </el-upload>
                <span class="upload-tip">管理员上传时先裁成正方形，后端统一压缩为 256 × 256 PNG</span>
            </div>
            <span slot="footer">
                <el-button @click="avatarDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="avatarSubmitting" :disabled="!memberCropImageUrl" @click="submitMemberAvatar">上传头像</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import { VueCropper } from "vue-cropper";
import DashboardPanel from "@/views/admin/components/DashboardPanel.vue";
import UserManagementPanel from "@/views/admin/components/UserManagementPanel.vue";
import MemberManagementPanel from "@/views/admin/components/MemberManagementPanel.vue";
import SystemMessageManagementPanel from "@/views/admin/components/SystemMessageManagementPanel.vue";
import { getAdminUserPage, getAdminUserStats, postAdminUser, putAdminUser, putAdminUserDefaultAvatar, putAdminUserRecalculateUsedSpace, deleteAdminUser } from "@/api/admin/user";
import { getRecentLogins } from "@/api/system/login-log";
import { getUserProfile, getUserAvatarUrl } from "@/api/system/user";
import { getAdminMemberPage, postMember, putMember, deleteMember, postMemberAvatar } from "@/api/admin/member";
import { getAdminSystemMessagePage, postSystemMessage, putSystemMessage, deleteSystemMessage } from "@/api/admin/system-message";

const createDefaultFilters = () => ({
    userId: "",
    username: "",
    email: "",
    nickname: "",
    role: null,
    sex: null,
    status: null,
});

const createDefaultUserForm = () => ({
    userId: null,
    username: "",
    email: "",
    password: "",
    nickname: "",
    avatar: "",
    role: 1,
    sex: null,
    birthday: "",
    synopsis: "",
    status: 1,
});

const createDefaultMemberFilters = () => ({
    name: "",
    memberType: null,
    roleName: "",
});

const createDefaultMemberForm = () => ({
    id: null,
    name: "",
    memberType: 2,
    roleName: "",
    intro: "",
    website: "",
    sort: 0,
});

const createDefaultSystemMessageFilters = () => ({
    keyword: "",
    isPopup: null,
});

const createDefaultSystemMessageForm = () => ({
    id: null,
    title: "",
    content: "",
    isPopup: 0,
    popupStartTime: "",
    popupEndTime: "",
});

export default {
    name: "AdminView",
    components: {
        VueCropper,
        DashboardPanel,
        UserManagementPanel,
        MemberManagementPanel,
        SystemMessageManagementPanel,
    },
    data() {
        const usernameValidator = (rule, value, callback) => {
            if (!value) return callback();
            const ok = /^[a-zA-Z_][a-zA-Z0-9_]{3,15}$/.test(value);
            callback(ok ? undefined : new Error("4-16位，字母/数字/下划线，不能数字开头"));
        };
        const popupTimeValidator = (rule, value, callback) => {
            if (this.systemMessageForm.isPopup !== 1) return callback();
            const { popupStartTime, popupEndTime } = this.systemMessageForm;
            if (popupStartTime && popupEndTime && new Date(popupStartTime).getTime() > new Date(popupEndTime).getTime()) {
                callback(new Error("开始时间不能晚于结束时间"));
                return;
            }
            callback();
        };

        return {
            menus: [
                { key: "dashboard", label: "仪表盘", icon: "el-icon-data-analysis" },
                { key: "users", label: "用户管理", icon: "el-icon-user-solid" },
                { key: "members", label: "成员管理", icon: "el-icon-s-custom" },
                { key: "systemMessages", label: "系统消息", icon: "el-icon-message-solid" },
            ],
            activeMenu: "dashboard",
            drawerVisible: false,
            isMobile: false,
            currentUserId: null,
            stats: {
                totalUsers: 0,
                adminUsers: 0,
                activeUsers: 0,
                disabledUsers: 0,
                totalSpace: 0,
                usedSpace: 0,
            },
            recentLogs: [],
            filters: createDefaultFilters(),
            tableData: [],
            tableLoading: false,
            submitLoading: false,
            recalculateUsedSpaceLoading: false,
            pagination: {
                page: 1,
                size: 10,
                total: 0,
            },
            dialogVisible: false,
            dialogMode: "create",
            userForm: createDefaultUserForm(),
            formRules: {
                username: [{ validator: usernameValidator, trigger: "blur" }],
                email: [
                    { required: true, message: "请输入邮箱", trigger: "blur" },
                    { type: "email", message: "邮箱格式不正确", trigger: "blur" },
                ],
                password: [
                    { required: true, message: "请输入密码", trigger: "blur" },
                    { min: 6, message: "密码不能少于6位", trigger: "blur" },
                ],
                nickname: [{ max: 10, message: "昵称不能超过10个字符", trigger: "blur" }],
                role: [{ required: true, message: "请选择角色", trigger: "change" }],
                status: [{ required: true, message: "请选择状态", trigger: "change" }],
                synopsis: [{ max: 125, message: "签名不能超过125个字符", trigger: "blur" }],
            },
            memberFilters: createDefaultMemberFilters(),
            memberTableData: [],
            memberLoading: false,
            memberPagination: {
                page: 1,
                size: 10,
                total: 0,
            },
            memberDialogVisible: false,
            memberDialogMode: "create",
            memberSubmitLoading: false,
            memberForm: createDefaultMemberForm(),
            memberRules: {
                name: [
                    { required: true, message: "请输入成员姓名", trigger: "blur" },
                    { max: 50, message: "姓名长度不能超过50", trigger: "blur" },
                ],
                memberType: [{ required: true, message: "请选择成员类型", trigger: "change" }],
                roleName: [
                    { required: true, message: "请输入角色名称", trigger: "blur" },
                    { max: 100, message: "角色名称长度不能超过100", trigger: "blur" },
                ],
                intro: [{ max: 500, message: "成员介绍不能超过500个字符", trigger: "blur" }],
                website: [{ max: 500, message: "个人主页长度不能超过500", trigger: "blur" }],
            },
            systemMessageFilters: createDefaultSystemMessageFilters(),
            systemMessageTableData: [],
            systemMessageLoading: false,
            systemMessagePagination: {
                page: 1,
                size: 10,
                total: 0,
            },
            systemMessageDialogVisible: false,
            systemMessageDialogMode: "create",
            systemMessageSubmitLoading: false,
            systemMessageForm: createDefaultSystemMessageForm(),
            systemMessageRules: {
                title: [
                    { required: true, message: "请输入消息标题", trigger: "blur" },
                    { max: 100, message: "标题长度不能超过100个字符", trigger: "blur" },
                ],
                content: [
                    { required: true, message: "请输入消息内容", trigger: "blur" },
                    { max: 5000, message: "内容长度不能超过5000个字符", trigger: "blur" },
                ],
                popupStartTime: [{ validator: popupTimeValidator, trigger: "change" }],
                popupEndTime: [{ validator: popupTimeValidator, trigger: "change" }],
            },
            avatarDialogVisible: false,
            avatarSubmitting: false,
            selectedMemberId: null,
            memberCropImageUrl: "",
        };
    },
    computed: {
        activeMenuLabel() {
            return this.menus.find((item) => item.key === this.activeMenu)?.label || "-";
        },
        pageTitle() {
            if (this.activeMenu === "dashboard") return "仪表盘";
            if (this.activeMenu === "users") return "用户管理";
            if (this.activeMenu === "members") return "成员管理";
            return "系统消息";
        },
        userFormAvatarUrl() {
            return getUserAvatarUrl(this.userForm.avatar, Date.now()) || require("@/assets/images/components/avatar/user.png");
        },
    },
    async created() {
        await this.bootstrapPage();
    },
    mounted() {
        this.syncViewport();
        window.addEventListener("resize", this.syncViewport);
    },
    beforeDestroy() {
        window.removeEventListener("resize", this.syncViewport);
        this.cleanupMemberCropImage();
    },
    methods: {
        async bootstrapPage() {
            await Promise.all([this.fetchCurrentUser(), this.fetchStats(), this.fetchRecentLogs()]);
        },
        async fetchCurrentUser() {
            const currentUser = this.$store.state.currentUser;
            if (currentUser && currentUser.userId) {
                this.currentUserId = currentUser.userId;
                return;
            }
            try {
                const response = await getUserProfile();
                if (response.data && response.data.code === 200) {
                    this.currentUserId = response.data.data.userId;
                    this.$store.commit("setCurrentUser", response.data.data);
                }
            } catch (error) {
                this.currentUserId = null;
            }
        },
        syncViewport() {
            this.isMobile = window.innerWidth <= 900;
            if (!this.isMobile) {
                this.drawerVisible = false;
            }
        },
        switchMenu(key) {
            this.activeMenu = key;
            if (this.isMobile) this.drawerVisible = false;
            if (key === "users" && !this.tableData.length) {
                this.fetchUserTable();
            }
            if (key === "members" && !this.memberTableData.length) {
                this.fetchMemberTable();
            }
            if (key === "systemMessages" && !this.systemMessageTableData.length) {
                this.fetchSystemMessageTable();
            }
            if (key === "dashboard") {
                this.fetchStats();
                this.fetchRecentLogs();
            }
        },
        refreshCurrent() {
            if (this.activeMenu === "dashboard") {
                this.fetchStats();
                this.fetchRecentLogs();
                return;
            }
            if (this.activeMenu === "users") {
                this.fetchUserTable();
                return;
            }
            if (this.activeMenu === "members") {
                this.fetchMemberTable();
                return;
            }
            this.fetchSystemMessageTable();
        },
        async fetchStats() {
            try {
                const res = await getAdminUserStats();
                if (res.data.code === 200) {
                    this.stats = {
                        ...this.stats,
                        ...res.data.data,
                    };
                }
            } catch (error) {
                this.$message.error(error.response?.data?.message || "加载统计信息失败");
            }
        },
        async fetchRecentLogs() {
            try {
                const res = await getRecentLogins({ limit: 8 });
                if (res.data.code === 200) {
                    this.recentLogs = Array.isArray(res.data.data) ? res.data.data : [];
                }
            } catch (error) {
                this.recentLogs = [];
                this.$message.error(error.response?.data?.message || "加载最近登录信息失败");
            }
        },
        buildQueryParams() {
            const params = {
                page: this.pagination.page,
                size: this.pagination.size,
            };
            Object.keys(this.filters).forEach((key) => {
                const value = this.filters[key];
                if (value !== "" && value !== null && value !== undefined) {
                    params[key] = value;
                }
            });
            return params;
        },
        async fetchUserTable() {
            this.tableLoading = true;
            try {
                const res = await getAdminUserPage(this.buildQueryParams());
                if (res.data.code === 200) {
                    const pageData = res.data.data || {};
                    this.tableData = pageData.records || [];
                    this.pagination.total = Number(pageData.total || 0);
                    this.pagination.page = Number(pageData.current || this.pagination.page);
                    this.pagination.size = Number(pageData.size || this.pagination.size);
                }
            } catch (error) {
                this.$message.error(error.response?.data?.message || "加载用户列表失败");
            } finally {
                this.tableLoading = false;
            }
        },
        updateFilter({ key, value }) {
            this.$set(this.filters, key, value);
        },
        handleQuickPageSize(value) {
            this.pagination.size = value;
        },
        handleSearch() {
            this.pagination.page = 1;
            this.fetchUserTable();
        },
        resetFilter() {
            this.filters = createDefaultFilters();
            this.pagination.page = 1;
            this.pagination.size = 10;
            this.fetchUserTable();
        },
        handlePageChange(page) {
            this.pagination.page = page;
            this.fetchUserTable();
        },
        handleSizeChange(size) {
            this.pagination.size = size;
            this.pagination.page = 1;
            this.fetchUserTable();
        },
        openCreateDialog() {
            this.dialogMode = "create";
            this.userForm = createDefaultUserForm();
            this.dialogVisible = true;
            this.$nextTick(() => this.$refs.userForm && this.$refs.userForm.clearValidate());
        },
        openEditDialog(row) {
            this.dialogMode = "edit";
            this.userForm = {
                userId: row.userId,
                username: row.username || "",
                email: row.email || "",
                password: "",
                nickname: row.nickname || "",
                avatar: row.avatar || "",
                role: row.role,
                sex: row.sex,
                birthday: row.birthday || "",
                synopsis: row.synopsis || "",
                status: row.status,
            };
            this.dialogVisible = true;
            this.$nextTick(() => this.$refs.userForm && this.$refs.userForm.clearValidate());
        },
        submitUserForm() {
            this.$refs.userForm.validate(async (valid) => {
                if (!valid) return;
                this.submitLoading = true;
                try {
                    const payload = {
                        username: this.userForm.username || null,
                        email: this.userForm.email,
                        nickname: this.userForm.nickname || null,
                        role: this.userForm.role,
                        sex: this.userForm.sex,
                        birthday: this.userForm.birthday || null,
                        synopsis: this.userForm.synopsis || null,
                        status: this.userForm.status,
                    };

                    if (this.dialogMode === "create") {
                        payload.password = this.userForm.password;
                        const response = await postAdminUser(payload);
                        this.$message.success(response.data.message || "用户新增成功");
                    } else {
                        const response = await putAdminUser(this.userForm.userId, payload);
                        this.$message.success(response.data.message || "用户更新成功");
                    }

                    this.dialogVisible = false;
                    this.recalculateUsedSpaceLoading = false;
                    await Promise.all([this.fetchUserTable(), this.fetchStats(), this.fetchRecentLogs()]);
                } catch (error) {
                    this.$message.error(error.response?.data?.message || "保存失败");
                } finally {
                    this.submitLoading = false;
                }
            });
        },
        async handleResetDefaultAvatar() {
            if (!this.userForm.userId) return;
            try {
                const response = await putAdminUserDefaultAvatar(this.userForm.userId);
                this.userForm.avatar = "default_avatar.png";
                this.$message.success(response.data.message || "已设为默认头像");
                await Promise.all([this.fetchUserTable(), this.fetchRecentLogs()]);
            } catch (error) {
                this.$message.error(error.response?.data?.message || "设置默认头像失败");
            }
        },
        async handleRecalculateUsedSpace() {
            if (!this.userForm.userId) return;
            this.recalculateUsedSpaceLoading = true;
            try {
                const response = await putAdminUserRecalculateUsedSpace(this.userForm.userId);
                this.$message.success(response.data.message || "已重新计算用户已用空间");
                await Promise.all([this.fetchUserTable(), this.fetchStats()]);
                const currentRow = this.tableData.find((item) => item.userId === this.userForm.userId);
                if (currentRow) {
                    this.userForm = {
                        ...this.userForm,
                        avatar: currentRow.avatar || this.userForm.avatar,
                    };
                }
            } catch (error) {
                this.$message.error(error.response?.data?.message || "重新计算失败");
            } finally {
                this.recalculateUsedSpaceLoading = false;
            }
        },
        handleDelete(row) {
            this.$confirm(`确认删除用户 ${row.username || row.email} 吗？`, "删除提示", {
                type: "warning",
                confirmButtonText: "确认删除",
                cancelButtonText: "取消",
            }).then(async () => {
                try {
                    const response = await deleteAdminUser(row.userId);
                    this.$message.success(response.data.message || "删除成功");
                    if (this.tableData.length === 1 && this.pagination.page > 1) {
                        this.pagination.page -= 1;
                    }
                    await Promise.all([this.fetchUserTable(), this.fetchStats(), this.fetchRecentLogs()]);
                } catch (error) {
                    this.$message.error(error.response?.data?.message || "删除失败");
                }
            }).catch(() => {});
        },
        buildMemberQueryParams() {
            const params = {
                page: this.memberPagination.page,
                size: this.memberPagination.size,
            };
            Object.keys(this.memberFilters).forEach((key) => {
                const value = this.memberFilters[key];
                if (value !== "" && value !== null && value !== undefined) {
                    params[key] = value;
                }
            });
            return params;
        },
        async fetchMemberTable() {
            this.memberLoading = true;
            try {
                const res = await getAdminMemberPage(this.buildMemberQueryParams());
                if (res.data.code === 200) {
                    const pageData = res.data.data || {};
                    this.memberTableData = pageData.records || [];
                    this.memberPagination.total = Number(pageData.total || 0);
                    this.memberPagination.page = Number(pageData.current || this.memberPagination.page);
                    this.memberPagination.size = Number(pageData.size || this.memberPagination.size);
                }
            } catch (error) {
                this.$message.error(error.response?.data?.message || "加载成员列表失败");
            } finally {
                this.memberLoading = false;
            }
        },
        updateMemberFilter({ key, value }) {
            this.$set(this.memberFilters, key, value);
        },
        handleMemberQuickPageSize(value) {
            this.memberPagination.size = value;
        },
        handleMemberSearch() {
            this.memberPagination.page = 1;
            this.fetchMemberTable();
        },
        resetMemberFilter() {
            this.memberFilters = createDefaultMemberFilters();
            this.memberPagination.page = 1;
            this.memberPagination.size = 10;
            this.fetchMemberTable();
        },
        handleMemberPageChange(page) {
            this.memberPagination.page = page;
            this.fetchMemberTable();
        },
        handleMemberSizeChange(size) {
            this.memberPagination.size = size;
            this.memberPagination.page = 1;
            this.fetchMemberTable();
        },
        openCreateMemberDialog() {
            this.memberDialogMode = "create";
            this.memberForm = createDefaultMemberForm();
            this.memberDialogVisible = true;
            this.$nextTick(() => this.$refs.memberForm && this.$refs.memberForm.clearValidate());
        },
        openEditMemberDialog(row) {
            this.memberDialogMode = "edit";
            this.memberForm = {
                id: row.id,
                name: row.name || "",
                memberType: row.memberType,
                roleName: row.roleName || "",
                intro: row.intro || "",
                website: row.website || "",
                sort: row.sort ?? 0,
            };
            this.memberDialogVisible = true;
            this.$nextTick(() => this.$refs.memberForm && this.$refs.memberForm.clearValidate());
        },
        submitMemberForm() {
            this.$refs.memberForm.validate(async (valid) => {
                if (!valid) return;
                this.memberSubmitLoading = true;
                try {
                    const payload = {
                        name: this.memberForm.name,
                        memberType: this.memberForm.memberType,
                        roleName: this.memberForm.roleName,
                        intro: this.memberForm.intro || null,
                        website: this.memberForm.website || null,
                        sort: this.memberForm.sort ?? 0,
                    };
                    if (this.memberDialogMode === "create") {
                        const response = await postMember(payload);
                        this.$message.success(response.data.message || "成员新增成功");
                    } else {
                        const response = await putMember(this.memberForm.id, payload);
                        this.$message.success(response.data.message || "成员更新成功");
                    }
                    this.memberDialogVisible = false;
                    await this.fetchMemberTable();
                } catch (error) {
                    this.$message.error(error.response?.data?.message || "保存成员失败");
                } finally {
                    this.memberSubmitLoading = false;
                }
            });
        },
        handleDeleteMember(row) {
            this.$confirm(`确认删除成员 ${row.name} 吗？`, "删除提示", {
                type: "warning",
                confirmButtonText: "确认删除",
                cancelButtonText: "取消",
            }).then(async () => {
                try {
                    const response = await deleteMember(row.id);
                    this.$message.success(response.data.message || "删除成功");
                    if (this.memberTableData.length === 1 && this.memberPagination.page > 1) {
                        this.memberPagination.page -= 1;
                    }
                    await this.fetchMemberTable();
                } catch (error) {
                    this.$message.error(error.response?.data?.message || "删除成员失败");
                }
            }).catch(() => {});
        },
        buildSystemMessageQueryParams() {
            const params = {
                page: this.systemMessagePagination.page,
                size: this.systemMessagePagination.size,
            };
            Object.keys(this.systemMessageFilters).forEach((key) => {
                const value = this.systemMessageFilters[key];
                if (value !== "" && value !== null && value !== undefined) {
                    params[key] = value;
                }
            });
            return params;
        },
        async fetchSystemMessageTable() {
            this.systemMessageLoading = true;
            try {
                const res = await getAdminSystemMessagePage(this.buildSystemMessageQueryParams());
                if (res.data.code === 200) {
                    const pageData = res.data.data || {};
                    this.systemMessageTableData = pageData.records || [];
                    this.systemMessagePagination.total = Number(pageData.total || 0);
                    this.systemMessagePagination.page = Number(pageData.current || this.systemMessagePagination.page);
                    this.systemMessagePagination.size = Number(pageData.size || this.systemMessagePagination.size);
                }
            } catch (error) {
                this.$message.error(error.response?.data?.message || "加载系统消息失败");
            } finally {
                this.systemMessageLoading = false;
            }
        },
        updateSystemMessageFilter({ key, value }) {
            this.$set(this.systemMessageFilters, key, value);
        },
        handleSystemMessageQuickPageSize(value) {
            this.systemMessagePagination.size = value;
        },
        handleSystemMessageSearch() {
            this.systemMessagePagination.page = 1;
            this.fetchSystemMessageTable();
        },
        resetSystemMessageFilter() {
            this.systemMessageFilters = createDefaultSystemMessageFilters();
            this.systemMessagePagination.page = 1;
            this.systemMessagePagination.size = 10;
            this.fetchSystemMessageTable();
        },
        handleSystemMessagePageChange(page) {
            this.systemMessagePagination.page = page;
            this.fetchSystemMessageTable();
        },
        handleSystemMessageSizeChange(size) {
            this.systemMessagePagination.size = size;
            this.systemMessagePagination.page = 1;
            this.fetchSystemMessageTable();
        },
        openCreateSystemMessageDialog() {
            this.systemMessageDialogMode = "create";
            this.systemMessageForm = createDefaultSystemMessageForm();
            this.systemMessageDialogVisible = true;
            this.$nextTick(() => this.$refs.systemMessageForm && this.$refs.systemMessageForm.clearValidate());
        },
        openEditSystemMessageDialog(row) {
            this.systemMessageDialogMode = "edit";
            this.systemMessageForm = {
                id: row.id,
                title: row.title || "",
                content: row.content || "",
                isPopup: row.isPopup ?? 0,
                popupStartTime: row.popupStartTime || "",
                popupEndTime: row.popupEndTime || "",
            };
            this.systemMessageDialogVisible = true;
            this.$nextTick(() => this.$refs.systemMessageForm && this.$refs.systemMessageForm.clearValidate());
        },
        submitSystemMessageForm() {
            this.$refs.systemMessageForm.validate(async (valid) => {
                if (!valid) return;
                this.systemMessageSubmitLoading = true;
                try {
                    const payload = {
                        title: this.systemMessageForm.title,
                        content: this.systemMessageForm.content,
                        isPopup: this.systemMessageForm.isPopup,
                        popupStartTime: this.systemMessageForm.isPopup === 1 ? (this.systemMessageForm.popupStartTime || null) : null,
                        popupEndTime: this.systemMessageForm.isPopup === 1 ? (this.systemMessageForm.popupEndTime || null) : null,
                    };
                    if (this.systemMessageDialogMode === "create") {
                        const response = await postSystemMessage(payload);
                        this.$message.success(response.data.message || "发布成功");
                    } else {
                        const response = await putSystemMessage(this.systemMessageForm.id, payload);
                        this.$message.success(response.data.message || "更新成功");
                    }
                    this.systemMessageDialogVisible = false;
                    await this.fetchSystemMessageTable();
                } catch (error) {
                    this.$message.error(error.response?.data?.message || "保存系统消息失败");
                } finally {
                    this.systemMessageSubmitLoading = false;
                }
            });
        },
        handleDeleteSystemMessage(row) {
            this.$confirm(`确认删除系统消息《${row.title}》吗？`, "删除提示", {
                type: "warning",
                confirmButtonText: "确认删除",
                cancelButtonText: "取消",
            }).then(async () => {
                try {
                    const response = await deleteSystemMessage(row.id);
                    this.$message.success(response.data.message || "删除成功");
                    if (this.systemMessageTableData.length === 1 && this.systemMessagePagination.page > 1) {
                        this.systemMessagePagination.page -= 1;
                    }
                    await this.fetchSystemMessageTable();
                } catch (error) {
                    this.$message.error(error.response?.data?.message || "删除系统消息失败");
                }
            }).catch(() => {});
        },
        openAvatarDialog(row) {
            this.selectedMemberId = row.id;
            this.avatarDialogVisible = true;
        },
        handleMemberAvatarBeforeUpload(file) {
            if (!file.type.startsWith("image/")) {
                this.$message.error("只能上传图片文件");
                return false;
            }
            if (file.size > 10 * 1024 * 1024) {
                this.$message.error("头像文件大小不能超过10MB");
                return false;
            }
            this.cleanupMemberCropImage();
            this.memberCropImageUrl = URL.createObjectURL(file);
            return false;
        },
        cleanupMemberCropImage() {
            if (this.memberCropImageUrl) {
                URL.revokeObjectURL(this.memberCropImageUrl);
                this.memberCropImageUrl = "";
            }
        },
        submitMemberAvatar() {
            if (!this.selectedMemberId || !this.$refs.memberAvatarCropper) return;
            this.avatarSubmitting = true;
            this.$refs.memberAvatarCropper.getCropBlob(async (blob) => {
                try {
                    const formData = new FormData();
                    formData.append("file", blob, "member-avatar.png");
                    const response = await postMemberAvatar(this.selectedMemberId, formData);
                    this.$message.success(response.data.message || "头像上传成功");
                    this.avatarDialogVisible = false;
                    this.cleanupMemberCropImage();
                    await this.fetchMemberTable();
                } catch (error) {
                    this.$message.error(error.response?.data?.message || "头像上传失败");
                } finally {
                    this.avatarSubmitting = false;
                }
            });
        },
    },
};
</script>

<style scoped>
.admin-view {
    min-height: calc(100vh - 136px);
    background:
        radial-gradient(circle at top left, rgba(14, 165, 233, 0.12), transparent 28%),
        radial-gradient(circle at right 20%, rgba(99, 102, 241, 0.10), transparent 24%),
        linear-gradient(180deg, #f8fbff 0%, #eef4ff 100%);
    padding: 18px;
}

.admin-shell {
    max-width: 1600px;
    margin: 0 auto;
    display: grid;
    grid-template-columns: 248px minmax(0, 1fr);
    gap: 18px;
    min-width: 0;
}

.admin-sidebar,
.admin-header-card,
.admin-main-panel {
    min-width: 0;
}

.admin-sidebar {
    position: sticky;
    top: 88px;
    align-self: start;
    background: linear-gradient(180deg, #0f172a 0%, #172554 100%);
    color: #e2e8f0;
    border-radius: 24px;
    padding: 20px 16px;
    box-shadow: 0 24px 50px rgba(15, 23, 42, 0.24);
    overflow: hidden;
}

.sidebar-brand {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;
}

.brand-tag {
    display: inline-flex;
    align-items: center;
    padding: 4px 10px;
    border-radius: 999px;
    background: rgba(56, 189, 248, 0.18);
    color: #7dd3fc;
    font-size: 12px;
    letter-spacing: 0.12em;
    margin-bottom: 10px;
}

.sidebar-brand h1 {
    font-size: 26px;
    color: #f8fafc;
}

.sidebar-nav {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.nav-item {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 12px;
    border: 0;
    border-radius: 16px;
    padding: 14px 12px;
    background: rgba(255, 255, 255, 0.04);
    color: inherit;
    cursor: pointer;
    transition: transform 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}

.nav-item:hover,
.nav-item.active {
    background: linear-gradient(135deg, rgba(56, 189, 248, 0.24), rgba(99, 102, 241, 0.24));
    box-shadow: inset 0 0 0 1px rgba(125, 211, 252, 0.28);
    transform: translateX(2px);
}

.nav-item i {
    font-size: 18px;
}

.admin-content {
    display: flex;
    flex-direction: column;
    gap: 18px;
    min-width: 0;
}

.admin-header-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    padding: 18px 22px;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.84);
    border: 1px solid rgba(148, 163, 184, 0.18);
    box-shadow: 0 18px 38px rgba(15, 23, 42, 0.08);
}

.header-left {
    display: flex;
    align-items: center;
    gap: 14px;
    min-width: 0;
}

.location-text {
    font-size: 12px;
    color: #64748b;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    margin-bottom: 6px;
}

.admin-header-card h2 {
    font-size: 28px;
    line-height: 1.15;
    color: #0f172a;
    margin-bottom: 6px;
}

.admin-main-panel {
    min-width: 0;
}

.crop-wrapper {
    width: 100%;
    height: 420px;
    margin-bottom: 16px;
}

.upload-trigger {
    display: flex;
    align-items: center;
    gap: 14px;
    flex-wrap: wrap;
}

.user-avatar-setting {
    display: flex;
    align-items: center;
    gap: 12px;
}

.dialog-user-avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    object-fit: cover;
    display: block;
    box-shadow: 0 6px 18px rgba(15, 23, 42, 0.14);
}

.upload-tip {
    color: #64748b;
    font-size: 13px;
}

.mobile-menu-btn,
.sidebar-close {
    color: #334155;
}

.admin-mask {
    position: fixed;
    inset: 0;
    background: rgba(15, 23, 42, 0.38);
    z-index: 999;
}

.user-avatar-setting {
    display: flex;
    align-items: center;
    gap: 12px;
}

.dialog-user-avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    object-fit: cover;
    border: 1px solid rgba(148, 163, 184, 0.35);
}

.form-tip {
    margin-top: 8px;
    line-height: 1.6;
    font-size: 12px;
    color: #64748b;
}

.admin-mask-fade-enter-active,
.admin-mask-fade-leave-active {
    transition: opacity 0.2s ease;
}

.admin-mask-fade-enter,
.admin-mask-fade-leave-to {
    opacity: 0;
}

@media (max-width: 900px) {
    .admin-view {
        padding: 12px;
    }

    .admin-shell {
        grid-template-columns: minmax(0, 1fr);
    }

    .admin-sidebar {
        position: fixed;
        top: 0;
        left: 0;
        bottom: 0;
        width: min(82vw, 290px);
        border-radius: 0 24px 24px 0;
        z-index: 1000;
        transition: transform 0.24s ease;
    }

    .admin-sidebar.collapsed {
        transform: translateX(-105%);
    }

    .admin-header-card {
        padding: 16px;
        border-radius: 18px;
        align-items: flex-start;
    }
}

@media (max-width: 640px) {
    .admin-view {
        min-height: calc(100vh - 122px);
    }

    .admin-header-card {
        flex-direction: column;
    }

    .admin-header-card h2 {
        font-size: 22px;
    }

    .crop-wrapper {
        height: 320px;
    }
}
</style>
