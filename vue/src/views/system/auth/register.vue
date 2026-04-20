<template>
    <div class="register">
        <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form">
            <h2 class="title">注册KSCN账号</h2>
            <div class="icon">
                <img src="@/assets/images/system/auth/register/icon.svg" alt="icon" />
            </div>

            <el-form-item prop="email">
                <el-input v-model="registerForm.email" type="email" placeholder="邮箱" auto-complete="off" prefix-icon="el-icon-message" />
            </el-form-item>

            <el-form-item prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="密码" auto-complete="off" prefix-icon="el-icon-lock" show-password />
            </el-form-item>

            <el-form-item prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" auto-complete="off" prefix-icon="el-icon-lock" show-password />
            </el-form-item>

            <div class="code-form-item">
                <el-form-item prop="code" class="code-input-wrapper">
                    <el-input v-model="registerForm.code" type="text" placeholder="验证码" auto-complete="off" prefix-icon="el-icon-s-operation" class="code-input" />
                </el-form-item>

                <el-button @click.prevent="triggerHiddenClick" class="code-btn" :disabled="remainingTime > 0">
                    {{ buttonText }}
                </el-button>
            </div>

                <!--隐藏按钮，用于触发人机验证-->
                <el-button ref="hiddenBtn" style="display: none" id="captcha-button" />
                <div id="captcha-element"></div>

            <el-form-item>
                <el-button :loading="loading" type="primary" @click.prevent="handleRegister" class="register-btn">
                    <span v-if="!loading">注册</span>
                </el-button>
            </el-form-item>

            <div class="links">
                    <router-link to="/login">已有账户？登录</router-link>
                </div>
        </el-form>
    </div>
</template>

<script>
import { postAuthRegisterCode, postAuthRegister, postAuthCaptchaVerify } from "@/api/system/auth";

export default {
    name: "Register",

    data() {
        return {
            registerForm: {
                email: "",
                password: "",
                confirmPassword: "",
                code: "",
            },
            registerRules: {
                email: [
                    { required: true, message: "请输入邮箱地址", trigger: "blur" },
                    { type: "email", message: "请输入正确的邮箱格式", trigger: ["blur", "change"] },
                ],
                password: [
                    { required: true, message: "请输入密码", trigger: "blur" },
                    { min: 5, max: 20, message: "密码长度需在 5-20 个字符之间", trigger: "blur" },
                ],
                confirmPassword: [
                    { required: true, message: "请再次输入密码", trigger: "blur" },
                    { validator: this.validatePasswordMatch, trigger: "blur" },
                ],
                code: [{ required: true, message: "请输入验证码", trigger: "blur" }],
            },
            loading: false,
            remainingTime: 0,
            timer: null,
        };
    },

    mounted() {
        window.initAliyunCaptcha({
            SceneId: "rlztu5cf",
            prefix: "17pvo0",
            mode: "popup",
            element: "#captcha-element",
            button: "#captcha-button",
            captchaVerifyCallback: this.captchaVerifyCallback,
            onBizResultCallback: this.onBizResultCallback,
            getInstance: this.getInstance,
            slideStyle: {
                width: 360,
                height: 40,
            },
            language: "cn",
        });
    },

    beforeDestroy() {
        // 必须删除相关元素，否则再次 mount 多次调用 initAliyunCaptcha 会导致多次回调 captchaVerifyCallback
        document.getElementById("aliyunCaptcha-mask")?.remove();
        document.getElementById("aliyunCaptcha-window-popup")?.remove();
        clearInterval(this.timer);
    },

    computed: {
        buttonText() {
            return this.remainingTime > 0 ? `${this.remainingTime} 秒重发` : "获取验证码";
        },
    },

    created() {
        const savedEndTime = localStorage.getItem("countdownEnd");
        if (savedEndTime) {
            const remaining = Math.ceil((savedEndTime - Date.now()) / 1000);
            if (remaining > 0) {
                this.remainingTime = remaining;
                this.startTimer();
            } else {
                localStorage.removeItem("countdownEnd");
            }
        }
    },

    methods: {
        async triggerHiddenClick() {
            const isEmailValid = await this.validateEmail();
            const isPasswordValid = await this.validatePassword();
            const isConfirmPasswordValid = await this.validateConfirmPassword();
            if (isEmailValid && isPasswordValid && isConfirmPasswordValid) {
                if (this.$refs.hiddenBtn) {
                    this.$refs.hiddenBtn.$el.click();
                }
            }
        },

        async validateEmail() {
            return new Promise((resolve) => {
                this.$refs.registerForm.validateField("email", (error) => resolve(!error));
            });
        },
        async validatePassword() {
            return new Promise((resolve) => {
                this.$refs.registerForm.validateField("password", (error) => resolve(!error));
            });
        },
        async validateConfirmPassword() {
            return new Promise((resolve) => {
                this.$refs.registerForm.validateField("confirmPassword", (error) => resolve(!error));
            });
        },

        getInstance(instance) {
            this.captcha = instance;
        },

        // 人机验证回调
        async captchaVerifyCallback(captchaVerifyParam) {
            return await postAuthCaptchaVerify(captchaVerifyParam);
        },
        onBizResultCallback() {
            this.sendEmailCode();
            this.startCountdown();
        },

        async sendEmailCode() {
            try {
                const response = await postAuthRegisterCode({ email: this.registerForm.email, password: this.registerForm.password });
                    this.$message.success(response.data.message || "验证码已发送，请检查您的邮箱");
            } catch (error) {
                this.$message.error(error.response?.data?.message || "验证码发送失败");
            }
        },

        startCountdown() {
            if (this.remainingTime > 0) return;
            this.remainingTime = 60;
            localStorage.setItem("countdownEnd", Date.now() + 60000);
            this.startTimer();
        },

        startTimer() {
            clearInterval(this.timer);
            this.timer = setInterval(() => {
                this.remainingTime -= 1;
                if (this.remainingTime <= 0) {
                    clearInterval(this.timer);
                    localStorage.removeItem("countdownEnd");
                }
            }, 1000);
        },

        validatePasswordMatch(rule, value, callback) {
            if (value !== this.registerForm.password) {
                callback(new Error("两次输入的密码不一致"));
            } else {
                callback();
            }
        },

        async handleRegister() {
            this.$refs.registerForm.validate(async (valid) => {
                if (valid) {
                    try {
                        this.loading = true;
                        const response = await postAuthRegister({ email: this.registerForm.email, code: this.registerForm.code });
                        this.$message({ type: "success", message: response.data.message || "注册成功，即将跳转到登录页面", duration: 1500 });
                        setTimeout(() => { this.$router.push("/login"); }, 1500);
                    } catch (error) {
                        this.$message({ type: "error", message: error.response?.data?.message || "注册失败，请稍后重试", duration: 3000 });
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
.register {
    min-height: 100vh;
    min-width: 350px;
    display: flex;
    justify-content: center;
    align-items: center;
    background: url("@/assets/images/system/auth/register/background.jpg") no-repeat center/cover;
}

.title {
    color: #fff;
    text-align: center;
    margin: 0 0 14px 0;
    font-family: "Microsoft YaHei", Arial, sans-serif;
    font-size: 22px;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.register-form {
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

.code-form-item {
    display: flex;
    gap: 8px;
    align-items: center;
    margin-bottom: 16px;
}

::v-deep .code-input-wrapper {
    flex: 1;
    margin-bottom: 0 !important;
}

::v-deep .code-input-wrapper .el-form-item__content {
    display: block;
}

.code-input {
    width: 100%;
}

::v-deep .code-btn {
    width: 100px;
    height: 45px;
    background: rgba(255, 255, 255, 0.2);
    border: 1px solid rgba(255,255,255,0.3);
    color: #fff;
    border-radius: 8px;
    font-size: 12px;
    transition: all 0.3s ease;
    padding: 0;
    flex-shrink: 0;
}

.code-btn:hover { background: rgba(255, 255, 255, 0.3); }
.code-btn:disabled { background: rgba(255, 255, 255, 0.1); color: rgba(255, 255, 255, 0.5); }

.register-btn { width: 100%; height: 45px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border: none; border-radius: 8px; font-size: 16px; font-weight: 600; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); }
.register-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6); }
.register-btn:active { transform: translateY(0); }

.links { margin-top: 14px; display: flex; justify-content: space-between; align-items: center; }
.links a { color: rgba(255, 255, 255, 0.8); text-decoration: none; font-size: 14px; transition: color 0.3s ease; padding: 5px 10px; border-radius: 5px; }
.links a:hover { color: #fff; background-color: rgba(255, 255, 255, 0.1); }

@media (max-width: 480px) {
    .register-form { width: 90%; padding: 20px; }
    .title { font-size: 20px; }
    .icon img { width: 60px; height: 60px; }
    .code-btn { width: 80px; font-size: 11px; }
}
</style>
