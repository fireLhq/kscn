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
    }
};
</script>

<style>
/* 仅保留基础全局样式，避免页面明显变化 */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

html,
body,
#app {
    min-height: 100%;
}

body {
    line-height: 1.6;
    color: #495057;
    background-color: #f8f9fa;
}
</style>
