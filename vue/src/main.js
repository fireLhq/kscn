import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
import "@/assets/styles/_common.css"; // 导入通用样式
import "@/assets/styles/resources.css"; // 导入资源页面样式
import "@/assets/styles/cloud-drive.css"; // 导入云盘页面样式
import "@/assets/styles/service.css"; // 导入服务页面样式
import "@/assets/styles/user-center.css"; // 导入用户中心样式
import axios from "axios";
import VueAxios from "vue-axios";

Vue.use(ElementUI);
Vue.use(VueAxios, axios);

Vue.config.productionTip = false;

// 创建Vue实例
const app = new Vue({
    router,
    store,
    render: (h) => h(App),
});

// 全局错误处理，静默处理路由重定向错误
app.$mount("#app");

// 全局错误处理
Vue.config.errorHandler = (err, vm, info) => {
    // 静默处理路由重定向错误
    if (err.message && err.message.includes('Redirected when going from')) {
        console.log('路由重定向已处理');
        return;
    }
    
    // 其他错误正常处理
    console.error('Vue错误:', err);
};

// 全局路由错误处理
Vue.config.warnHandler = (msg, vm, trace) => {
    // 静默处理路由警告
    if (msg && msg.includes('Redirected when going from')) {
        console.log('路由重定向警告已处理');
        return;
    }
    
    // 其他警告正常处理
    console.warn('Vue警告:', msg);
};

// 请求拦截器
axios.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("jwt_token");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器
axios.interceptors.response.use(
    (response) => {
        // 检查响应数据中的状态码
        if (response.data && response.data.code === 401) {
            // 数据中返回401，清除token
            store.commit("clearToken");
            
            // 如果当前页面需要认证，跳转到登录页
            const currentRoute = router.currentRoute;
            if (currentRoute.meta && currentRoute.meta.requiresAuth) {
                router.push("/login?redirect=" + encodeURIComponent(currentRoute.fullPath));
            }
        }
        return response;
    },
    (error) => {
        if (error.response) {
            if (error.response.status === 401) {
                // HTTP状态码401，清除token
                store.commit("clearToken");
                
                // 如果当前页面需要认证，跳转到登录页
                const currentRoute = router.currentRoute;
                if (currentRoute.meta && currentRoute.meta.requiresAuth) {
                    router.push("/login?redirect=" + encodeURIComponent(currentRoute.fullPath));
                }
            }
        }
        return Promise.reject(error);
    }
);






