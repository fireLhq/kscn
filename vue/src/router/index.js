import Vue from "vue";
import VueRouter from "vue-router";
import HomeLayout from "@/views/system/home/layout.vue";
import Home from "@/views/system/home/index.vue";
import Login from "@/views/system/auth/login.vue";
import Register from "@/views/system/auth/register.vue";
import Recover from "@/views/system/auth/recover.vue";
import PublicFiles from "@/views/system/public-files/index.vue";
import UserFiles from "@/views/system/user-files/index.vue";
import Service from "@/views/system/service/index.vue";
import Profile from "@/views/system/profile/index.vue";
import Admin from "@/views/admin/index.vue";
import About from "@/views/system/home/subpages/about/index.vue";
import Team from "@/views/system/home/subpages/team/index.vue";
import Sponsor from "@/views/system/home/subpages/sponsor/index.vue";
import Notice from "@/views/system/home/subpages/notice/index.vue";
import ErrorPage from "@/views/error/error.vue";
import NotFound from "@/views/error/404.vue";
import { isTokenExpired } from "@/utils/isTokenExpired";

Vue.use(VueRouter);

const routes = [
    {
        path: "/",
        component: HomeLayout,
        meta: {
            requiresAuth: false,
        },
        children: [
            {
                path: "",
        name: "Home",
        component: Home,
        meta: {
            title: "首页",
            requiresAuth: false,
        },
            },
            {
                path: "about",
                name: "About",
                component: About,
                meta: {
                    title: "关于我们",
                    requiresAuth: false,
                },
            },
            {
                path: "team",
                name: "Team",
                component: Team,
                meta: {
                    title: "核心人员",
                    requiresAuth: false,
                },
            },
            {
                path: "sponsor",
                name: "Sponsor",
                component: Sponsor,
                meta: {
                    title: "赞助支持",
                    requiresAuth: false,
                },
            },
            {
                path: "notice",
                name: "Notice",
                component: Notice,
                meta: {
                    title: "注意事项",
                    requiresAuth: false,
                },
            },
        ],
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
        path: "/public-files",
        name: "PublicFiles",
        component: PublicFiles,
        meta: {
            title: "资源",
            requiresAuth: false,
        },
    },
    {
        path: "/user-files",
        name: "UserFiles",
        component: UserFiles,
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
        path: "/profile",
        name: "Profile",
        component: Profile,
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
        path: "/error",
        name: "Error",
        component: ErrorPage,
        props: true,
        meta: {
            title: "系统异常",
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
        return savedPosition || { x: 0, y: 0 };
    },
});

// 路由守卫：只负责前端页面准入控制
router.beforeEach((to, from, next) => {
    document.title = to.meta.title ? `${to.meta.title} - KSCN` : "KSCN";
    
        const token = localStorage.getItem("jwt_token");
    const validToken = token && !isTokenExpired(token);

    if (to.meta.requiresAuth && !validToken) {
            localStorage.removeItem("jwt_token");
            next("/login?redirect=" + encodeURIComponent(to.fullPath));
            return;
        }

    if (to.path === "/login" && validToken) {
        next("/");
                    return;
                }

    next();
});

router.onError((error) => {
    if (error.message && error.message.includes("Redirected when going from")) {
        return;
    }
    
    console.error("路由错误:", error);
});

export default router;
