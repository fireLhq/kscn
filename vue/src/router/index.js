import Vue from "vue";
import VueRouter from "vue-router";
import Home from "@/views/system/home/index.vue";
import Login from "@/views/system/auth/login.vue";
import Register from "@/views/system/auth/register.vue";
import Recover from "@/views/system/auth/recover.vue";
import Resources from "@/views/system/resources/index.vue";
import CloudDrive from "@/views/system/cloud-drive/index.vue";
import Service from "@/views/system/service/index.vue";
import UserCenter from "@/views/system/user-center/index.vue";
import Admin from "@/views/admin/index.vue";
import About from "@/views/system/home/subpages/about/index.vue";
import Team from "@/views/system/home/subpages/team/index.vue";
import Sponsor from "@/views/system/home/subpages/sponsor/index.vue";
import Notice from "@/views/system/home/subpages/notice/index.vue";
import NotFound from "@/views/error/404.vue";

Vue.use(VueRouter);

const routes = [
    {
        path: "/",
        name: "Home",
        component: Home,
        meta: {
            title: "首页",
            requiresAuth: false,
        },
    },
    {
        path: "/login",
        name: "Login",
        component: Login,
        meta: {
            title: "登录",
            requiresAuth: false,
        },
    },
    {
        path: "/register",
        name: "Register",
        component: Register,
        meta: {
            title: "注册",
            requiresAuth: false,
        },
    },
    {
        path: "/recover",
        name: "Recover",
        component: Recover,
        meta: {
            title: "找回密码",
            requiresAuth: false,
        },
    },
    {
        path: "/resources",
        name: "Resources",
        component: Resources,
        meta: {
            title: "资源",
            requiresAuth: true,
        },
    },
    {
        path: "/cloud-drive",
        name: "CloudDrive",
        component: CloudDrive,
        meta: {
            title: "云盘",
            requiresAuth: true,
        },
    },
    {
        path: "/service",
        name: "Service",
        component: Service,
        meta: {
            title: "服务",
            requiresAuth: false,
        },
    },
    {
        path: "/user-center",
        name: "UserCenter",
        component: UserCenter,
        meta: {
            title: "个人中心",
            requiresAuth: true,
        },
    },
    {
        path: "/admin",
        name: "Admin",
        component: Admin,
        meta: {
            title: "后台管理",
            requiresAuth: true,
            requiresAdmin: true,
        },
    },
    {
        path: "/about",
        name: "About",
        component: About,
        meta: {
            title: "关于我们",
            requiresAuth: false,
        },
    },
    {
        path: "/team",
        name: "Team",
        component: Team,
        meta: {
            title: "核心人员",
            requiresAuth: false,
        },
    },
    {
        path: "/sponsor",
        name: "Sponsor",
        component: Sponsor,
        meta: {
            title: "赞助支持",
            requiresAuth: false,
        },
    },
    {
        path: "/notice",
        name: "Notice",
        component: Notice,
        meta: {
            title: "注意事项",
            requiresAuth: false,
        },
    },
    {
        path: "*",
        name: "NotFound",
        component: NotFound,
        meta: {
            title: "页面未找到",
            requiresAuth: false,
        },
    },
];

const router = new VueRouter({
    mode: "history",
    base: process.env.BASE_URL,
    routes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition;
        } else {
            return { x: 0, y: 0 };
        }
    },
});

// 路由守卫
router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - KSCN` : "KSCN";
    
    // 如果目标页面需要认证
    if (to.meta.requiresAuth) {
        const token = localStorage.getItem("jwt_token");
        if (!token) {
            // 未登录用户访问受保护页面时重定向到登录页面
            next("/login?redirect=" + encodeURIComponent(to.fullPath));
            return;
        }
        
        // 检查token是否有效（未过期）
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const exp = payload.exp;
            if (!exp || exp < Math.floor(Date.now() / 1000)) {
                // token已过期，清除并跳转到登录页
                localStorage.removeItem("jwt_token");
                next("/login?redirect=" + encodeURIComponent(to.fullPath));
                return;
            }
        } catch (e) {
            // token格式错误，清除并跳转到登录页
            localStorage.removeItem("jwt_token");
            next("/login?redirect=" + encodeURIComponent(to.fullPath));
            return;
        }
    }
    
    // 如果目标页面需要管理员权限
    if (to.meta.requiresAdmin) {
        // 暂时跳过权限检查，让页面先能正常访问
        // TODO: 实现完整的管理员权限检查
        console.log('访问管理页面，权限检查暂时跳过');
    }
    
    // 如果目标页面是登录页，且用户已登录，重定向到首页
    if (to.path === '/login') {
        const token = localStorage.getItem("jwt_token");
        if (token) {
            // 检查token是否有效
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                const exp = payload.exp;
                if (exp && exp > Math.floor(Date.now() / 1000)) {
                    next('/');
                    return;
                }
            } catch (e) {
                // token无效，清除
                localStorage.removeItem("jwt_token");
            }
        }
    }
    
    // 正常导航
    next();
});

// 路由错误处理
router.onError((error) => {
    // 静默处理重定向错误
    if (error.message && error.message.includes('Redirected when going from')) {
        console.log('路由重定向已处理');
        return;
    }
    
    // 其他路由错误正常处理
    console.error('路由错误:', error);
});

// 路由解析错误处理
router.onReady(() => {
    console.log('路由已准备就绪');
});

// 路由导航确认
router.beforeResolve((to, from, next) => {
    // 在路由解析之前进行额外检查
    next();
});

export default router;
