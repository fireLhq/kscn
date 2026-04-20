import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";

Vue.use(ElementUI);

Vue.config.productionTip = false;

// 全局错误处理
Vue.config.errorHandler = (err) => {
    if (err.message && err.message.includes("Redirected when going from")) {
        return;
    }
    console.error("Vue错误:", err);
};

// 全局警告处理
Vue.config.warnHandler = (msg) => {
    if (msg && msg.includes("Redirected when going from")) {
        return;
    }
    console.warn("Vue警告:", msg);
};

new Vue({
    router,
    store,
    render: (h) => h(App),
}).$mount("#app");
