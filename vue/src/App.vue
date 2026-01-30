<template>
    <div id="app">
        <Navbar v-if="showNavbar" />
        <router-view />
        <Footer v-if="showFooter" />
    </div>
</template>

<script>
import Navbar from "@/components/Navbar.vue";
import Footer from "@/components/Footer.vue";

export default {
    name: "App",
    components: {
        Navbar,
        Footer,
    },
    computed: {
        showNavbar() {
            // 登录、注册、忘记密码页面不显示导航栏
            const authPages = ['/login', '/register', '/recover'];
            return !authPages.includes(this.$route.path);
        },
        showFooter() {
            // 登录、注册、忘记密码页面不显示页脚
            const authPages = ['/login', '/register', '/recover'];
            return !authPages.includes(this.$route.path);
        }
    },
    created() {
        // 初始化时恢复登录状态
        const token = localStorage.getItem("jwt_token"); // 从localStorage中获取token
        if (token) {
            this.$store.commit("setToken", token); // 将token存储到vuex中
        }
    },
    watch: {
        $route() {
            // 路由变化时重新计算是否显示导航栏和页脚
        }
    }
};
</script>

<style>
/* 这里可以引入全局的样式 */
@import "@/assets/styles/_common.css";
</style>
