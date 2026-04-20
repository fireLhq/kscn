<template>
    <div class="profile-view">
        <div class="profile-body">
            <div class="profile-banner">
                <div class="banner-mask">
                    <div class="hero-content">
                        <div class="hero-avatar-wrap">
                            <img :src="avatarUrl" alt="头像" class="avatar-img" />
                            <el-upload
                                :show-file-list="false"
                                :before-upload="handleAvatarBeforeUpload"
                                accept="image/*"
                                action="#"
                            >
                                <el-button type="primary" size="small">更换头像</el-button>
                            </el-upload>
                        </div>

                        <div class="hero-info-grid">
                            <div class="hero-info-item">
                                <span class="k">用户名</span>
                                <span class="v">{{ user.username || '-' }}</span>
                            </div>
                            <div class="hero-info-item">
                                <span class="k">邮箱</span>
                                <span class="v">{{ user.email || '-' }}</span>
                            </div>
                            <div class="hero-info-item">
                                <span class="k">注册时间</span>
                                <span class="v">{{ formatDateTime(user.createTime) }}</span>
                            </div>
                            <div class="hero-info-item">
                                <span class="k">总空间</span>
                                <span class="v">{{ formatFileSize(totalSpace) }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="section-card">
                <div class="section-header">
                    <h2>基本信息</h2>
                    <p>查看并维护个人资料信息</p>
                </div>

                <div class="section-content">
                    <div class="basic-grid">
                        <div class="basic-item">
                            <i class="el-icon-user"></i>
                            <div class="basic-meta">
                                <span class="label">昵称</span>
                                <span class="value">{{ user.nickname || '-' }}</span>
                            </div>
                        </div>
                        <div class="basic-item">
                            <i class="el-icon-male"></i>
                            <div class="basic-meta">
                                <span class="label">性别</span>
                                <span class="value">{{ sexLabel(user.sex) }}</span>
                            </div>
                        </div>
                        <div class="basic-item">
                            <i class="el-icon-date"></i>
                            <div class="basic-meta">
                                <span class="label">生日</span>
                                <span class="value">{{ formatDate(user.birthday) }}</span>
                            </div>
                        </div>
                        <div class="basic-item">
                            <i class="el-icon-edit-outline"></i>
                            <div class="basic-meta">
                                <span class="label">个人签名</span>
                                <span class="value">{{ user.synopsis || '-' }}</span>
                            </div>
                        </div>
                    </div>

                    <div class="actions">
                        <el-button type="primary" @click="openProfileDialog">修改基本信息</el-button>
                    </div>
                </div>
            </div>

            <div class="section-card">
                <div class="section-header">
                    <h2>账户安全</h2>
                    <p>用户名、邮箱、密码管理</p>
                </div>

                <div class="section-content">
                    <div class="security-grid">
                        <div class="security-item">
                            <i class="el-icon-user-solid"></i>
                            <div class="security-text">
                                <h4>用户名</h4>
                                <p>当前：{{ user.username || '-' }}</p>
                            </div>
                            <el-button size="small" @click="openUsernameDialog">修改</el-button>
                        </div>
                        <div class="security-item">
                            <i class="el-icon-message"></i>
                            <div class="security-text">
                                <h4>邮箱</h4>
                                <p>当前：{{ user.email || '-' }}</p>
                            </div>
                            <el-button size="small" @click="openEmailDialog">修改</el-button>
                        </div>
                        <div class="security-item">
                            <i class="el-icon-lock"></i>
                            <div class="security-text">
                                <h4>密码</h4>
                                <p>建议定期修改以提升安全性</p>
                            </div>
                            <el-button size="small" type="warning" @click="openPasswordDialog">修改</el-button>
                        </div>
                        <div class="security-item">
                            <i class="el-icon-switch-button"></i>
                            <div class="security-text">
                                <h4>退出登录</h4>
                                <p>如需注销当前登录状态，可点击此处安全退出账号</p>
                            </div>
                            <el-button size="small" type="danger" @click="handleLogout">退出</el-button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="section-card stats-card">
                <div class="section-header">
                    <h2>账户统计</h2>
                    <p>近期账号与云盘使用情况</p>
                </div>

                <div class="stats-grid">
                    <div class="stat-item">
                        <div class="stat-number">{{ formatFileSize(usedSpace) }}</div>
                        <div class="stat-label">已用空间</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-number">{{ fileCount ?? 0 }}</div>
                        <div class="stat-label">文件数量</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-number stat-number-sm stat-ip">{{ loginStats.lastLoginIp || '--' }}</div>
                        <div class="stat-label">最后登录 IP</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-number stat-number-sm stat-datetime" v-if="formatDateTime(loginStats.lastActiveTime) !== '--'">
                            <span>{{ formatDateTime(loginStats.lastActiveTime).slice(0, 10) }}</span>
                            <span>{{ formatDateTime(loginStats.lastActiveTime).slice(11, 19) }}</span>
                        </div>
                        <div class="stat-number stat-number-sm" v-else>--</div>
                        <div class="stat-label">最后活跃</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-number">{{ loginStats.loginCount ?? 0 }}</div>
                        <div class="stat-label">近30天登录次数</div>
                    </div>
                </div>
            </div>
        </div>

        <el-dialog title="头像裁剪" :visible.sync="cropDialogVisible" width="720px" :close-on-click-modal="false" :close-on-press-escape="false" @close="cleanupCropImage">
            <div class="crop-wrapper">
                <vue-cropper
                    ref="avatarCropper"
                    :img="cropImageUrl"
                    :autoCrop="true"
                    :fixed="true"
                    :fixedNumber="[1, 1]"
                    :autoCropWidth="260"
                    :autoCropHeight="260"
                    :centerBox="true"
                    :canMove="true"
                    :canMoveBox="true"
                    outputType="png"
                />
            </div>
            <span slot="footer">
                <el-button @click="cropDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitting" @click="confirmAvatarCrop">裁剪并上传</el-button>
            </span>
        </el-dialog>

        <el-dialog title="修改资料" :visible.sync="profileDialogVisible" width="500px" :close-on-click-modal="false" :close-on-press-escape="false">
            <el-form ref="profileForm" :model="profileForm" :rules="profileRules" label-width="80px">
                <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="profileForm.nickname" maxlength="10" show-word-limit />
                </el-form-item>
                <el-form-item label="性别" prop="sex">
                    <el-select v-model="profileForm.sex" style="width: 100%" clearable>
                        <el-option label="男" :value="0" />
                        <el-option label="女" :value="1" />
                        <el-option label="不愿透露" :value="2" />
                    </el-select>
                </el-form-item>
                <el-form-item label="生日" prop="birthday">
                    <el-date-picker
                        v-model="profileForm.birthday"
                        type="date"
                        value-format="yyyy-MM-dd"
                        format="yyyy-MM-dd"
                        style="width: 100%"
                        placeholder="请选择生日"
                    />
                </el-form-item>
                <el-form-item label="签名" prop="synopsis">
                    <el-input v-model="profileForm.synopsis" type="textarea" :rows="3" maxlength="125" show-word-limit />
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="profileDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitting" @click="submitProfile">保存</el-button>
            </span>
        </el-dialog>

        <el-dialog title="修改用户名" :visible.sync="usernameDialogVisible" width="420px" :close-on-click-modal="false" :close-on-press-escape="false">
            <el-form ref="usernameForm" :model="usernameForm" :rules="usernameRules" label-width="90px">
                <el-form-item label="新用户名" prop="username">
                    <el-input v-model.trim="usernameForm.username" placeholder="4-16位，字母/数字/下划线，不能数字开头" />
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="usernameDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitting" @click="submitUsername">保存</el-button>
            </span>
        </el-dialog>

        <el-dialog title="修改邮箱" :visible.sync="emailDialogVisible" width="460px" :close-on-click-modal="false" :close-on-press-escape="false">
            <el-form ref="emailForm" :model="emailForm" :rules="emailRules" label-width="90px">
                <el-form-item label="新邮箱" prop="email">
                    <el-input v-model.trim="emailForm.email" placeholder="请输入新邮箱" />
                </el-form-item>
                <el-form-item label="验证码" prop="code">
                    <div class="code-row">
                        <el-input v-model.trim="emailForm.code" placeholder="请输入邮箱验证码" />
                        <el-button :disabled="codeCountdown > 0" @click="sendCode">
                            {{ codeCountdown > 0 ? `${codeCountdown}s` : "获取验证码" }}
                        </el-button>
                    </div>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="emailDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitting" @click="submitEmail">保存</el-button>
            </span>
        </el-dialog>

        <el-dialog title="修改密码" :visible.sync="passwordDialogVisible" width="460px" :close-on-click-modal="false" :close-on-press-escape="false">
            <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="100px">
                <el-form-item label="旧密码" prop="oldPassword">
                    <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="passwordDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitting" @click="submitPassword">保存</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import { VueCropper } from "vue-cropper";
import {
    getUserProfile,
    putUserProfile,
    putUserProfileUsername,
    postUserProfileEmailCode,
    putUserProfileEmail,
    putUserProfilePassword,
    postUserProfileAvatar,
    getUserAvatarUrl,
} from "@/api/system/user";
import { getLoginStats } from "@/api/system/login-log";
import { getUserSpaceInfo, getUserFileCount } from "@/api/system/user-files";
import { postAuthLogout } from "@/api/system/auth";

const DEFAULT_AVATAR = require("@/assets/images/components/avatar/user.png");

export default {
    name: "ProfileView",
    components: {
        VueCropper,
    },
    data() {
        return {
            user: {},
            usedSpace: 0,
            totalSpace: 0,
            fileCount: 0,
            loginStats: {
                loginCount: 0,
                lastActiveTime: null,
                lastLoginIp: null,
            },
            submitting: false,
            codeCountdown: 0,
            codeTimer: null,
            cropDialogVisible: false,
            cropImageUrl: "",
            avatarRefreshKey: Date.now(),

            profileDialogVisible: false,
            usernameDialogVisible: false,
            emailDialogVisible: false,
            passwordDialogVisible: false,

            profileForm: {
                nickname: "",
                sex: null,
                birthday: "",
                synopsis: "",
            },
            usernameForm: {
                username: "",
            },
            emailForm: {
                email: "",
                code: "",
            },
            passwordForm: {
                oldPassword: "",
                newPassword: "",
            },

            profileRules: {
                nickname: [{ max: 10, message: "昵称长度不能超过10", trigger: "blur" }],
                synopsis: [{ max: 125, message: "签名长度不能超过125", trigger: "blur" }],
            },
            usernameRules: {
                username: [
                    { required: true, message: "请输入新用户名", trigger: "blur" },
                    {
                        pattern: /^[a-zA-Z_][a-zA-Z0-9_]{3,15}$/,
                        message: "用户名不合法，必须由字母、下划线、数字组成，长度4-16，不能数字开头",
                        trigger: "blur",
                    },
                ],
            },
            emailRules: {
                email: [
                    { required: true, message: "请输入新邮箱", trigger: "blur" },
                    { type: "email", message: "邮箱格式不正确", trigger: "blur" },
                ],
                code: [{ required: true, message: "请输入验证码", trigger: "blur" }],
            },
            passwordRules: {
                oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
                newPassword: [
                    { required: true, message: "请输入新密码", trigger: "blur" },
                    { min: 6, message: "新密码长度不能少于6位", trigger: "blur" },
                ],
            },
        };
    },
    computed: {
        avatarUrl() {
            const cacheKey = this.avatarRefreshKey || (this.user.updateTime ? new Date(this.user.updateTime).getTime() : undefined);
            const timestamp = Number.isNaN(cacheKey) ? undefined : cacheKey;
            return getUserAvatarUrl(this.user.avatar, timestamp) || DEFAULT_AVATAR;
        },
    },
    async mounted() {
        await this.loadMe();
        await this.loadSpaceInfo();
        await this.loadFileCount();
        await this.loadLoginStats();
    },
    beforeDestroy() {
        if (this.codeTimer) clearInterval(this.codeTimer);
        this.cleanupCropImage();
    },
    methods: {
        async loadMe() {
            const res = await getUserProfile();
            if (res.data.code === 200) {
                this.user = res.data.data || {};
                this.profileForm.nickname = this.user.nickname || "";
                this.profileForm.sex = this.user.sex ?? null;
                this.profileForm.birthday = this.user.birthday ? this.user.birthday.slice(0, 10) : "";
                this.profileForm.synopsis = this.user.synopsis || "";
                this.usernameForm.username = this.user.username || "";
                this.emailForm.email = this.user.email || "";
            }
        },
        async loadSpaceInfo() {
            try {
                const res = await getUserSpaceInfo(this.$store.state.token);
                if (res.data.code === 200) {
                    this.usedSpace = res.data.data.usedSpace || 0;
                    this.totalSpace = res.data.data.totalSpace || 0;
                }
            } catch (e) {
                this.usedSpace = this.user.usedSpace || 0;
                this.totalSpace = this.user.totalSpace || 0;
            }
        },
        async loadLoginStats() {
            try {
                const res = await getLoginStats();
                if (res.data.code === 200) {
                    this.loginStats = {
                        loginCount: res.data.data.loginCount || 0,
                        lastActiveTime: res.data.data.lastActiveTime || null,
                        lastLoginIp: res.data.data.lastLoginIp || null,
                    };
                }
            } catch (e) {
                this.loginStats = {
                    loginCount: 0,
                    lastActiveTime: null,
                    lastLoginIp: null,
                };
            }
        },
        async loadFileCount() {
            try {
                const res = await getUserFileCount(this.$store.state.token);
                if (res.data.code === 200) {
                    this.fileCount = res.data.data || 0;
                }
            } catch (e) {
                this.fileCount = 0;
            }
        },
        sexLabel(sex) {
            if (sex === 0) return "男";
            if (sex === 1) return "女";
            if (sex === 2) return "不愿透露";
            return "-";
        },
        formatDate(v) {
            if (!v) return "-";
            const str = String(v);
            const pureDateMatch = str.match(/^(\d{4}-\d{2}-\d{2})/);
            if (pureDateMatch) return pureDateMatch[1];
            const d = new Date(str);
            if (Number.isNaN(d.getTime())) return str.slice(0, 10);
            const yyyy = d.getFullYear();
            const mm = String(d.getMonth() + 1).padStart(2, "0");
            const dd = String(d.getDate()).padStart(2, "0");
            return `${yyyy}-${mm}-${dd}`;
        },
        formatDateTime(v) {
            if (!v) return "--";
            const d = new Date(v);
            if (Number.isNaN(d.getTime())) return String(v).replace("T", " ").slice(0, 19);
            const yyyy = d.getFullYear();
            const mm = String(d.getMonth() + 1).padStart(2, "0");
            const dd = String(d.getDate()).padStart(2, "0");
            const hh = String(d.getHours()).padStart(2, "0");
            const mi = String(d.getMinutes()).padStart(2, "0");
            const ss = String(d.getSeconds()).padStart(2, "0");
            return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`;
        },
        formatFileSize(bytes) {
            const n = Number(bytes || 0);
            if (!n) return "0 B";
            const units = ["B", "KB", "MB", "GB", "TB"];
            let idx = 0;
            let val = n;
            while (val >= 1024 && idx < units.length - 1) {
                val /= 1024;
                idx += 1;
            }
            return `${val.toFixed(idx === 0 ? 0 : 2)} ${units[idx]}`;
        },

        handleAvatarBeforeUpload(file) {
            if (!file.type.startsWith("image/")) {
                this.$message.error("只能上传图片文件");
                return false;
            }
            if (file.size > 10 * 1024 * 1024) {
                this.$message.error("头像文件大小不能超过10MB");
                return false;
            }
            this.cleanupCropImage();
            this.cropImageUrl = URL.createObjectURL(file);
            this.cropDialogVisible = true;
            return false;
        },
        cleanupCropImage() {
            if (this.cropImageUrl) {
                URL.revokeObjectURL(this.cropImageUrl);
                this.cropImageUrl = "";
            }
        },
        async confirmAvatarCrop() {
            if (!this.$refs.avatarCropper) return;
            this.submitting = true;
            this.$refs.avatarCropper.getCropBlob(async (blob) => {
                try {
                    const formData = new FormData();
                    formData.append("file", blob, "avatar.png");
                    await postUserProfileAvatar(formData);
                    this.$message.success("头像上传成功");
                    this.cropDialogVisible = false;
                    this.avatarRefreshKey = Date.now();
                    await this.loadMe();
                    this.$root.$emit("refreshNavbarAvatar");
                } catch (e) {
                    this.$message.error(e.response?.data?.message || "头像上传失败");
                } finally {
                    this.submitting = false;
                }
            });
        },

        openProfileDialog() {
            this.profileDialogVisible = true;
        },
        openUsernameDialog() {
            this.usernameDialogVisible = true;
        },
        openEmailDialog() {
            this.emailDialogVisible = true;
            this.emailForm.code = "";
        },
        openPasswordDialog() {
            this.passwordDialogVisible = true;
            this.passwordForm.oldPassword = "";
            this.passwordForm.newPassword = "";
        },

        async submitProfile() {
            this.$refs.profileForm.validate(async (valid) => {
                if (!valid) return;
                this.submitting = true;
                try {
                    await putUserProfile({
                        nickname: this.profileForm.nickname || null,
                        sex: this.profileForm.sex,
                        birthday: this.profileForm.birthday || null,
                        synopsis: this.profileForm.synopsis || null,
                    });
                    this.$message.success("资料更新成功");
                    this.profileDialogVisible = false;
                    await this.loadMe();
                } catch (e) {
                    this.$message.error(e.response?.data?.message || "资料更新失败");
                } finally {
                    this.submitting = false;
                }
            });
        },

        async submitUsername() {
            this.$refs.usernameForm.validate(async (valid) => {
                if (!valid) return;
                this.submitting = true;
                try {
                    await putUserProfileUsername({ username: this.usernameForm.username });
                    this.$message.success("用户名更新成功");
                    this.usernameDialogVisible = false;
                    await this.loadMe();
                } catch (e) {
                    this.$message.error(e.response?.data?.message || "用户名更新失败");
                } finally {
                    this.submitting = false;
                }
            });
        },

        async sendCode() {
            if (!this.emailForm.email) {
                this.$message.warning("请先输入邮箱");
                return;
            }
            try {
                await postUserProfileEmailCode({ email: this.emailForm.email });
                this.$message.success("验证码已发送，请查收邮箱");
                this.startCountdown();
            } catch (e) {
                this.$message.error(e.response?.data?.message || "验证码发送失败");
            }
        },
        startCountdown() {
            this.codeCountdown = 60;
            if (this.codeTimer) clearInterval(this.codeTimer);
            this.codeTimer = setInterval(() => {
                this.codeCountdown -= 1;
                if (this.codeCountdown <= 0) {
                    clearInterval(this.codeTimer);
                    this.codeTimer = null;
                }
            }, 1000);
        },

        async submitEmail() {
            this.$refs.emailForm.validate(async (valid) => {
                if (!valid) return;
                this.submitting = true;
                try {
                    await putUserProfileEmail({ email: this.emailForm.email, code: this.emailForm.code });
                    this.$message.success("邮箱更新成功");
                    this.emailDialogVisible = false;
                    await this.loadMe();
                } catch (e) {
                    this.$message.error(e.response?.data?.message || "邮箱更新失败");
                } finally {
                    this.submitting = false;
                }
            });
        },

        async submitPassword() {
            this.$refs.passwordForm.validate(async (valid) => {
                if (!valid) return;
                this.submitting = true;
                try {
                    await putUserProfilePassword({ oldPassword: this.passwordForm.oldPassword, newPassword: this.passwordForm.newPassword });
                    this.$message.success("密码更新成功，请重新登录");
                    this.passwordDialogVisible = false;
                    this.$store.commit("clearToken");
                    this.$router.push("/login");
                } catch (e) {
                    this.$message.error(e.response?.data?.message || "密码更新失败");
                } finally {
                    this.submitting = false;
                }
            });
        },
        async handleLogout() {
            this.submitting = true;
            try {
                const res = await postAuthLogout();
                if (res.data && res.data.code !== 200) {
                    this.$message.error(res.data.message || "退出登录失败");
                    return;
                }
                this.$store.commit("clearToken");
                this.$message.success("已退出登录");
                this.$router.push("/login");
            } catch (e) {
                this.$message.error(e.response?.data?.message || "退出登录失败，请稍后重试");
            } finally {
                this.submitting = false;
            }
        },
    },
};
</script>

<style scoped>
.profile-view {
    min-height: 100vh;
    background: #f8f9fa;
}

.profile-body {
    padding: 2rem 0 3rem;
    max-width: 1400px;
    margin: 0 auto;
}

.profile-banner,
.section-card {
    margin: 0 2rem 1.5rem;
    border-radius: 15px;
    overflow: hidden;
}

.profile-banner {
    background: url("@/assets/images/system/profile/background.jpg") center/cover no-repeat;
}

.banner-mask {
    padding: 1.5rem;
    background: rgba(0, 0, 0, 0.35);
}

.hero-content {
    display: grid;
    grid-template-columns: 240px 1fr;
    gap: 1rem;
    align-items: stretch;
}

.hero-avatar-wrap {
    border: none;
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 1rem;
}

.avatar-img {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    border: 4px solid rgba(255, 255, 255, 0.7);
    margin-bottom: 1rem;
}

.hero-info-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(220px, 1fr));
    gap: 0.8rem;
}

.hero-info-item {
    background: transparent;
    border: none;
    border-radius: 10px;
    padding: 0.75rem 0.9rem;
    color: #fff;
}

.hero-info-item .k {
    display: block;
    font-size: 12px;
    opacity: 0.85;
    margin-bottom: 0.35rem;
}

.hero-info-item .v {
    font-weight: 700;
    word-break: break-all;
}

.section-card {
    background: #fff;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
}

.section-header {
    padding: 1.2rem 1.6rem;
    border-bottom: 1px solid #eef1f5;
}

.section-header h2 {
    margin: 0;
    font-size: 1.3rem;
    color: #2c3e50;
}

.section-header p {
    margin: 0.45rem 0 0;
    color: #7f8c8d;
}

.section-content {
    padding: 1.15rem 1.6rem 1.35rem;
}

.basic-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(260px, 1fr));
    gap: 0.8rem;
}

.basic-item {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    border: 1px solid #ebf0ff;
    background: #f8faff;
    border-radius: 10px;
    padding: 0.8rem 0.9rem;
}

.basic-item i {
    font-size: 1.05rem;
    color: #5e73df;
    margin-top: 0.2rem;
}

.basic-meta .label {
    display: block;
    font-size: 12px;
    color: #7f8c8d;
}

.basic-meta .value {
    display: block;
    margin-top: 0.2rem;
    color: #2c3e50;
    font-weight: 600;
    word-break: break-all;
}

.security-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 0.8rem;
}

.security-item {
    display: grid;
    grid-template-columns: 30px 1fr auto;
    gap: 0.8rem;
    align-items: center;
    border: 1px solid #e9edf3;
    border-radius: 10px;
    background: #fbfcfe;
    padding: 0.8rem 0.9rem;
}

.security-item i {
    font-size: 1rem;
    color: #5e73df;
}

.security-text h4 {
    margin: 0;
    font-size: 0.98rem;
    color: #2c3e50;
}

.security-text p {
    margin: 0.3rem 0 0;
    font-size: 0.88rem;
    color: #7f8c8d;
}

.actions {
    margin-top: 1rem;
    display: flex;
    flex-wrap: wrap;
    gap: 0.6rem;
}

.stats-card .stats-grid {
    padding: 1.2rem 1.4rem 1.4rem;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 1rem;
}

.stat-item {
    background: #f8f9fa;
    border-radius: 10px;
    text-align: center;
    padding: 1.2rem 1rem;
    border: 1px solid #e9edf3;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-height: 148px;
}

.stat-number {
    min-height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.9rem;
    font-weight: 700;
    color: #5e73df;
    line-height: 1.25;
}

.stat-number-sm {
    font-size: 1.9rem;
    line-height: 1.25;
    min-height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    word-break: break-word;
}

.stat-datetime {
    flex-direction: column;
    gap: 0.2rem;
}

.stat-datetime span {
    display: block;
    font-size: 1.5rem;
    line-height: 1.2;
}

.stat-ip {
    font-size: 1.5rem;
    line-height: 1.2;
}

.stat-label {
    margin-top: 0.6rem;
    color: #7f8c8d;
    min-height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1rem;
    line-height: 1.4;
}

.crop-wrapper {
    height: 420px;
    background: #f2f4f7;
}

.code-row {
    display: grid;
    grid-template-columns: 1fr auto;
    gap: 8px;
}

@media (max-width: 992px) {
    .profile-banner,
    .section-card {
        margin-left: 1rem;
        margin-right: 1rem;
    }

    .hero-content {
        grid-template-columns: 1fr;
    }

    .hero-info-grid,
    .basic-grid {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 768px) {
    .profile-body {
        padding-top: 1rem;
    }

    .security-item {
        grid-template-columns: 30px 1fr;
        align-items: start;
    }

    .security-item .el-button {
        grid-column: 1 / -1;
        justify-self: start;
        margin-top: 0.2rem;
    }

    .stats-card .stats-grid {
        grid-template-columns: 1fr;
    }
}
</style>
