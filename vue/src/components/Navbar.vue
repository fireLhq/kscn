<template>
    <div class="navbar-bottom">
        <div class="navbar">
            <nav class="nav-items-1">
                <div class="logo">
                    <img src="@/assets/images/components/navbar/logo.svg" alt="Logo" class="logo-image" />
                    <span class="website-name">KSCN</span>
                </div>

                <div class="desktop-menu">
                    <router-link to="/" exact class="nav-link">首页</router-link>
                    <router-link to="/public-files" class="nav-link">资源</router-link>
                    <router-link to="/user-files" class="nav-link">云盘</router-link>
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
                        <el-dropdown-item command="/public-files">资源</el-dropdown-item>
                        <el-dropdown-item command="/user-files">云盘</el-dropdown-item>
                        <el-dropdown-item command="/service">服务</el-dropdown-item>
                        <el-dropdown-item v-if="isAdmin" command="/admin">管理</el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
            </nav>

            <nav class="nav-items-2">
                <div class="visitor-count">访客数量: <span>{{ visitorCount }}</span></div>
                <el-dropdown :trigger="userDropdownTrigger" @command="handleUserCommand">
                    <span class="user-icon" @click="goToProfile">
                        <img :src="avatarUrl" alt="用户头像" class="user-avatar" />
                    </span>
                    <el-dropdown-menu slot="dropdown" class="dropdown-menu">
                        <div class="dropdown-content" @click="goToProfile">
                            <img :src="avatarUrl" alt="用户头像" class="dropdown-avatar" />
                            <p class="user-id">{{ nickname || username || email || (isLoggedIn ? '已登录' : '未登录') }}</p>
                        </div>
                        <el-dropdown-item divided :command="isLoggedIn ? 'logout' : 'login'">
                            <span :style="isLoggedIn ? 'color:#F56C6C' : 'color:#409EFF'">
                                {{ isLoggedIn ? '退出' : '登录' }}
                            </span>
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
            </nav>
        </div>
    </div>
</template>

<script>
import { getUserProfile, getUserAvatarUrl } from "@/api/system/user";
import { postAuthLogout as logoutApi } from "@/api/system/auth";
import { getVisitorCount } from "@/api/system/login-log";
import { isTokenExpired } from "@/utils/isTokenExpired";

export default {
    name: "Navbar",
    data() {
        return {
            visitorCount: 0,
            username: "",
            nickname: "",
            email: "",
            avatarName: "",
            userRole: null,
            tokenCheckTimer: null,
            isMobileViewport: window.innerWidth <= 767,
            avatarTimestamp: Date.now(), // 用于强制刷新头像
        };
    },
    computed: {
        avatarUrl() {
            // 引用 avatarTimestamp 确保头像更新时重新计算
            const timestamp = this.avatarTimestamp;
            void timestamp;

            if (this.isLoggedIn) {
                const url = getUserAvatarUrl(this.avatarName, this.avatarTimestamp);
                return url || require("@/assets/images/components/avatar/user.png");
            }
            // 未登录时使用本地默认头像
            return require("@/assets/images/components/avatar/user.png");
        },
        isLoggedIn() {
            const token = this.$store.state.token;
            return !!(
                this.userRole !== null ||
                this.username ||
                this.email ||
                this.nickname ||
                this.avatarName ||
                (token && !isTokenExpired(token))
            );
        },
        isAdmin() {
            return this.isLoggedIn && (this.userRole === 0 || this.$store.state.userRole === 0);
        },
        userDropdownTrigger() {
            return this.isMobileViewport ? "click" : "hover";
        },
    },
    async mounted() {
        await this.loadVisitorCount();
            try {
                const res = await getUserProfile();
                if (res.data.code === 200) {
                    this.username = res.data.data.username;
                    this.nickname = res.data.data.nickname;
                this.email = res.data.data.email;
                    this.avatarName = res.data.data.avatar;
                    this.userRole = res.data.data.role;
                    this.$store.commit("setUserRole", res.data.data.role);
                }
            } catch (e) {
                if (e.response && e.response.status === 401) {
                    this.$store.commit("clearToken");
            }
        }

        // 启动定时检查token有效性
        this.startTokenCheck();

        window.addEventListener("resize", this.handleViewportResize);

        // 监听用户中心的头像更新事件
        this.$root.$on("refreshNavbarAvatar", this.refreshAvatar);
    },
    beforeDestroy() {
        // 组件销毁时清除定时器
        if (this.tokenCheckTimer) {
            clearInterval(this.tokenCheckTimer);
        }
        window.removeEventListener("resize", this.handleViewportResize);
        // 移除事件监听
        this.$root.$off("refreshNavbarAvatar", this.refreshAvatar);
    },
    methods: {
        handleViewportResize() {
            this.isMobileViewport = window.innerWidth <= 767;
        },
        async loadVisitorCount() {
            try {
                const res = await getVisitorCount();
                if (res.data.code === 200) {
                    this.visitorCount = res.data.data?.visitorCount ?? 0;
                }
            } catch (e) {
                this.visitorCount = 0;
            }
        },
        // 启动定时检查token有效性
        startTokenCheck() {
            // 每分钟检查一次token是否过期
            this.tokenCheckTimer = setInterval(() => {
                const token = this.$store.state.token;
                if (token && isTokenExpired(token)) {
                    // token已过期，清除并更新状态
                    this.$store.commit("clearToken");
                    this.username = "";
                    this.nickname = "";
                    this.email = "";
                    this.avatarName = "";
                    this.userRole = null;
                }
            }, 60000); // 60秒检查一次
        },
        goToProfile() {
            if (this.$route.path === "/profile") {
                window.scrollTo({ top: 0, behavior: "smooth" });
            } else if (!this.isLoggedIn) {
                this.$router.push("/login?redirect=" + encodeURIComponent("/profile"));
            } else {
                this.$router.push("/profile");
            }
        },
        async logout() {
            try {
                const res = await logoutApi();
                if (res.data && res.data.code !== 200) {
                    this.$message.error(res.data.message || "登出失败");
                    return;
                }
            } catch (e) {
                this.$message.error(e.response?.data?.message || "登出失败，请稍后重试");
                return;
            }

            this.$store.commit("clearToken");
            this.username = "";
            this.nickname = "";
            this.email = "";
            this.avatarName = "";
            this.userRole = null;
            this.$message.success("已注销");
            this.$router.push("/login");
        },
        handleMobileCommand(path) {
            if (this.$route.path === path) {
                window.scrollTo({ top: 0, behavior: "smooth" });
            } else {
                const targetRoute = this.$router.resolve(path).route;
                const requiresAuth = targetRoute.meta && targetRoute.meta.requiresAuth;

                if (requiresAuth) {
                    const token = localStorage.getItem("jwt_token");
                    if (!token || isTokenExpired(token)) {
                        this.$router.push("/login?redirect=" + encodeURIComponent(path));
                        return;
                    }
                }

                    if (path === "/admin" && !this.isAdmin) {
                        this.$message.warning("您没有权限访问管理页面");
                        return;
                    }

                this.$router.push(path);
            }
        },
        handleUserCommand(cmd) {
            if (cmd === "logout") {
                this.logout();
            } else if (cmd === "login") {
                this.$router.push("/login");
            }
        },
        async refreshAvatar() {
            const token = this.$store.state.token;
            if (token && !isTokenExpired(token)) {
                try {
                    const res = await getUserProfile();
                    if (res.data.code === 200) {
                        this.avatarName = res.data.data.avatar;
                        this.avatarTimestamp = Date.now();
                    }
                } catch (e) {
                    console.error("刷新头像失败:", e);
                }
            }
        },
    },
};
</script>

<style scoped>
/* 导航栏样式 */
.navbar-bottom {
    position: sticky;
    top: 0;
    z-index: 1000;
    background: #2c3e50;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.navbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 2rem;
    height: 70px;
    max-width: 1400px;
    margin: 0 auto;
}

/* 左侧导航 */
.nav-items-1 {
    display: flex;
    align-items: center;
    gap: 3rem;
}

.logo {
    display: flex;
    align-items: center;
    gap: 0.8rem;
    cursor: pointer;
    transition: transform 0.2s ease;
}

.logo:hover {
    transform: scale(1.05);
}

.logo-image {
    width: 40px;
    height: 40px;
}

.website-name {
    font-size: 1.8rem;
    font-weight: 700;
    color: #fff;
    font-family: "Microsoft YaHei", Arial, sans-serif;
}

.nav-link {
    color: #bdc3c7;
    text-decoration: none;
    font-size: 1.1rem;
    font-weight: 500;
    padding: 0.5rem 1rem;
    border-radius: 6px;
    transition: all 0.2s ease;
    position: relative;
}

.nav-link:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.1);
}

.nav-link.router-link-active {
    color: #fff;
    background: rgba(255, 255, 255, 0.15);
}

/* 右侧用户信息 */
.nav-items-2 {
    display: flex;
    align-items: center;
    gap: 2rem;
}

.visitor-count {
    color: #bdc3c7;
    font-size: 0.9rem;
    font-weight: 500;
}

.visitor-count span {
    color: #3498db;
    font-weight: 600;
}

.user-icon {
    cursor: pointer;
}

.user-avatar {
    width: 40px;
    height: 40px;
    display: block;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.2);
    transition: border-color 0.2s ease;
}

.user-avatar:hover {
    border-color: rgba(255, 255, 255, 0.4);
}

/* 用户下拉菜单 */
.dropdown-menu {
    padding: 0;
    border: none;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    overflow: hidden;
}

.dropdown-content {
    padding: 1.5rem;
    background: #fff;
    min-width: 160px;
}

.dropdown-avatar {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    margin: 0 auto 1rem;
    display: block;
    border: 3px solid #e9ecef;
}

.user-id {
    text-align: center;
    color: #2c3e50;
    font-weight: 600;
    margin-bottom: 1.5rem;
    font-size: 1rem;
}

.dropdown-actions {
    display: flex;
    gap: 0.8rem;
    justify-content: center;
}

.dropdown-actions .el-button {
    flex: 1;
    border-radius: 6px;
    font-weight: 500;
    transition: all 0.2s ease;
    text-align: center;
}

.dropdown-actions .el-button:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .navbar {
        padding: 0 1.5rem;
    }

    .nav-items-1 {
        gap: 2rem;
    }

    .nav-link {
        font-size: 1rem;
        padding: 0.4rem 0.8rem;
    }
}

@media (max-width: 992px) {
    .navbar {
        padding: 0 1rem;
    }

    .nav-items-1 {
        gap: 1.5rem;
    }

    .website-name {
        font-size: 1.6rem;
    }

    .logo-image {
        width: 35px;
        height: 35px;
    }

    .nav-link {
        font-size: 0.95rem;
        padding: 0.3rem 0.6rem;
    }
}

@media (max-width: 768px) {
    .navbar {
        height: 60px;
        padding: 0 1rem;
    }

    .nav-items-1 {
        gap: 1rem;
    }

    .website-name {
        font-size: 1.4rem;
    }

    .logo-image {
        width: 30px;
        height: 30px;
    }

    .visitor-count {
        font-size: 0.8rem;
    }

    .user-avatar {
        width: 35px;
        height: 35px;
    }

    .dropdown-content {
        padding: 1.2rem;
        min-width: 180px;
    }

    .dropdown-avatar {
        width: 50px;
        height: 50px;
    }

    .dropdown-actions {
        flex-direction: column;
        gap: 0.6rem;
    }
}

@media (max-width: 480px) {
    .navbar {
        padding: 0 0.8rem;
    }

    .nav-items-1 {
        gap: 0.8rem;
    }

    .website-name {
        font-size: 1.2rem;
    }

    .logo-image {
        width: 28px;
        height: 28px;
    }

    .visitor-count {
        display: none;
    }

    .user-avatar {
        width: 32px;
        height: 32px;
    }
}

/* 移动端下拉按钮样式 */
.mobile-menu-btn {
    display: none;
}

.hamburger {
    display: inline-flex;
    flex-direction: column;
    gap: 4px;
    padding: 6px;
    cursor: pointer;
}

.hamburger-line {
    width: 22px;
    height: 2px;
    background: #fff;
}

.admin-link {
    color: #ff6b6b !important;
    font-weight: 600;
}

.admin-link:hover {
    color: #ff5252 !important;
}

@media (max-width: 767px) {
    .desktop-menu {
        display: none;
    }

    .mobile-menu-btn {
        display: inline-block;
    }
}
</style>
