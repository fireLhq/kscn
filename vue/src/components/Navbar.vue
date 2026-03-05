<template>
    <div class="navbar-bottom">
        <div class="navbar">
            <nav class="nav-items-1">
                <div class="logo">
                    <img src="@/assets/images/navbar/logo.svg" alt="Logo" class="logo-image" />
                    <span class="website-name">KSCN</span>
                </div>

                <div class="desktop-menu">
                    <router-link to="/" exact class="nav-link">首页</router-link>
                    <router-link to="/resources" class="nav-link">资源</router-link>
                    <router-link to="/cloud-drive" class="nav-link">云盘</router-link>
                    <router-link to="/service" class="nav-link">服务</router-link>
                    <router-link 
                        v-if="isAdmin" 
                        to="/admin" 
                        class="nav-link admin-link"
                    >
                        管理
                    </router-link>
                </div>

                <!-- 移动端：使用下拉菜单而不是整栏展开 -->
                <el-dropdown class="mobile-menu-btn" trigger="click" @command="handleMobileCommand">
                    <span class="hamburger">
                        <span class="hamburger-line"></span>
                        <span class="hamburger-line"></span>
                        <span class="hamburger-line"></span>
                    </span>
                    <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item command="/">首页</el-dropdown-item>
                        <el-dropdown-item command="/resources">资源</el-dropdown-item>
                        <el-dropdown-item command="/cloud-drive">云盘</el-dropdown-item>
                        <el-dropdown-item command="/service">服务</el-dropdown-item>
                        <el-dropdown-item v-if="isAdmin" command="/admin">管理</el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
            </nav>

            <nav class="nav-items-2">
                <div class="visitor-count">访客数量: <span>{{ visitorCount }}</span></div>
                <el-dropdown trigger="hover" @command="handleUserCommand">
                    <span class="user-icon" @click="goToProfile">
                        <img :src="avatarUrl" alt="用户头像" class="user-avatar" />
                    </span>
                    <el-dropdown-menu slot="dropdown" class="dropdown-menu">
                        <div class="dropdown-content" @click="goToProfile">
                            <img :src="avatarUrl" alt="用户头像" class="dropdown-avatar" />
                            <p class="user-id">{{ username || '未登录' }}</p>
                        </div>
                        <el-dropdown-item divided :command="isLoggedIn ? 'logout' : 'login'">
                            <span :style="isLoggedIn ? 'color:#F56C6C' : 'color:#409EFF'">
                                {{ isLoggedIn ? '注销' : '登录' }}
                            </span>
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
            </nav>
        </div>
    </div>
</template>

<script>
import { getUserInfo } from "@/api/system/user";
import { getUserAvatarUrl } from "@/utils/avatarUtils";
export default {
    name: "Navbar",
    data() {
        return {
            visitorCount: 999,
            username: "",
            nickname: "",
            avatarName: "",
            userRole: null,
            tokenCheckTimer: null,
            avatarTimestamp: Date.now(), // 用于强制刷新头像
        };
    },
    computed: {
        avatarUrl() {
            // 引用 avatarTimestamp 确保头像更新时重新计算
            const timestamp = this.avatarTimestamp;
            
            if (this.isLoggedIn && (this.avatarName || this.username)) {
                const url = getUserAvatarUrl({ 
                    avatar: this.avatarName, 
                    nickname: this.nickname, 
                    username: this.username 
                });
                // 如果返回 null（默认头像），使用本地默认头像
                return url || require("@/assets/images/avatar/user.png");
            }
            // 未登录时使用本地默认头像
            return require("@/assets/images/avatar/user.png");
        },
        isLoggedIn() {
            const token = this.$store.state.token;
            return token && !this.isTokenExpired(token) && this.username;
        },
        isAdmin() {
            return this.isLoggedIn && (this.userRole === 0 || this.$store.state.userRole === 0);
        },
    },
    async mounted() {
        const token = this.$store.state.token;
        if (token && !this.isTokenExpired(token)) {
            try {
                const res = await getUserInfo(token);
                if (res.data.code === 200) {
                    this.username = res.data.data.username;
                    this.nickname = res.data.data.nickname;
                    this.avatarName = res.data.data.avatar;
                    this.userRole = res.data.data.role;
                    // 将用户角色存储到store中
                    this.$store.commit("setUserRole", res.data.data.role);
                }
            } catch (e) {
                // 如果获取用户信息失败，可能是token过期，清除token
                if (e.response && e.response.status === 401) {
                    this.$store.commit("clearToken");
                }
            }
        }
        
        // 启动定时检查token有效性
        this.startTokenCheck();
        
        // 监听用户中心的头像更新事件
        this.$root.$on('refreshNavbarAvatar', this.refreshAvatar);
    },
    beforeDestroy() {
        // 组件销毁时清除定时器
        if (this.tokenCheckTimer) {
            clearInterval(this.tokenCheckTimer);
        }
        // 移除事件监听
        this.$root.$off('refreshNavbarAvatar', this.refreshAvatar);
    },
    methods: {
        // 检查token是否过期
        isTokenExpired(token) {
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                const exp = payload.exp;
                if (!exp) return true;
                const currentTime = Math.floor(Date.now() / 1000);
                return exp < currentTime;
            } catch (e) {
                return true; // 解析失败视为过期
            }
        },
        // 启动定时检查token有效性
        startTokenCheck() {
            // 每分钟检查一次token是否过期
            this.tokenCheckTimer = setInterval(() => {
                const token = this.$store.state.token;
                if (token && this.isTokenExpired(token)) {
                    // token已过期，清除并更新状态
                    this.$store.commit("clearToken");
                    this.username = "";
                    this.nickname = "";
                    this.avatarName = "";
                    this.userRole = null;
                }
            }, 60000); // 60秒检查一次
        },
        goToProfile() {
            if (this.$route.path === '/user-center') {
                window.scrollTo({ top: 0, behavior: 'smooth' });
            } else {
                // 检查是否需要登录，包括token有效性检查
                const token = localStorage.getItem("jwt_token");
                if (!token || this.isTokenExpired(token)) {
                    this.$router.push('/login?redirect=' + encodeURIComponent('/user-center'));
                } else {
                    this.$router.push('/user-center');
                }
            }
        },
        logout() {
            this.$store.commit("clearToken");
            this.username = "";
            this.nickname = "";
            this.avatarName = "";
            this.userRole = null;
            this.$message.success("已注销");
            this.$router.push("/login");
        },
        handleMobileCommand(path) {
            if (this.$route.path === path) {
                window.scrollTo({ top: 0, behavior: 'smooth' });
            } else {
                // 检查是否需要登录
                if (path === '/resources' || path === '/cloud-drive' || path === '/user-center' || path === '/admin') {
                    const token = localStorage.getItem("jwt_token");
                    if (!token || this.isTokenExpired(token)) {
                        this.$router.push('/login?redirect=' + encodeURIComponent(path));
                        return;
                    }
                    // 检查管理页面权限
                    if (path === '/admin' && !this.isAdmin) {
                        this.$message.warning('您没有权限访问管理页面');
                        return;
                    }
                }
                this.$router.push(path);
            }
        },
        handleUserCommand(cmd) {
            if (cmd === 'logout') {
                this.logout();
            } else if (cmd === 'login') {
                this.$router.push('/login');
            }
        },
        async refreshAvatar() {
            // 重新获取用户信息以更新头像
            const token = this.$store.state.token;
            if (token && !this.isTokenExpired(token)) {
                try {
                    const res = await getUserInfo(token);
                    if (res.data.code === 200) {
                        // 更新头像名称
                        this.avatarName = res.data.data.avatar;
                        // 更新时间戳，强制 computed 重新计算
                        this.avatarTimestamp = Date.now();
                    }
                } catch (e) {
                    console.error('刷新头像失败:', e);
                }
            }
        }
    },
};
</script>

<style scoped>
@import "@/assets/styles/navbar.css";

/* 移动端下拉按钮样式 */
.mobile-menu-btn {
    display: none;
}
.hamburger { display: inline-flex; flex-direction: column; gap: 4px; padding: 6px; cursor: pointer; }
.hamburger-line { width: 22px; height: 2px; background: #fff; }

.admin-link {
    color: #ff6b6b !important;
    font-weight: 600;
}

.admin-link:hover {
    color: #ff5252 !important;
}

@media (max-width: 767px) {
    .desktop-menu { display: none; }
    .mobile-menu-btn { display: inline-block; }
}
</style>
