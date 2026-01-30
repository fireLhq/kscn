<template>
    <div class="profile-view">
        <div class="profile-body">
            <div class="profile-container">
                <!-- 用户基本信息卡片 -->
                <div class="user-card">
                    <div class="user-avatar">
                        <img :src="getAvatarUrl()" alt="用户头像" />
                        <div class="avatar-edit">
                            <el-button size="mini" type="primary" @click="editAvatar" :disabled="!isLoggedIn || loading">
                                <i class="el-icon-camera"></i>
                            </el-button>
                        </div>
                    </div>
                    <div class="user-info">
                        <!-- 加载状态 -->
                        <div v-if="loading" class="loading-placeholder">
                            <el-skeleton :rows="4" animated />
                        </div>
                        <!-- 错误状态 -->
                        <div v-else-if="dataError" class="error-placeholder">
                            <div class="error-content">
                                <i class="el-icon-warning-outline"></i>
                                <h3>数据加载失败</h3>
                                <p>服务器连接异常，请检查网络连接或稍后重试</p>
                                <el-button type="primary" size="small" @click="loadUserInfo">重新加载</el-button>
                            </div>
                        </div>
                        <!-- 正常数据 -->
                        <div v-else>
                            <h2>{{ isLoggedIn ? (userInfo.username || '用户') : '未登录' }}</h2>
                            <p>{{ isLoggedIn ? (userInfo.email || '邮箱未设置') : '请先登录查看邮箱信息' }}</p>
                            <p>注册时间：{{ isLoggedIn ? formatDate(userInfo.createTime) : '未登录' }}</p>
                            <p>最后更新：{{ isLoggedIn ? formatDate(userInfo.updateTime) : '未登录' }}</p>
                        </div>
                    </div>
                </div>

                <!-- 信息修改区域 -->
                <div class="profile-sections">
                    <!-- 基本信息修改 -->
                    <div class="section-card">
                        <div class="section-header">
                            <h3>基本信息</h3>
                            <el-button 
                                type="text" 
                                @click="toggleBasicEdit"
                                :disabled="!isLoggedIn"
                            >
                                {{ isBasicEditing ? '取消' : '编辑' }}
                            </el-button>
                        </div>
                        
                        <!-- 加载状态 -->
                        <div v-if="loading" class="form-loading">
                            <el-skeleton :rows="6" animated />
                        </div>
                        <!-- 错误状态 -->
                        <div v-else-if="dataError" class="form-error">
                            <div class="error-content">
                                <i class="el-icon-warning-outline"></i>
                                <p>数据加载失败，无法显示表单</p>
                                <el-button type="text" @click="loadUserInfo">重新加载</el-button>
                            </div>
                        </div>
                        <!-- 正常表单 -->
                        <el-form v-else :model="basicForm" :rules="basicRules" ref="basicForm" label-width="100px" class="basic-info-form">
                            <el-row :gutter="20">
                                <el-col :span="12">
                                    <el-form-item label="昵称" prop="nickname">
                                        <el-input 
                                            v-model="basicForm.nickname" 
                                            :disabled="!isBasicEditing || !isLoggedIn || loading"
                                            :placeholder="isLoggedIn ? (userInfo.nickname ? '请输入昵称' : '昵称未设置') : '请先登录'"
                                        />
                                    </el-form-item>
                                </el-col>
                                <el-col :span="12">
                                    <el-form-item label="性别" prop="sex">
                                        <el-select 
                                            v-model="basicForm.sex" 
                                            :disabled="!isBasicEditing || !isLoggedIn || loading" 
                                            :placeholder="isLoggedIn ? '请选择性别' : '请先登录'"
                                        >
                                            <el-option label="男" :value="0"></el-option>
                                            <el-option label="女" :value="1"></el-option>
                                            <el-option label="不愿透露" :value="2"></el-option>
                                        </el-select>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                            <el-row :gutter="20">
                                <el-col :span="12">
                                    <el-form-item label="生日" prop="birthday">
                                        <el-date-picker
                                            v-model="basicForm.birthday"
                                            type="date"
                                            :placeholder="isLoggedIn ? (userInfo.birthday ? '选择日期' : '生日未设置') : '请先登录'"
                                            :disabled="!isBasicEditing || !isLoggedIn || loading"
                                            style="width: 100%"
                                        />
                                    </el-form-item>
                                </el-col>
                                <el-col :span="12">
                                    <el-form-item label="用户名" prop="username">
                                        <el-input 
                                            v-model="basicForm.username" 
                                            disabled 
                                            :placeholder="isLoggedIn ? userInfo.username : '未登录'"
                                        />
                                        <el-button 
                                            type="text" 
                                            size="small" 
                                            @click="showUsernameDialog = true"
                                            :disabled="!isLoggedIn"
                                            style="margin-top: 5px;"
                                        >
                                            修改用户名
                                        </el-button>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                            <el-form-item label="个人简介" prop="synopsis">
                                <el-input
                                    v-model="basicForm.synopsis"
                                    type="textarea"
                                    :rows="3"
                                    :disabled="!isBasicEditing || !isLoggedIn || loading"
                                    :placeholder="isLoggedIn ? (userInfo.synopsis ? '请输入个人简介' : '个人简介未设置') : '请先登录后编辑个人简介'"
                                />
                            </el-form-item>
                            <el-form-item v-if="isBasicEditing && isLoggedIn">
                                <el-button type="primary" @click="saveBasicInfo">保存</el-button>
                                <el-button @click="cancelBasicEdit">取消</el-button>
                            </el-form-item>
                        </el-form>
                    </div>

                    <!-- 账户安全 -->
                    <div class="section-card">
                        <div class="section-header">
                            <h3>账户安全</h3>
                        </div>
                        <div class="security-items">
                            <div class="security-item">
                                <div class="security-info">
                                    <i class="el-icon-lock"></i>
                                    <div>
                                        <h4>登录密码</h4>
                                        <p>定期更换密码可以保护账户安全</p>
                                    </div>
                                </div>
                                <el-button 
                                    type="primary" 
                                    size="small" 
                                    @click="showPasswordDialog = true"
                                    :disabled="!isLoggedIn"
                                >
                                    修改密码
                                </el-button>
                            </div>
                            <div class="security-item">
                                <div class="security-info">
                                    <i class="el-icon-message"></i>
                                    <div>
                                        <h4>邮箱验证</h4>
                                        <p>当前邮箱：{{ isLoggedIn ? userInfo.email : '未登录' }}</p>
                                    </div>
                                </div>
                                <el-button 
                                    type="primary" 
                                    size="small" 
                                    @click="showEmailDialog = true"
                                    :disabled="!isLoggedIn"
                                >
                                    修改邮箱
                                </el-button>
                            </div>
                        </div>
                    </div>

                    <!-- 账户统计 -->
                    <div class="section-card">
                        <div class="section-header">
                            <h3>账户统计</h3>
                        </div>
                        <div class="stats-grid">
                            <div class="stat-item">
                                <div class="stat-number">{{ isLoggedIn ? userStats.loginCount : '--' }}</div>
                                <div class="stat-label">登录次数</div>
                            </div>
                            <div class="stat-item">
                                <div class="stat-number">{{ isLoggedIn ? userStats.fileCount : '--' }}</div>
                                <div class="stat-label">文件数量</div>
                            </div>
                            <div class="stat-item">
                                <div class="stat-number">{{ isLoggedIn ? userStats.storageUsed : '--' }}</div>
                                <div class="stat-label">已用空间</div>
                            </div>
                            <div class="stat-item">
                                <div class="stat-number">{{ isLoggedIn ? userStats.lastActive : '--' }}</div>
                                <div class="stat-label">最后活跃</div>
                            </div>
                        </div>
                    </div>


                </div>
            </div>
        </div>

        <!-- 修改用户名对话框 -->
        <el-dialog
            title="修改用户名"
            :visible.sync="showUsernameDialog"
            width="450px"
        >
            <el-form :model="usernameForm" :rules="usernameRules" ref="usernameForm" label-width="100px">
                <el-form-item label="新用户名" prop="newUsername">
                    <el-input v-model="usernameForm.newUsername" placeholder="请输入新用户名" />
                </el-form-item>
                <div class="username-tips">
                    <p><i class="el-icon-info"></i> 用户名规则：</p>
                    <ul>
                        <li>长度：4-16个字符</li>
                        <li>必须以字母或下划线开头</li>
                        <li>只能包含字母、数字、下划线</li>
                        <li>示例：user_123、admin、test_user</li>
                    </ul>
                </div>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showUsernameDialog = false">取消</el-button>
                <el-button type="primary" @click="changeUsername">确认修改</el-button>
            </span>
        </el-dialog>

        <!-- 修改密码对话框 -->
        <el-dialog
            title="修改密码"
            :visible.sync="showPasswordDialog"
            width="400px"
        >
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
                <el-form-item label="当前密码" prop="oldPassword">
                    <el-input
                        v-model="passwordForm.oldPassword"
                        type="password"
                        placeholder="请输入当前密码"
                        show-password
                    />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                    <el-input
                        v-model="passwordForm.newPassword"
                        type="password"
                        placeholder="请输入新密码"
                        show-password
                    />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input
                        v-model="passwordForm.confirmPassword"
                        type="password"
                        placeholder="请再次输入新密码"
                        show-password
                    />
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showPasswordDialog = false">取消</el-button>
                <el-button type="primary" @click="changePassword">确认修改</el-button>
            </span>
        </el-dialog>

        <!-- 修改邮箱对话框 -->
        <el-dialog
            title="修改邮箱"
            :visible.sync="showEmailDialog"
            width="500px"
        >
            <el-form :model="emailForm" :rules="emailRules" ref="emailForm" label-width="100px">
                <el-form-item label="当前邮箱">
                    <el-input v-model="userInfo.email" disabled />
                </el-form-item>
                <el-form-item label="新邮箱" prop="newEmail">
                    <el-input v-model="emailForm.newEmail" placeholder="请输入新邮箱地址" />
                </el-form-item>
                <el-form-item label="验证码" prop="code">
                    <div class="verification-input">
                        <el-input v-model="emailForm.code" placeholder="请输入验证码" />
                        <el-button 
                            @click="sendVerificationCode" 
                            :disabled="countdown > 0"
                            size="small"
                        >
                            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                        </el-button>
                    </div>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showEmailDialog = false">取消</el-button>
                <el-button type="primary" @click="changeEmail">确认修改</el-button>
            </span>
        </el-dialog>

    </div>
</template>

<script>
import { 
    getUserInfo, 
    updateUserInfo, 
    updateUsername, 
    updateEmail, 
    updatePassword,
    sendEmailVerificationCode 
} from "@/api/system/user";
import { getUserAvatarUrl } from "@/utils/avatarUtils";

export default {
    name: "ProfileView",
    data() {
        // 密码确认验证
        const validateConfirmPassword = (rule, value, callback) => {
            if (value !== this.passwordForm.newPassword) {
                callback(new Error('两次输入的密码不一致'));
            } else {
                callback();
            }
        };

        return {
            isBasicEditing: false,
            showUsernameDialog: false,
            showPasswordDialog: false,
            showEmailDialog: false,
            countdown: 0,
            loading: false,
            dataError: false,
            userInfo: {
                username: "",
                email: "",
                avatar: "",
                nickname: "",
                sex: 2,
                birthday: "",
                synopsis: "",
                createTime: "",
                updateTime: ""
            },
            basicForm: {
                username: "",
                nickname: "",
                sex: 2,
                birthday: "",
                synopsis: ""
            },
            basicRules: {
                nickname: [
                    { required: true, message: "请输入昵称", trigger: "blur" },
                    { min: 1, max: 50, message: "长度在 1 到 50 个字符", trigger: "blur" }
                ],
                sex: [
                    { required: true, message: "请选择性别", trigger: "change" }
                ]
            },
            usernameForm: {
                newUsername: ""
            },
            usernameRules: {
                newUsername: [
                    { required: true, message: "请输入新用户名", trigger: "blur" },
                    { min: 4, max: 16, message: "长度在 4 到 16 个字符", trigger: "blur" },
                    { 
                        pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/, 
                        message: "用户名必须以字母或下划线开头，只能包含字母、数字、下划线", 
                        trigger: "blur" 
                    }
                ]
            },
            passwordForm: {
                oldPassword: "",
                newPassword: "",
                confirmPassword: ""
            },
            passwordRules: {
                oldPassword: [
                    { required: true, message: "请输入当前密码", trigger: "blur" }
                ],
                newPassword: [
                    { required: true, message: "请输入新密码", trigger: "blur" },
                    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" }
                ],
                confirmPassword: [
                    { required: true, message: "请再次输入新密码", trigger: "blur" },
                    { validator: validateConfirmPassword, trigger: "blur" }
                ]
            },
            emailForm: {
                newEmail: "",
                code: ""
            },
            emailRules: {
                newEmail: [
                    { required: true, message: "请输入新邮箱地址", trigger: "blur" },
                    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" }
                ],
                code: [
                    { required: true, message: "请输入验证码", trigger: "blur" },
                    { len: 6, message: "验证码长度为6位", trigger: "blur" }
                ]
            },
            userStats: {
                loginCount: 0,
                fileCount: 0,
                storageUsed: "0 MB",
                lastActive: "从未登录"
            }
        };
    },
    computed: {
        isLoggedIn() {
            const token = this.$store.state.token;
            if (!token) return false;
            
            try {
                // 检查token是否过期
                const payload = JSON.parse(atob(token.split('.')[1]));
                const exp = payload.exp;
                if (!exp) return false;
                const currentTime = Math.floor(Date.now() / 1000);
                return exp > currentTime;
            } catch (e) {
                return false;
            }
        }
    },
    watch: {
        // 监听登录状态变化
        isLoggedIn(newVal, oldVal) {
            if (oldVal && !newVal) {
                // 登录状态从true变为false，说明token失效，跳转到登录页
                this.$router.push('/login?redirect=' + encodeURIComponent('/user-center'));
            }
        }
    },
    async mounted() {
        // 检查登录状态，如果未登录或token过期，立即跳转
        if (!this.isLoggedIn) {
            this.$router.push('/login?redirect=' + encodeURIComponent('/user-center'));
            return;
        }
        
        // 登录状态有效，加载用户信息
        await this.loadUserInfo();
    },
    methods: {
        async loadUserInfo() {
            this.loading = true;
            this.dataError = false;
            
            try {
                const token = this.$store.state.token;
                if (token && this.isLoggedIn) {
                    const res = await getUserInfo(token);
                    if (res.data.code === 200) {
                        const userData = res.data.data;
                        this.userInfo = {
                            username: userData.username || "",
                            email: userData.email || "",
                            avatar: userData.avatar || "",
                            nickname: userData.nickname || "",
                            sex: userData.sex !== undefined ? userData.sex : 2,
                            birthday: userData.birthday || "",
                            synopsis: userData.synopsis || "",
                            createTime: userData.createTime || "",
                            updateTime: userData.updateTime || ""
                        };
                        this.basicForm = { ...this.userInfo };
                    } else {
                        this.dataError = true;
                        this.$message.error(res.data.message || "获取用户信息失败");
                    }
                }
            } catch (error) {
                console.error("获取用户信息失败:", error);
                this.dataError = true;
                this.$message.error("服务器连接失败，请检查网络连接");
            } finally {
                this.loading = false;
            }
        },
        formatDate(dateString) {
            if (!dateString) return "未设置";
            return new Date(dateString).toLocaleString("zh-CN");
        },
        editAvatar() { 
            this.$message.info("头像编辑功能开发中..."); 
        },
        toggleBasicEdit() { 
            if (!this.isLoggedIn) {
                this.$message.warning("请先登录");
                return;
            }
            this.isBasicEditing ? this.cancelBasicEdit() : (this.isBasicEditing = true); 
        },
        cancelBasicEdit() {
            this.isBasicEditing = false;
            this.basicForm = { ...this.userInfo };
        },
        async saveBasicInfo() {
            if (!this.isLoggedIn) {
                this.$message.warning("请先登录");
                return;
            }
            
            this.$refs.basicForm.validate(async (valid) => {
                if (valid) {
                    try {
                        const token = this.$store.state.token;
                        const updateData = {
                            nickname: this.basicForm.nickname,
                            sex: this.basicForm.sex,
                            birthday: this.basicForm.birthday,
                            synopsis: this.basicForm.synopsis
                        };
                        
                        const res = await updateUserInfo(token, updateData);
                        if (res.data.code === 200) {
                            this.$message.success("基本信息保存成功");
                            this.isBasicEditing = false;
                            await this.loadUserInfo(); // 重新加载用户信息
                        } else {
                            this.$message.error(res.data.message || "保存失败");
                        }
                    } catch (error) {
                        console.error("保存失败:", error);
                        this.$message.error("保存失败，请重试");
                    }
                }
            });
        },
        async changeUsername() {
            if (!this.isLoggedIn) {
                this.$message.warning("请先登录");
                return;
            }
            
            this.$refs.usernameForm.validate(async (valid) => {
                if (valid) {
                    try {
                        const token = this.$store.state.token;
                        const res = await updateUsername(token, this.usernameForm.newUsername);
                        if (res.data.code === 200) {
                            this.$message.success("用户名修改成功");
                            this.showUsernameDialog = false;
                            this.usernameForm.newUsername = "";
                            await this.loadUserInfo(); // 重新加载用户信息
                        } else {
                            this.$message.error(res.data.message || "修改失败");
                        }
                    } catch (error) {
                        console.error("修改失败:", error);
                        this.$message.error("修改失败，请重试");
                    }
                }
            });
        },
        async changePassword() {
            if (!this.isLoggedIn) {
                this.$message.warning("请先登录");
                return;
            }
            
            this.$refs.passwordForm.validate(async (valid) => {
                if (valid) {
                    try {
                        const token = this.$store.state.token;
                        const res = await updatePassword(token, this.passwordForm.oldPassword, this.passwordForm.newPassword);
                        if (res.data.code === 200) {
                            this.$message.success("密码修改成功");
                            this.showPasswordDialog = false;
                            this.passwordForm = { oldPassword: "", newPassword: "", confirmPassword: "" };
                        } else {
                            this.$message.error(res.data.message || "修改失败");
                        }
                    } catch (error) {
                        console.error("修改失败:", error);
                        this.$message.error("修改失败，请重试");
                    }
                }
            });
        },
        async sendVerificationCode() {
            if (!this.isLoggedIn) {
                this.$message.warning("请先登录");
                return;
            }
            
            if (!this.emailForm.newEmail) { 
                this.$message.warning("请先输入新邮箱地址"); 
                return; 
            }
            
            try {
                const token = this.$store.state.token;
                const res = await sendEmailVerificationCode(token, this.emailForm.newEmail);
                if (res.data.code === 200) {
                    this.$message.success("验证码已发送到新邮箱");
                    this.startCountdown();
                } else {
                    this.$message.error(res.data.message || "发送失败");
                }
            } catch (error) {
                console.error("发送验证码失败:", error);
                this.$message.error("发送验证码失败，请重试");
            }
        },
        startCountdown() {
            this.countdown = 60;
            const timer = setInterval(() => { 
                this.countdown--; 
                if (this.countdown <= 0) clearInterval(timer); 
            }, 1000);
        },
        async changeEmail() {
            if (!this.isLoggedIn) {
                this.$message.warning("请先登录");
                return;
            }
            
            this.$refs.emailForm.validate(async (valid) => {
                if (valid) {
                    try {
                        const token = this.$store.state.token;
                        const res = await updateEmail(token, this.emailForm.newEmail, this.emailForm.code);
                        if (res.data.code === 200) {
                            this.$message.success("邮箱修改成功");
                            this.showEmailDialog = false;
                            this.emailForm = { newEmail: "", code: "" };
                            await this.loadUserInfo(); // 重新加载用户信息
                        } else {
                            this.$message.error(res.data.message || "修改失败");
                        }
                    } catch (error) {
                        console.error("修改失败:", error);
                        this.$message.error("修改失败，请重试");
                    }
                }
            });
        },
        getUserAvatarUrl,
        getAvatarUrl() {
            // 如果未登录或数据错误，使用默认头像
            if (!this.isLoggedIn || this.dataError || !this.userInfo.avatar) {
                return require("@/assets/images/navbar/user.png");
            }
            // 否则使用用户头像
            return this.getUserAvatarUrl(this.userInfo);
        }
    }
};
</script>

<style scoped>
.profile-view { min-height: 100vh; background-color: #f8f9fa; }
.profile-body { padding: 2rem 0; max-width: 1200px; margin: 0 auto; }

/* 用户信息卡片布局修正，防止头像覆盖文字 */
.user-card { 
    display: flex; 
    align-items: flex-start; 
    gap: 1.5rem; 
    flex-wrap: nowrap; 
    width: 100%;
    box-sizing: border-box;
    justify-content: flex-start; /* 确保左对齐 */
}
.user-avatar { 
    position: relative; 
    flex: 0 0 120px; 
    width: 120px; 
    margin: 0; /* 移除所有外边距 */
}
.user-avatar img { 
    width: 120px; 
    height: 120px; 
    border-radius: 50%; 
    border: 4px solid #e9ecef; 
    object-fit: cover; 
    display: block; 
}
.avatar-edit { 
    position: absolute; 
    bottom: 0; 
    right: 0; 
}

.user-info { 
    flex: 1 1 auto; 
    min-width: 0; 
    margin: 0; /* 移除所有外边距 */
    padding: 0; /* 移除左内边距 */
    box-sizing: border-box;
}
.user-info h2 { 
    margin: 0 0 8px 0; 
    font-size: 1.5rem;
}
.user-info p { 
    margin: 4px 0; 
    font-size: 0.95rem;
}

/* 响应式设计 - 平板 */
@media (max-width: 1024px) {
    .profile-container { 
        padding: 0 1.5rem; 
        width: 100%;
    }
    .section-card { 
        padding: 1.5rem; 
        margin-bottom: 1.5rem; 
        width: 100%;
        box-sizing: border-box;
    }
    .user-card {
        width: 100%;
        box-sizing: border-box;
        justify-content: flex-start; /* 保持左对齐 */
    }
    .user-avatar { 
        flex: 0 0 100px; 
        width: 100px; 
        margin: 0; /* 确保没有外边距 */
    }
    .user-avatar img { 
        width: 100px; 
        height: 100px; 
    }
    .user-info {
        width: 100%;
        box-sizing: border-box;
        margin: 0;
        padding: 0;
    }
    .user-info h2 { 
        font-size: 1.3rem; 
    }
    .user-info p { 
        font-size: 0.9rem; 
    }
}

/* 响应式设计 - 手机 */
@media (max-width: 768px) {
    .profile-body { 
        padding: 1.5rem 0; 
    }
    .profile-container { 
        padding: 0 1rem; 
        width: 100%;
    }
    .user-card { 
        flex-direction: column; 
        align-items: center;
        text-align: center;
        gap: 1.5rem;
        width: 100%;
        box-sizing: border-box;
        justify-content: center; /* 小屏幕时居中对齐 */
    }
    .user-avatar { 
        flex: none; 
        width: 80px; 
        height: 80px;
        margin: 0; /* 移除auto外边距 */
        position: relative;
    }
    .user-avatar img { 
        width: 80px; 
        height: 80px; 
        display: block;
    }
    .avatar-edit {
        position: absolute;
        bottom: 0;
        right: 0;
    }
    .user-info { 
        text-align: center; 
        margin: 0; 
        padding: 0; 
        min-width: 0;
        width: 100%;
        flex: none;
        box-sizing: border-box;
    }
    .user-info h2 { 
        font-size: 1.2rem; 
        margin: 0 0 8px 0;
    }
    .user-info p { 
        font-size: 0.85rem; 
        margin: 4px 0;
    }
    .section-card { 
        padding: 1.2rem; 
        margin-bottom: 1.2rem; 
        width: 100%;
        box-sizing: border-box;
    }
    .section-header h3 { 
        font-size: 1.1rem; 
    }
    .stats-grid { 
        grid-template-columns: repeat(2, 1fr); 
        gap: 1rem; 
        justify-items: stretch; /* 改为stretch确保完全填充 */
        width: 100%;
    }
    .stat-item { 
        padding: 1rem; 
        width: 100%;
        max-width: none; /* 移除最大宽度限制 */
        box-sizing: border-box;
    }
    .stat-number { 
        font-size: 1.5rem; 
    }
}

/* 响应式设计 - 小手机 */
@media (max-width: 480px) {
    .profile-body { 
        padding: 1rem 0; 
    }
    .profile-container { 
        padding: 0 0.8rem; 
        width: 100%;
    }
    .user-card {
        gap: 1.2rem;
        width: 100%;
        box-sizing: border-box;
        justify-content: center; /* 小屏幕时居中对齐 */
    }
    .user-avatar { 
        flex: none; 
        width: 70px; 
        height: 70px;
        position: relative;
        margin: 0; /* 确保没有外边距 */
    }
    .user-avatar img { 
        width: 70px; 
        height: 70px; 
        display: block;
    }
    .avatar-edit {
        position: absolute;
        bottom: 0;
        right: 0;
    }
    .user-info {
        width: 100%;
        box-sizing: border-box;
        margin: 0;
        padding: 0;
    }
    .user-info h2 { 
        font-size: 1.1rem; 
        margin: 0 0 6px 0;
    }
    .user-info p { 
        font-size: 0.8rem; 
        margin: 3px 0;
    }
    .section-card { 
        padding: 1rem; 
        margin-bottom: 1rem; 
        width: 100%;
        box-sizing: border-box;
    }
    .section-header h3 { 
        font-size: 1rem; 
    }
    .stats-grid { 
        grid-template-columns: 1fr; 
        gap: 0.8rem; 
        justify-items: stretch; /* 改为stretch确保完全填充 */
        width: 100%;
    }
    .stat-item { 
        padding: 0.8rem; 
        width: 100%;
        max-width: none; /* 移除最大宽度限制 */
        box-sizing: border-box;
    }
    .stat-number { 
        font-size: 1.3rem; 
    }
    .security-item { 
        flex-direction: column; 
        gap: 0.8rem; 
        text-align: center; 
    }
    .security-info { 
        justify-content: center; 
    }
}

/* 添加缺失的样式 */
.profile-container { 
    padding: 0 2rem; 
    width: 100%;
    box-sizing: border-box;
}
.profile-sections { 
    margin-top: 2rem; 
    width: 100%;
}
.section-card { 
    background: #fff; 
    border-radius: 15px; 
    padding: 2rem; 
    margin-bottom: 2rem; 
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1); 
    width: 100%;
    box-sizing: border-box;
}
.section-header { 
    display: flex; 
    justify-content: space-between; 
    align-items: center; 
    margin-bottom: 1.5rem; 
}
.section-header h3 { 
    margin: 0; 
    color: #2c3e50; 
    font-size: 1.3rem; 
}
.security-items { 
    display: flex; 
    flex-direction: column; 
    gap: 1rem; 
    width: 100%;
}
.security-item { 
    display: flex; 
    justify-content: space-between; 
    align-items: center; 
    padding: 1rem; 
    background: #f8f9fa; 
    border-radius: 8px; 
    width: 100%;
    box-sizing: border-box;
}
.security-info { 
    display: flex; 
    align-items: center; 
    gap: 1rem; 
}
.security-info i { 
    font-size: 1.5rem; 
    color: #667eea; 
}
.security-info h4 { 
    margin: 0 0 0.5rem 0; 
    color: #2c3e50; 
}
.security-info p { 
    margin: 0; 
    color: #7f8c8d; 
    font-size: 0.9rem; 
}
.stats-grid { 
    display: grid; 
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); 
    gap: 1.5rem; 
    justify-items: stretch; /* 改为stretch确保完全填充 */
    width: 100%;
    box-sizing: border-box;
}
.stat-item { 
    text-align: center; 
    padding: 1.5rem; 
    background: #f8f9fa; 
    border-radius: 8px; 
    width: 100%;
    max-width: none; /* 移除最大宽度限制，让它们完全填充网格 */
    box-sizing: border-box;
}
.stat-number { 
    font-size: 2rem; 
    font-weight: 700; 
    color: #667eea; 
    margin-bottom: 0.5rem; 
}
.stat-label { 
    color: #7f8c8d; 
    font-size: 0.9rem; 
}
.verification-input { 
    display: flex; 
    gap: 0.5rem; 
}
.verification-input .el-input { 
    flex: 1; 
}

/* 加载和错误状态样式 */
.loading-placeholder,
.form-loading {
    padding: 1rem;
}

.error-placeholder,
.form-error {
    padding: 2rem;
    text-align: center;
    background-color: #fef0f0;
    border: 1px solid #fbc4c4;
    border-radius: 8px;
    margin: 1rem 0;
}

.error-content {
    color: #f56c6c;
}

.error-content i {
    font-size: 2rem;
    margin-bottom: 0.5rem;
    display: block;
}

.error-content h3 {
    margin: 0.5rem 0;
    color: #f56c6c;
}

.error-content p {
    margin: 0.5rem 0 1rem 0;
    color: #666;
}

/* 基本信息表单基础样式 */
.basic-info-form {
    width: 100%;
}



.basic-info-form .el-input,
.basic-info-form .el-select,
.basic-info-form .el-date-picker,
.basic-info-form .el-textarea {
    width: 100%;
    max-width: none;
    box-sizing: border-box;
}

/* 表单响应式优化 */
@media (max-width: 768px) {
    .el-form-item {
        margin-bottom: 1rem;
        width: 100%;
        box-sizing: border-box;
    }
    .el-form-item__label {
        line-height: 1.4;
        display: block;
        width: 100% !important;
        text-align: left;
        margin-bottom: 0.5rem;
    }
    .el-form-item__content {
        width: 100% !important;
        margin-left: 0 !important;
    }
    .verification-input {
        flex-direction: column;
        gap: 0.8rem;
        width: 100%;
    }
    .verification-input .el-button {
        align-self: stretch;
    }
    
    /* 基本信息表单改为单列排布 */
    .el-row .el-col {
        width: 100% !important;
        margin-bottom: 0.5rem;
        box-sizing: border-box;
    }
    .el-row {
        margin: 0 !important;
        width: 100%;
    }
    
    .el-form {
        width: 100%;
        box-sizing: border-box;
    }
    
    /* 强制所有表单项标签和内容都占满宽度 */
    .el-form-item__label,
    .el-input,
    .el-select,
    .el-date-picker,
    .el-textarea {
        width: 100% !important;
        max-width: none !important;
    }
    

}

@media (max-width: 480px) {
    .el-form-item__label {
        font-size: 0.9rem;
        display: block;
        width: 100% !important;
        text-align: left;
        margin-bottom: 0.5rem;
    }
    .el-input__inner {
        font-size: 0.9rem;
        width: 100% !important;
    }
    .el-button {
        font-size: 0.9rem;
        padding: 8px 15px;
    }
    
    .el-form-item {
        width: 100%;
        box-sizing: border-box;
    }
    
    .el-form-item__content {
        width: 100% !important;
        margin-left: 0 !important;
    }
    
    .el-form {
        width: 100%;
        box-sizing: border-box;
    }
    
    /* 强制所有表单项标签和内容都占满宽度 */
    .el-form-item__label,
    .el-input,
    .el-select,
    .el-date-picker,
    .el-textarea {
        width: 100% !important;
        max-width: none !important;
    }
    

}

/* 极小屏幕优化 */
@media (max-width: 360px) {
    .el-form-item__label {
        font-size: 0.85rem;
        margin-bottom: 0.3rem;
    }
    .el-input__inner {
        font-size: 0.85rem;
        padding: 8px 10px;
    }
    .el-button {
        font-size: 0.85rem;
        padding: 6px 12px;
    }
    
    .profile-container {
        padding: 0 0.5rem;
    }
    
    .section-card {
        padding: 0.8rem;
    }
    

}

/* 超小屏幕优化 */
@media (max-width: 320px) {
    .profile-container {
        padding: 0 0.3rem;
    }
    
    .section-card {
        padding: 0.6rem;
    }
    

    
    .el-input__inner {
        font-size: 0.8rem;
        padding: 6px 8px;
    }
    
    .el-button {
        font-size: 0.8rem;
        padding: 5px 8px;
    }
}



/* 未登录状态下禁用按钮的样式 */
.el-button:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.el-button:disabled:hover {
    opacity: 0.6;
    cursor: not-allowed;
}

/* 未登录状态下禁用输入框的样式 */
.el-input.is-disabled .el-input__inner,
.el-select.is-disabled .el-input__inner,
.el-textarea.is-disabled .el-textarea__inner {
    background-color: #f5f7fa;
    border-color: #e4e7ed;
    color: #c0c4cc;
    cursor: not-allowed;
}

/* 用户名提示信息样式 */
.username-tips { 
    background: #f0f9ff; 
    border: 1px solid #bae6fd; 
    border-radius: 8px; 
    padding: 1rem; 
    margin-top: 1rem; 
}
.username-tips p { 
    margin: 0 0 0.5rem 0; 
    color: #0369a1; 
    font-weight: 500; 
    display: flex; 
    align-items: center; 
    gap: 0.5rem; 
}
.username-tips ul { 
    margin: 0; 
    padding-left: 1.5rem; 
    color: #0c4a6e; 
}
.username-tips li { 
    margin-bottom: 0.3rem; 
    font-size: 0.9rem; 
}
.username-tips i { 
    color: #0284c7; 
}

/* 对话框响应式优化 */
@media (max-width: 768px) {
    .el-dialog {
        margin: 5vh auto !important;
        width: 90% !important;
        max-width: 400px;
    }
    .el-dialog__body {
        padding: 1rem;
    }
    .el-dialog__header {
        padding: 1rem 1rem 0.5rem;
    }
    .el-dialog__footer {
        padding: 0.5rem 1rem 1rem;
    }
}

@media (max-width: 480px) {
    .el-dialog {
        width: 95% !important;
        margin: 2vh auto !important;
    }
    .el-dialog__body {
        padding: 0.8rem;
    }
    .el-dialog__header {
        padding: 0.8rem 0.8rem 0.4rem;
    }
    .el-dialog__footer {
        padding: 0.4rem 0.8rem 0.8rem;
    }
}
</style>
