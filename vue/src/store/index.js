import Vue from "vue";
import Vuex from "vuex";
import { isTokenExpired } from "@/utils/isTokenExpired";

Vue.use(Vuex);

export default new Vuex.Store({
    state: {
        token: localStorage.getItem("jwt_token") || null, // 初始化时读取本地存储
        userRole: null, // 用户角色信息
        currentUser: null, // 当前登录用户信息
    },
    mutations: {
        setToken(state, token) {
            state.token = token;
            localStorage.setItem("jwt_token", token); // 同步存储
        },
        clearToken(state) {
            state.token = null;
            state.userRole = null;
            state.currentUser = null;
            localStorage.removeItem("jwt_token");
        },
        setUserRole(state, role) {
            state.userRole = role;
        },
        setCurrentUser(state, user) {
            state.currentUser = user;
        },
    },
    getters: {
        isAuthenticated: (state) => {
            const token = state.token;
            return token && !isTokenExpired(token);
        },
    },
});
