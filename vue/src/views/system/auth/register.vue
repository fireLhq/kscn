<template>
    <div class="register">
        <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form">
            <h3 class="title">注册KSCN账号</h3>

            <el-form-item prop="email">
                <el-input v-model="registerForm.email" type="email" placeholder="邮箱" auto-complete="off" prefix-icon="el-icon-message" />
            </el-form-item>

            <el-form-item prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="密码" auto-complete="off" prefix-icon="el-icon-lock" show-password />
            </el-form-item>

            <el-form-item prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" auto-complete="off" prefix-icon="el-icon-lock" show-password />
            </el-form-item>

            <el-form-item prop="code">
                <el-input v-model="registerForm.code" type="text" placeholder="验证码" style="width: 63%" auto-complete="off" prefix-icon="el-icon-s-operation" />

                <el-button @click.prevent="triggerHiddenClick" class="register-code-btn" :disabled="remainingTime > 0">
                    {{ buttonText }}
                </el-button>

                <!--隐藏按钮，用于触发人机验证-->
                <el-button ref="hiddenBtn" style="display: none" id="captcha-button" />
                <div id="captcha-element"></div>
            </el-form-item>

            <el-form-item style="width: 100%">
                <el-button :loading="loading" type="primary" @click.prevent="handleRegister" class="register-btn">
                    <span v-if="!loading">注册</span>
                </el-button>

                <div class="login-link">
                    <router-link to="/login">已有账户？登录</router-link>
                </div>
            </el-form-item>
        </el-form>
    </div>
</template>

<script>
import { registerRequest, registerVerify } from "@/api/system/register";

export default {
    name: "Register",

    data() {
        return {
            // 注册表单
            registerForm: {
                email: "",
                password: "",
                confirmPassword: "",
                code: "",
            },

            // 注册表单验证规则
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

            loading: false, // 注册按钮加载状态

            remainingTime: 0, // 剩余时间
            timer: null, // 倒计时定时器

            captcha: null, // 人机验证实例
        };
    },

    // 初始化人机验证
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

    // 销毁人机验证
    beforeUnmount() {
        // 必须删除相关元素，否则再次 mount 多次调用 initAliyunCaptcha 会导致多次回调 captchaVerifyCallback
        document.getElementById("aliyunCaptcha-mask")?.remove();
        document.getElementById("aliyunCaptcha-window-popup")?.remove();
    },

    // 计算属性
    computed: {
        buttonText() {
            return this.remainingTime > 0 ? `${this.remainingTime} 秒重发` : "获取验证码";
        },
    },

    // 生命周期钩子
    created() {
        // 组件创建时恢复倒计时
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

    // 启动倒计时
    beforeDestroy() {
        clearInterval(this.timer);
    },

    methods: {
        // 触发隐藏按钮点击事件
        async triggerHiddenClick() {
            // 表单验证
            const isEmailValid = await this.validateEmail();
            const isPasswordValid = await this.validatePassword();
            const isConfirmPasswordValid = await this.validateConfirmPassword();

            if (isEmailValid && isPasswordValid && isConfirmPasswordValid) {
                if (this.$refs.hiddenBtn) {
                    this.$refs.hiddenBtn.$el.click(); // 访问组件的 $el（真实 DOM）并触发点击
                }
            }
        },

        // 表单验证方法
        async validateEmail() {
            return new Promise((resolve) => {
                this.$refs.registerForm.validateField("email", (error) => {
                    resolve(!error);
                });
            });
        },
        async validatePassword() {
            return new Promise((resolve) => {
                this.$refs.registerForm.validateField("password", (error) => {
                    resolve(!error);
                });
            });
        },
        async validateConfirmPassword() {
            return new Promise((resolve) => {
                this.$refs.registerForm.validateField("confirmPassword", (error) => {
                    resolve(!error);
                });
            });
        },

        // 获取人机验证实例
        getInstance(instance) {
            this.captcha = instance;
        },

        // 人机验证回调
        async captchaVerifyCallback(captchaVerifyParam) {
            try {
                const response = await fetch("/api/user/auth/captcha", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: captchaVerifyParam,
                });

                if (!response.ok) {
                    throw new Error(`HTTP 错误! 状态码: ${response.status}`);
                }

                const result = await response.json();

                return {
                    captchaResult: result.data, // 服务器返回的验证结果
                };
            } catch (error) {
                console.error("验证码请求失败:", error);
                return {
                    captchaResult: false, // 验证失败
                };
            }
        },
        onBizResultCallback() {
            // 人机验证通过
            this.sendEmailCode(); // 发送邮箱验证码
            this.startCountdown(); // 启动倒计时
        },

        async sendEmailCode() {
            try {
                const response = await registerRequest({ email: this.registerForm.email, password: this.registerForm.password });

                if (response.data.code === 200) {
                    this.$message.success(response.data.message || "验证码已发送，请检查您的邮箱");
                } else {
                    this.$message.error(response.data.message || "验证码发送失败");
                }
            } catch (error) {
                this.$message.error(error.response?.data?.message || "验证码发送失败");
            }
        },

        startCountdown() {
            if (this.remainingTime > 0) return;

            // 设置60秒倒计时
            this.remainingTime = 60;
            const endTime = Date.now() + 60000;

            // 存储到本地
            localStorage.setItem("countdownEnd", endTime);
            this.startTimer();
        },

        startTimer() {
            clearInterval(this.timer); // 清除旧定时器
            this.timer = setInterval(() => {
                this.remainingTime -= 1;

                if (this.remainingTime <= 0) {
                    clearInterval(this.timer);
                    localStorage.removeItem("countdownEnd");
                }
            }, 1000);
        },

        // 验证密码是否一致
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
                        this.loading = true; // 开启加载状态

                        // 调用注册接口
                        await registerVerify({ email: this.registerForm.email, code: this.registerForm.code });

                        // 注册成功处理
                        this.$message({
                            type: "success",
                            message: "注册成功，即将跳转到登录页面",
                            duration: 1500,
                        });

                        // 1.5秒后跳转
                        setTimeout(() => {
                            this.$router.push("/login"); // 修改为你的登录路由路径
                        }, 1500);
                    } catch (error) {
                        // 错误处理
                        const errorMessage = error.response?.data?.message || "注册失败，请稍后重试";
                        this.$message({
                            type: "error",
                            message: errorMessage,
                            duration: 3000,
                        });
                    } finally {
                        this.loading = false; // 关闭加载状态
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
    display: flex;
    justify-content: center;
    align-items: center;
    background: url("@/assets/images/register/background.jpg") no-repeat center/cover;
    background-size: cover;
    background-position: center;
}

.title {
    text-align: center;
    color: #fff;
    margin: 20px 0 30px 0;
    font-family: "Microsoft YaHei", Arial, sans-serif;
    font-size: 24px;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.register-form {
    background: rgba(0, 0, 0, 0.6);
    padding: 30px;
    border-radius: 10px;
    width: 350px;
    backdrop-filter: blur(10px);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.1);
}

::v-deep .el-form-item {
    margin-bottom: 20px;
}

::v-deep .el-input__inner {
    background-color: rgba(255, 255, 255, 0.15) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
    color: #fff !important;
    height: 45px;
    border-radius: 8px;
    font-size: 14px;
}

::v-deep .el-input__inner::placeholder {
    color: rgba(255, 255, 255, 0.7) !important;
}

::v-deep .el-input__prefix {
    color: rgba(255, 255, 255, 0.8) !important;
}

.register-code-btn {
    float: right;
    width: 33%;
    height: 45px;
    padding: 0;
    border: none;
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
    border-radius: 8px;
    font-size: 12px;
    transition: all 0.3s ease;
}

.register-code-btn:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.3);
}

.register-code-btn:disabled {
    background: rgba(255, 255, 255, 0.1);
    color: rgba(255, 255, 255, 0.5);
    cursor: not-allowed;
}

.register-btn {
    width: 100%;
    height: 45px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    transition: all 0.3s ease;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.register-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.register-btn:active {
    transform: translateY(0);
}

.login-link {
    text-align: center;
    margin-top: 20px;
}

.login-link a {
    color: rgba(255, 255, 255, 0.8);
    text-decoration: none;
    font-size: 14px;
    transition: color 0.3s ease;
    padding: 5px 10px;
    border-radius: 5px;
}

.login-link a:hover {
    color: #fff;
    background-color: rgba(255, 255, 255, 0.1);
}

/* 响应式设计 */
@media (max-width: 480px) {
    .register-form {
        width: 90%;
        padding: 25px 20px;
    }
    
    .title {
        font-size: 20px;
    }
    
    .register-code-btn {
        width: 35%;
        font-size: 11px;
    }
}
</style>
