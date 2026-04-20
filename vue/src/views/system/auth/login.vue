<template>
    <div class="login">
            <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
                <h2 class="title">登录KSCN系统</h2>
                <div class="icon">
                <img src="@/assets/images/system/auth/login/icon.svg" alt="icon" />
                </div>

                <el-form-item prop="account">
                    <el-input v-model="loginForm.account" type="text" placeholder="用户名/邮箱" prefix-icon="el-icon-user" />
                </el-form-item>

                <el-form-item prop="password">
                    <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="el-icon-lock" show-password />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click.prevent="handleLogin" class="login-btn" :loading="loading"> 登录 </el-button>
                </el-form-item>

                <div class="links">
                    <router-link to="/recover">忘记密码</router-link>
                    <router-link to="/register">点击注册</router-link>
                </div>
            </el-form>
    </div>
</template>

<script>
import { postAuthLogin } from "@/api/system/auth";

export default {
    name: "Login",

    data() {
        return {
            // 登录表单
            loginForm: {
                account: "",
                password: "",
            },
            // 登录表单验证规则
            loginRules: {
                account: [{ required: true, message: "请输入用户名/邮箱", trigger: "blur" }],
                password: [
                    { required: true, message: "请输入密码", trigger: "blur" },
                    {
                        min: 5,
                        max: 20,
                        message: "密码长度在5到20个字符",
                        trigger: "blur",
                    },
                ],
            },

            loading: false, // 登录按钮loading
        };
    },

    methods: {
        // 登录
        async handleLogin() {
            this.$refs.loginForm.validate(async (valid) => {
                if (valid) {
                    this.loading = true;
                    try {
                        const response = await postAuthLogin(this.loginForm);
                        // 状态码 200 即成功，直接取 data 中的 token
                            const token = response.data.data;
                            this.$store.commit("setToken", token);
                        this.$message.success(response.data.message || "登录成功");
                            const redirect = this.$route.query.redirect || "/";
                            this.$router.push(redirect);
                    } catch (error) {
                        const errorMessage = error.response?.data?.message || "登录失败，请重试";
                        this.$message.error(errorMessage);
                    } finally {
                        this.loading = false;
                    }
                }
            });
        },
    },
};
</script>

<style scoped>
.login {
    min-height: 100vh;
    min-width: 350px;
    display: flex;
    justify-content: center;
    align-items: center;
    background: url("@/assets/images/system/auth/login/background.jpg") no-repeat center/cover;
}

.title {
    color: #fff;
    text-align: center;
    margin: 0 0 14px 0;
    font-family: "Microsoft YaHei", Arial, sans-serif;
    font-size: 22px;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.login-form {
    background: rgba(0, 0, 0, 0.6);
    padding: 24px 30px 30px;
    border-radius: 10px;
    width: 350px;
    backdrop-filter: blur(10px);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.1);
    text-align: center;
}

.icon { margin-bottom: 18px; }
.icon img { width: 80px; height: 80px; border-radius: 50%; border: 3px solid rgba(255, 255, 255, 0.3); }

::v-deep .el-form-item { margin-bottom: 16px; }
::v-deep .el-input__inner { background-color: rgba(255,255,255,0.15) !important; border: 1px solid rgba(255,255,255,0.2) !important; color: #fff !important; height: 45px; border-radius: 8px; font-size: 14px; }
::v-deep .el-input__inner::placeholder { color: rgba(255,255,255,0.7) !important; }
::v-deep .el-input__prefix { color: rgba(255,255,255,0.8) !important; }

.login-btn { width: 100%; height: 45px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border: none; border-radius: 8px; font-size: 16px; font-weight: 600; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); }
.login-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6); }
.login-btn:active { transform: translateY(0); }

.links { margin-top: 14px; display: flex; justify-content: space-between; align-items: center; }
.links a { color: rgba(255, 255, 255, 0.8); text-decoration: none; font-size: 14px; transition: color 0.3s ease; padding: 5px 10px; border-radius: 5px; }
.links a:hover { color: #fff; background-color: rgba(255, 255, 255, 0.1); }

@media (max-width: 480px) {
    .login-form { width: 90%; padding: 20px; }
    .title { font-size: 20px; }
    .icon img { width: 60px; height: 60px; }
}
</style>
