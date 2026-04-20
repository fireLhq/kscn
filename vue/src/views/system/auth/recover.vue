<template>
    <div class="recover">
            <el-form ref="recoverForm" :model="recoverForm" :rules="recoverRules" class="recover-form">
                <h2 class="title">找回密码</h2>
                <div class="icon">
                <img src="@/assets/images/system/auth/recover/icon.svg" alt="icon" />
                </div>

                <el-form-item prop="email">
                    <el-input v-model="recoverForm.email" type="email" placeholder="请输入注册邮箱" prefix-icon="el-icon-message" />
                </el-form-item>

            <div class="code-form-item">
                <el-form-item prop="code" class="code-input-wrapper">
                    <el-input v-model="recoverForm.code" type="text" placeholder="验证码" prefix-icon="el-icon-s-operation" class="code-input" />
                </el-form-item>
                    <el-button @click.prevent="sendCode" class="code-btn" :disabled="remainingTime > 0">
                        {{ buttonText }}
                    </el-button>
            </div>

                <el-form-item prop="newPassword">
                    <el-input v-model="recoverForm.newPassword" type="password" placeholder="新密码" prefix-icon="el-icon-lock" show-password />
                </el-form-item>

                <el-form-item prop="confirmPassword">
                    <el-input v-model="recoverForm.confirmPassword" type="password" placeholder="确认新密码" prefix-icon="el-icon-lock" show-password />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click.prevent="handleRecover" class="recover-btn" :loading="loading">重置密码</el-button>
                </el-form-item>

                <div class="links">
                    <router-link to="/login">返回登录</router-link>
                    <router-link to="/register">注册新账号</router-link>
                </div>
            </el-form>
    </div>
</template>

<script>
import { postAuthPasswordCode, putAuthPassword } from "@/api/system/auth";

export default {
    name: "Recover",
    data() {
        // 密码确认验证
        const validateConfirmPassword = (rule, value, callback) => {
            if (value !== this.recoverForm.newPassword) {
                callback(new Error('两次输入的密码不一致'));
            } else {
                callback();
            }
        };

        return {
            recoverForm: {
                email: "",
                code: "",
                newPassword: "",
                confirmPassword: ""
            },
            recoverRules: {
                email: [
                    { required: true, message: "请输入邮箱地址", trigger: "blur" },
                    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" }
                ],
                code: [
                    { required: true, message: "请输入验证码", trigger: "blur" }
                ],
                newPassword: [
                    { required: true, message: "请输入新密码", trigger: "blur" },
                    { min: 6, max: 20, message: "密码长度必须在6到20位之间", trigger: "blur" }
                ],
                confirmPassword: [
                    { required: true, message: "请再次输入新密码", trigger: "blur" },
                    { validator: validateConfirmPassword, trigger: "blur" }
                ]
            },
            loading: false,
            remainingTime: 0,
            timer: null,
        };
    },
    computed: {
        buttonText() {
            return this.remainingTime > 0 ? `${this.remainingTime}s` : "发送验证码";
        },
    },
    created() {
        const savedEndTime = localStorage.getItem("recoverCountdownEnd");
        if (savedEndTime) {
            const remaining = Math.ceil((savedEndTime - Date.now()) / 1000);
            if (remaining > 0) {
                this.remainingTime = remaining;
                this.startTimer();
            } else {
                localStorage.removeItem("recoverCountdownEnd");
            }
        }
    },
    beforeDestroy() {
        clearInterval(this.timer);
    },
    methods: {
        async sendCode() {
            if (!this.recoverForm.email) {
                this.$message.warning("请先输入邮箱地址");
                return;
            }

            try {
                const response = await postAuthPasswordCode({ email: this.recoverForm.email });
                    this.$message.success(response.data.message || "验证码已发送到您的邮箱");
                    this.startCountdown();
            } catch (error) {
                const errorMessage = error.response?.data?.message || "发送验证码失败，请重试";
                this.$message.error(errorMessage);
            }
        },
        startCountdown() {
            if (this.remainingTime > 0) return;
            this.remainingTime = 60;
            localStorage.setItem("recoverCountdownEnd", Date.now() + 60000);
            this.startTimer();
        },
        startTimer() {
            clearInterval(this.timer);
            this.timer = setInterval(() => {
                this.remainingTime--;
                if (this.remainingTime <= 0) {
                    clearInterval(this.timer);
                    localStorage.removeItem("recoverCountdownEnd");
                }
            }, 1000);
        },
        async handleRecover() {
            this.$refs.recoverForm.validate(async (valid) => {
                if (valid) {
                    this.loading = true;
                    try {
                        const response = await putAuthPassword({
                            email: this.recoverForm.email,
                            code: this.recoverForm.code,
                            newPassword: this.recoverForm.newPassword,
                        });
                            this.$message.success(response.data.message || "密码重置成功，请使用新密码登录");
                            this.$router.push("/login");
                    } catch (error) {
                        const errorMessage = error.response?.data?.message || "密码重置失败，请重试";
                        this.$message.error(errorMessage);
                    } finally {
                        this.loading = false;
                    }
                }
            });
        }
    }
};
</script>

<style scoped>
.recover {
    min-height: 100vh;
    min-width: 350px;
    display: flex;
    justify-content: center;
    align-items: center;
    background: url("@/assets/images/system/auth/recover/background.jpg") no-repeat center/cover;
}

.title {
    color: #fff;
    text-align: center;
    margin: 0 0 14px 0;
    font-family: "Microsoft YaHei", Arial, sans-serif;
    font-size: 22px;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.recover-form {
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

.recover-btn { width: 100%; height: 45px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border: none; border-radius: 8px; font-size: 16px; font-weight: 600; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); }
.recover-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6); }
.recover-btn:active { transform: translateY(0); }

.links { margin-top: 14px; display: flex; justify-content: space-between; align-items: center; }
.links a { color: rgba(255, 255, 255, 0.8); text-decoration: none; font-size: 14px; transition: color 0.3s ease; padding: 5px 10px; border-radius: 5px; }
.links a:hover { color: #fff; background-color: rgba(255, 255, 255, 0.1); }

@media (max-width: 480px) {
    .recover-form { width: 90%; padding: 20px; }
    .title { font-size: 20px; }
    .icon img { width: 60px; height: 60px; }
    .code-btn { width: 80px; font-size: 11px; }
}
</style>
