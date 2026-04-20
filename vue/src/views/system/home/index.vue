<template>
    <div class="home-view">
        <div class="home-body">
            <div class="banner">
                <img src="@/assets/images/system/home/banner.jpg" alt="banner" class="banner-img" />
                <div class="banner-overlay">
                    <h1 class="banner-title">KSCN 知识共享协作网络</h1>
                    <p class="banner-subtitle">连接全球学习者，共享知识智慧</p>
                </div>
            </div>

            <div class="home-main">
                <section class="content-section core-features">
                    <div class="section-header">
                        <span class="section-tag">CORE MODULES</span>
                        <h2>核心功能</h2>
                        <p>汇聚学习资料、个人存储与实用服务，帮助你更高效地获取内容、整理信息与开展协作</p>
                    </div>
                    <div class="features-grid">
                        <div class="feature-card feature-card-primary" @click="goToPublicFiles">
                            <div class="feature-icon">
                                <i class="el-icon-folder-opened"></i>
                            </div>
                            <div class="feature-content">
                                <span class="feature-label">RESOURCE</span>
                                <h3>公共资源</h3>
                                <p>集中收录优质学习资料，便于随时查阅、下载与分享，让知识获取更加便捷高效</p>
                            </div>
                        </div>
                        <div class="feature-card" @click="goToUserFiles">
                            <div class="feature-icon">
                                <i class="el-icon-cloudy"></i>
                            </div>
                            <div class="feature-content">
                                <span class="feature-label">STORAGE</span>
                                <h3>个人云盘</h3>
                                <p>为个人资料提供专属空间，方便长期保存、分类整理与随取随用，让内容管理更加从容有序</p>
                            </div>
                        </div>
                        <div class="feature-card" @click="goToService">
                            <div class="feature-icon">
                                <i class="el-icon-setting"></i>
                            </div>
                            <div class="feature-content">
                                <span class="feature-label">SERVICES</span>
                                <h3>其它服务</h3>
                                <p>整合平台内多样服务与常用入口，满足不同场景下的使用需求，带来更加顺手的体验</p>
                            </div>
                        </div>
                    </div>
                </section>

                <section class="content-section quick-nav">
                    <div class="section-header">
                        <span class="section-tag">DISCOVER</span>
                        <h2>了解更多</h2>
                        <p>在这里进一步了解平台理念、服务内容与相关说明，帮助你更安心、更顺畅地使用平台</p>
                    </div>
                    <div class="nav-grid">
                        <div class="nav-item" @click="goToAbout">
                            <div class="nav-icon"><i class="el-icon-info"></i></div>
                            <h3>关于我们</h3>
                            <p>了解平台的初心、愿景与发展方向，帮助你更全面地认识 KSCN 的价值与意义所在</p>
                        </div>
                        <div class="nav-item" @click="goToTeam">
                            <div class="nav-icon"><i class="el-icon-user"></i></div>
                            <h3>核心团队</h3>
                            <p>认识平台背后的成员与力量，感受团队的用心投入与持续建设带来的可靠支持保障</p>
                        </div>
                        <div class="nav-item" @click="goToSponsor">
                            <div class="nav-icon"><i class="el-icon-coin"></i></div>
                            <h3>赞助支持</h3>
                            <p>你的支持将助力平台持续成长，让更多优质内容与贴心服务长期稳定地陪伴大家前行</p>
                        </div>
                        <div class="nav-item" @click="goToNotice">
                            <div class="nav-icon"><i class="el-icon-warning-outline"></i></div>
                            <h3>使用须知</h3>
                            <p>提前了解相关说明与注意事项，有助于你更顺利地使用平台并收获更好的体验感受</p>
                        </div>
                    </div>
                </section>

                <section class="content-section notification-section">
                    <div class="section-header">
                        <span class="section-tag">MESSAGE CENTER</span>
                        <h2>系统通知</h2>
                        <p>集中查看平台的重要提醒与最新动态，避免错过与你有关的重要信息内容</p>
                    </div>

                    <div class="message-box">
                            <div class="message-head">
                                <div class="message-head-left">
                                    <div class="message-head-icon">
                                        <i class="el-icon-bell"></i>
                                    </div>
                                    <div>
                                        <h3>消息列表</h3>
                                        <p>这里会持续更新平台发布的重要内容，方便你及时了解最新消息与相关变化</p>
                                    </div>
                                </div>
                                <el-button v-if="messageState.hasMore && !messageState.loading" type="text" @click="loadMoreMessages">
                                    查看更多
                                </el-button>
                            </div>

                            <div
                                ref="messageScroll"
                                class="message-scroll"
                                @scroll.passive="handleMessageScroll"
                            >
                                <div v-if="messageState.loading && !messageState.records.length" class="message-empty">
                                    正在加载系统消息...
                                </div>

                                <template v-else-if="messageState.records.length">
                                    <article
                                        v-for="message in messageState.records"
                                        :key="message.id"
                                        class="message-item"
                                    >
                                        <div class="message-item-header">
                                            <h4>{{ message.title }}</h4>
                                            <div class="message-time-meta" :class="getMessageAgeMeta(message.createTime).className">
                                                <span class="message-time-icon">{{ getMessageAgeMeta(message.createTime).icon }}</span>
                                                <span>{{ formatDateTime(message.createTime) }}</span>
                                            </div>
                                        </div>
                                        <div class="message-item-content">{{ message.content }}</div>
                                    </article>

                                    <div v-if="messageState.loading" class="message-loading-more">加载中...</div>
                                    <div v-else-if="!messageState.hasMore" class="message-end">没有了</div>
                                </template>

                                <div v-else class="message-empty">暂无系统消息</div>
                            </div>
                        </div>
                </section>

                <section class="content-section stats-section">
                    <div class="section-header">
                        <span class="section-tag">PLATFORM METRICS</span>
                        <h2>平台数据</h2>
                        <p>直观呈现平台的发展活力与服务覆盖情况，让你更清晰地了解平台规模与整体成长状态</p>
                    </div>
                    <div class="stats-grid">
                        <div class="stat-item">
                            <div class="stat-number">{{ formatStatCount(statsState.userCount) }}</div>
                            <div class="stat-label">注册用户</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-number">{{ formatStatCount(statsState.publicFileCount) }}</div>
                            <div class="stat-label">资源文件</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-number">10+</div>
                            <div class="stat-label">合作伙伴</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-number">9.9%</div>
                            <div class="stat-label">服务可用性</div>
                        </div>
                    </div>
                </section>
            </div>
        </div>

        <el-dialog
            :visible.sync="popupVisible"
            width="620px"
            custom-class="home-message-dialog"
            :close-on-click-modal="false"
            :show-close="true"
            @close="handlePopupClose"
        >
            <template #title>
                <div class="popup-dialog-titlebar">
                    <div class="popup-dialog-title-main">
                        <div class="popup-dialog-title-group">
                            <span class="popup-dialog-badge">系统消息提醒</span>
                            <span v-if="currentPopupMessage" class="popup-dialog-progress">{{ popupProgressText }}</span>
                        </div>
                    </div>
                    <div v-if="currentPopupMessage" class="popup-dialog-title-text">{{ currentPopupMessage.title }}</div>
                </div>
            </template>

            <div v-if="currentPopupMessage" class="popup-message-body">
                <div class="popup-message-meta">发布时间：{{ formatDateTime(currentPopupMessage.createTime) }}</div>
                <div class="popup-message-content">{{ currentPopupMessage.content }}</div>
            </div>
            <span slot="footer" class="popup-footer-actions">
                <el-button @click="handlePopupAcknowledge">知道了</el-button>
                <el-button v-if="isLoggedIn" type="primary" @click="handlePopupDismissCurrent">不再提示</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import { getHomeSystemMessageList, getActivePopupMessages, dismissPopupMessage } from "@/api/system/system-message";
import { getRegisteredUserCount } from "@/api/system/user";
import { getPublicResourceFileCount } from "@/api/system/public-files";

const BROWSER_ID_KEY = "kscn_browser_id";

function getOrCreateBrowserId() {
    let browserId = localStorage.getItem(BROWSER_ID_KEY);
    if (browserId) return browserId;
    browserId = `browser_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`;
    localStorage.setItem(BROWSER_ID_KEY, browserId);
    return browserId;
}

export default {
    name: "HomeView",
    data() {
        return {
            browserId: getOrCreateBrowserId(),
            messageState: {
                records: [],
                page: 1,
                size: 10,
                hasMore: false,
                loading: false,
            },
            statsState: {
                userCount: 0,
                publicFileCount: 0,
            },
            popupMessages: [],
            popupIndex: 0,
            popupVisible: false,
        };
    },
    computed: {
        currentPopupMessage() {
            return this.popupMessages[this.popupIndex] || null;
        },
        hasNextPopup() {
            return this.popupIndex < this.popupMessages.length - 1;
        },
        isLoggedIn() {
            return this.$store.getters.isAuthenticated;
        },
        popupProgressText() {
            if (!this.currentPopupMessage) return "";
            return `${this.popupIndex + 1}/${this.popupMessages.length}`;
        },
    },
    async created() {
        await this.initHomeData();
    },
    methods: {
        async initHomeData() {
            await Promise.all([
                this.fetchMessagePage(1, true),
                this.fetchPopupMessages(),
                this.fetchHomeStats(),
            ]);
        },
        async fetchMessagePage(page, reset = false) {
            if (this.messageState.loading) return;
            this.messageState.loading = true;
            try {
                const res = await getHomeSystemMessageList({ page, size: this.messageState.size });
                if (res.data.code === 200) {
                    const pageData = res.data.data || {};
                    const records = Array.isArray(pageData.records) ? pageData.records : [];
                    this.messageState.records = reset ? records : [...this.messageState.records, ...records];
                    this.messageState.page = Number(pageData.current || page);
                    this.messageState.hasMore = Boolean(pageData.hasMore);
                }
            } catch (error) {
                this.$message.error(error.response?.data?.message || "加载系统消息失败");
            } finally {
                this.messageState.loading = false;
            }
        },
        async loadMoreMessages() {
            if (!this.messageState.hasMore || this.messageState.loading) return;
            await this.fetchMessagePage(this.messageState.page + 1);
        },
        handleMessageScroll() {
            const container = this.$refs.messageScroll;
            if (!container || !this.messageState.hasMore || this.messageState.loading) return;
            const remain = container.scrollHeight - container.scrollTop - container.clientHeight;
            if (remain <= 60) {
                this.loadMoreMessages();
            }
        },
        async fetchPopupMessages() {
            try {
                const res = await getActivePopupMessages({ browserId: this.browserId });
                if (res.data.code === 200) {
                    this.popupMessages = Array.isArray(res.data.data) ? res.data.data : [];
                    this.popupIndex = 0;
                    this.popupVisible = this.popupMessages.length > 0;
                }
            } catch (error) {
                this.popupMessages = [];
                this.popupVisible = false;
            }
        },
        async fetchHomeStats() {
            try {
                const [userRes, publicFileRes] = await Promise.all([
                    getRegisteredUserCount(),
                    getPublicResourceFileCount(),
                ]);
                if (userRes.data.code === 200) {
                    this.statsState.userCount = Number(userRes.data.data || 0);
                }
                if (publicFileRes.data.code === 200) {
                    this.statsState.publicFileCount = Number(publicFileRes.data.data || 0);
                }
            } catch (error) {
                this.statsState.userCount = 0;
                this.statsState.publicFileCount = 0;
            }
        },
        async persistNoLongerPrompt(messageId) {
            if (!messageId || !this.isLoggedIn) return false;
            try {
                await dismissPopupMessage(messageId, { browserId: this.browserId });
                return true;
            } catch (error) {
                this.$message.error(error.response?.data?.message || "记录提醒状态失败");
                return false;
            }
        },
        showNextPopupMessage() {
            if (this.hasNextPopup) {
                this.popupIndex += 1;
                return;
            }
            this.popupVisible = false;
        },
        handlePopupAcknowledge() {
            this.showNextPopupMessage();
        },
        async handlePopupDismissCurrent() {
            const currentMessageId = this.currentPopupMessage?.id;
            const success = await this.persistNoLongerPrompt(currentMessageId);
            if (!success) return;
            this.showNextPopupMessage();
        },
        handlePopupClose() {
            this.popupVisible = false;
        },
        goToPublicFiles() {
                this.$router.push("/public-files");
        },
        goToUserFiles() {
            const token = localStorage.getItem("jwt_token");
            if (!token) {
                this.$router.push("/login?redirect=" + encodeURIComponent("/user-files"));
            } else {
                this.$router.push("/user-files");
            }
        },
        goToService() { this.$router.push("/service"); },
        goToAbout() { this.$router.push("/about"); },
        goToTeam() { this.$router.push("/team"); },
        goToSponsor() { this.$router.push("/sponsor"); },
        goToNotice() { this.$router.push("/notice"); },
        formatDateTime(value) {
            if (!value) return "--";
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return "--";
            const pad = (num) => String(num).padStart(2, "0");
            return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
        },
        formatStatCount(value) {
            const count = Number(value || 0);
            return Number.isNaN(count) ? "0" : String(count);
        },
        getMessageAgeMeta(value) {
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) {
                return { icon: "🗂️", className: "is-archive" };
            }
            const diff = Date.now() - date.getTime();
            const hour = 1000 * 60 * 60;
            const day = hour * 24;
            const week = day * 7;
            const month = day * 30;
            const year = day * 365;

            if (diff <= day) {
                return { icon: "🔥", className: "is-hot" };
            }
            if (diff <= week) {
                return { icon: "📢", className: "is-notice" };
            }
            if (diff <= month) {
                return { icon: "🗓️", className: "is-recent" };
            }
            if (diff <= year) {
                return { icon: "🕰️", className: "is-history" };
            }
            return { icon: "🗂️", className: "is-archive" };
        },
    },
};
</script>

<style scoped>
.home-view {
    min-height: 100vh;
    background-color: #f8f9fa;
}

.home-body {
    padding: 0;
}

.banner {
    position: relative;
    height: 500px;
    overflow: hidden;
}

.banner::after {
    content: "";
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.35);
    z-index: 1;
}

.banner-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    opacity: 1;
}

.banner-overlay {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    color: #fff;
    z-index: 2;
    width: 90%;
    max-width: 800px;
}

.banner-title {
    font-size: 3rem;
    font-weight: 700;
    margin-bottom: 1rem;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
    line-height: 1.2;
}

.banner-subtitle {
    font-size: 1.5rem;
    margin-bottom: 2rem;
    opacity: 0.9;
    font-weight: 300;
}

.home-main {
    position: relative;
    padding: 40px 0 88px;
    background: #f8f9fa;
}

.content-section {
    max-width: 1400px;
    margin: 0 auto 56px;
    padding: 0 2rem;
    box-sizing: border-box;
}

.feature-card,
.nav-item {
    background: rgba(255, 255, 255, 0.92);
    border: 1px solid rgba(223, 230, 240, 0.9);
    box-shadow: 0 18px 50px rgba(31, 41, 55, 0.08);
}

.section-tag {
    display: inline-flex;
    align-items: center;
    letter-spacing: 0.24em;
    font-size: 12px;
    font-weight: 700;
    color: #667eea;
    text-shadow: 0 6px 18px rgba(102, 126, 234, 0.16);
}

.section-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    margin-bottom: 28px;
}

.section-header-left {
    align-items: flex-start;
    text-align: left;
}

.section-header h2 {
    margin: 12px 0 0;
    font-size: 2.45rem;
    font-weight: 700;
    color: #223248;
    letter-spacing: 0.02em;
}

.section-header p {
    max-width: 700px;
    margin: 16px 0 0;
    color: #6b7d92;
    line-height: 1.8;
    font-size: 1rem;
}

.features-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 22px;
}

.feature-card {
    position: relative;
    display: flex;
    align-items: flex-start;
    gap: 18px;
    min-height: 238px;
    border-radius: 24px;
    padding: 28px 26px;
    overflow: hidden;
    transition: transform 0.28s ease, box-shadow 0.28s ease, border-color 0.28s ease;
    cursor: pointer;
}

.feature-card::after {
    content: "";
    position: absolute;
    right: -30px;
    bottom: -54px;
    width: 150px;
    height: 150px;
    background: radial-gradient(circle, rgba(91, 124, 255, 0.14), rgba(91, 124, 255, 0));
}

.feature-card-primary::after {
    background: radial-gradient(circle, rgba(139, 92, 246, 0.16), rgba(139, 92, 246, 0));
}

.feature-card:hover,
.nav-item:hover,
.stat-item:hover {
    transform: translateY(-8px);
    box-shadow: 0 24px 54px rgba(31, 41, 55, 0.12);
    border-color: #d9dcff;
}

.feature-icon {
    position: relative;
    z-index: 1;
    width: 72px;
    height: 72px;
    flex-shrink: 0;
    border-radius: 22px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 30px;
    box-shadow: 0 14px 30px rgba(102, 126, 234, 0.26);
}

.feature-content {
    position: relative;
    z-index: 1;
}

.feature-label {
    display: inline-block;
    margin-bottom: 12px;
    color: #7c3aed;
    font-size: 12px;
    letter-spacing: 0.18em;
    font-weight: 700;
}

.feature-card h3,
.nav-item h3 {
    margin: 0 0 12px;
    color: #223248;
    font-size: 1.48rem;
}

.feature-card p,
.nav-item p {
    margin: 0;
    color: #6d7f93;
    line-height: 1.8;
}

.nav-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 18px;
}

.nav-item {
    border-radius: 22px;
    padding: 26px 22px;
    transition: transform 0.28s ease, box-shadow 0.28s ease, border-color 0.28s ease;
    cursor: pointer;
}

.nav-icon {
    width: 54px;
    height: 54px;
    border-radius: 18px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    margin-bottom: 18px;
    box-shadow: 0 12px 26px rgba(102, 126, 234, 0.24);
}

.notification-section {
}

.message-box {
    border-radius: 24px;
    padding: 22px;
    background: rgba(255, 255, 255, 0.92);
    border: 1px solid rgba(223, 230, 240, 0.9);
    box-shadow: 0 18px 50px rgba(31, 41, 55, 0.08);
}

.message-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    margin-bottom: 18px;
}

.message-head-left {
    display: flex;
    align-items: center;
    gap: 14px;
}

.message-head-icon {
    width: 56px;
    height: 56px;
    border-radius: 18px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
    box-shadow: 0 14px 30px rgba(102, 126, 234, 0.26);
}

.message-head-left h3 {
    margin: 0 0 6px;
    color: #223248;
    font-size: 1.2rem;
}

.message-head-left p {
    margin: 0;
    color: #6d7f93;
    line-height: 1.7;
}

.message-scroll {
    max-height: 560px;
    overflow-y: auto;
    padding: 0 12px;
    scrollbar-width: none;
    -ms-overflow-style: none;
}

.message-scroll::-webkit-scrollbar {
    width: 0;
    height: 0;
    display: none;
}

.message-item {
    position: relative;
    margin-bottom: 14px;
    padding: 18px 22px;
    border-radius: 18px;
    background: linear-gradient(180deg, #fbffff 0%, #fbffff 100%);
    border: 1px solid #d8eaf7;
    box-shadow: none;
}

.message-item:last-child {
    margin-bottom: 0;
}

.message-item-header {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    align-items: flex-start;
    margin-bottom: 12px;
}

.message-item-header h4 {
    margin: 0;
    font-size: 18px;
    color: #223248;
}

.message-item-header span {
    color: #96a5b7;
    white-space: nowrap;
    font-size: 13px;
}

.message-time-meta {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 7px 12px;
    border-radius: 999px;
    font-size: 13px;
    font-weight: 600;
    line-height: 1;
    border: 1px solid transparent;
}

.message-time-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 17px;
}

.message-time-meta.is-hot {
    color: #dc2626;
    background: linear-gradient(135deg, #fff1f2 0%, #ffe4e6 100%);
    border-color: #fecdd3;
}

.message-time-meta.is-notice {
    color: #c2410c;
    background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
    border-color: #fed7aa;
}

.message-time-meta.is-recent {
    color: #0369a1;
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    border-color: #bae6fd;
}

.message-time-meta.is-history {
    color: #6d28d9;
    background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
    border-color: #ddd6fe;
}

.message-time-meta.is-archive {
    color: #475569;
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-color: #cbd5e1;
}

.message-item-content {
    color: #53677e;
    line-height: 1.9;
    white-space: pre-wrap;
    word-break: break-word;
}

.message-empty,
.message-loading-more,
.message-end {
    text-align: center;
    color: #7a8da4;
    padding: 24px 0 8px;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 18px;
}

.stat-item {
    background: #ffffff;
    border: 1px solid #e9ecef;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border-radius: 24px;
    padding: 30px 20px;
    text-align: center;
    transition: transform 0.28s ease, box-shadow 0.28s ease, border-color 0.28s ease;
}

.stat-item:hover {
    transform: translateY(-8px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
    border-color: #667eea;
}

.stat-number {
    font-size: 3rem;
    font-weight: 700;
    margin-bottom: 8px;
    color: #667eea;
}

.stat-label {
    font-size: 1rem;
    color: #7f8c8d;
    font-weight: 500;
}

.popup-message-body {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 4px 2px 2px;
}

.popup-dialog-titlebar {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding-right: 32px;
}

.popup-dialog-title-main {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    gap: 14px;
}

.popup-dialog-title-group {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
}

.popup-dialog-badge {
    display: inline-flex;
    align-items: center;
    padding: 6px 12px;
    border-radius: 999px;
    background: linear-gradient(135deg, #eef2ff 0%, #f3e8ff 100%);
    color: #6d28d9;
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.12em;
}

.popup-dialog-progress {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 64px;
    height: 32px;
    padding: 0 14px;
    border-radius: 999px;
    background: #111827;
    color: #ffffff;
    font-size: 14px;
    font-weight: 700;
    box-shadow: 0 10px 24px rgba(17, 24, 39, 0.16);
}

.popup-dialog-title-text {
    margin: 0;
    color: #111827;
    font-size: 24px;
    font-weight: 700;
    line-height: 1.4;
}

.popup-message-meta {
    color: #94a3b8;
    font-size: 13px;
}

.popup-message-content {
    color: #334155;
    line-height: 1.9;
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 320px;
    overflow-y: auto;
    padding: 18px 20px;
    border-radius: 18px;
    background: linear-gradient(180deg, #f8fbff 0%, #fdfcff 100%);
    border: 1px solid #dbe7ff;
}

.popup-footer-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    width: 100%;
}

:deep(.home-message-dialog) {
    border-radius: 26px;
    overflow: hidden;
}

:deep(.home-message-dialog .el-dialog__header) {
    padding: 24px 24px 10px;
    background: linear-gradient(180deg, #ffffff 0%, #fbf8ff 100%);
}

:deep(.home-message-dialog .el-dialog__body) {
    padding: 8px 24px 18px;
}

:deep(.home-message-dialog .el-dialog__footer) {
    padding: 0 24px 24px;
}

:deep(.home-message-dialog .el-dialog__headerbtn) {
    top: 24px;
    right: 22px;
}

:deep(.home-message-dialog .el-dialog__headerbtn .el-dialog__close) {
    color: #64748b;
    font-size: 18px;
    font-weight: 700;
}

:deep(.home-message-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
    color: #111827;
}

@media (max-width: 1200px) {
    .banner-title {
        font-size: 2.5rem;
    }

    .banner-subtitle {
        font-size: 1.3rem;
    }

    .features-grid,
    .nav-grid,
    .stats-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .section-header h2 {
        font-size: 2.1rem;
    }
}

@media (max-width: 768px) {
    .banner {
        height: 400px;
    }

    .banner-title {
        font-size: 2rem;
    }

    .banner-subtitle {
        font-size: 1.1rem;
    }

    .home-main {
        padding: 28px 0 56px;
    }

    .content-section {
        padding: 0 1rem;
    }

    .features-grid,
    .nav-grid,
    .stats-grid {
        grid-template-columns: 1fr;
    }

    .section-header,
    .section-header-left {
        align-items: center;
        text-align: center;
    }

    .section-header h2 {
        font-size: 1.85rem;
    }

    .message-box,
    .feature-card,
    .nav-item,
    .stat-item {
        border-radius: 22px;
    }

    .feature-card {
        min-height: auto;
        flex-direction: column;
    }

    .message-head,
    .message-item-header,
    .popup-dialog-title-main {
        flex-direction: column;
        align-items: flex-start;
    }
}

@media (max-width: 480px) {
    .banner {
        height: 350px;
    }

    .banner-title {
        font-size: 1.8rem;
    }

    .banner-subtitle {
        font-size: 1rem;
    }

    .section-header h2 {
        font-size: 1.6rem;
    }

    .popup-dialog-title-text {
        font-size: 20px;
    }
}
</style>
