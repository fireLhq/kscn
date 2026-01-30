<template>
    <div class="home-view">
        <div class="home-body">
            <!-- 横幅区域 -->
            <div class="banner">
                <img src="@/assets/images/home/横幅图片.jpg" alt="横幅图片" class="banner-img" />
                <div class="banner-overlay">
                    <h1 class="banner-title">KSCN 知识共享与协作网络</h1>
                    <p class="banner-subtitle">连接全球学习者，共享知识智慧</p>
                </div>
            </div>

            <!-- 核心功能区域 -->
            <div class="core-features">
                <div class="section-header">
                    <h2>核心功能</h2>
                    <p>为您提供全方位的知识管理解决方案</p>
                </div>
                <div class="features-grid">
                    <div class="feature-card" @click="goToResources">
                        <div class="feature-icon">
                            <i class="el-icon-folder-opened"></i>
                        </div>
                        <h3>公共资源库</h3>
                        <p>海量学习资源，涵盖各个领域，支持搜索、下载、分享</p>
                    </div>
                    <div class="feature-card" @click="goToCloudDrive">
                        <div class="feature-icon">
                            <i class="el-icon-cloudy"></i>
                        </div>
                        <h3>个人云盘</h3>
                        <p>专属文件存储空间，安全可靠，随时随地访问</p>
                    </div>
                    <div class="feature-card" @click="goToService">
                        <div class="feature-icon">
                            <i class="el-icon-service"></i>
                        </div>
                        <h3>在线服务</h3>
                        <p>多种在线工具和服务，提升学习和工作效率</p>
                    </div>
                </div>
            </div>

            <!-- 快捷导航区域 -->
            <div class="quick-nav">
                <div class="section-header">
                    <h2>了解更多</h2>
                    <p>探索KSCN的各个方面</p>
                </div>
                <div class="nav-grid">
                    <div class="nav-item" @click="goToAbout">
                        <i class="el-icon-info"></i>
                        <h3>关于我们</h3>
                        <p>了解KSCN的使命和愿景</p>
                    </div>
                    <div class="nav-item" @click="goToTeam">
                        <i class="el-icon-user"></i>
                        <h3>核心团队</h3>
                        <p>认识我们的开发团队</p>
                    </div>
                    <div class="nav-item" @click="goToSponsor">
                        <i class="el-icon-coin"></i>
                        <h3>赞助支持</h3>
                        <p>支持我们的发展</p>
                    </div>
                    <div class="nav-item" @click="goToNotice">
                        <i class="el-icon-warning-outline"></i>
                        <h3>使用须知</h3>
                        <p>平台使用规则和注意事项</p>
                    </div>
                </div>
            </div>

            <!-- 系统通知区域 -->
            <div class="notification">
                <div class="section-header">
                    <h2>系统通知</h2>
                    <p>重要信息及时掌握</p>
                </div>
                <div class="message-box">
                    <div class="message-content">
                        <i class="el-icon-info"></i>
                        <div class="message-text">
                            <p>目前网站大部分功能不能访问，正在研发中，敬请期待...</p>
                            <p>公共资源库已经开放，如想体验更多功能，请访问由Java Web开发的<a href="http://115.190.39.59:8080/KSCN/login.jsp" target="_blank">KSCN测试网站</a>。</p>
                            <p>如有意见或建议，请联系开发者，QQ：3011499075，请备注"KSCN"。</p>
                            <p>公共资源库使用注意事项：</p>
                            <ul>
                                <li>资源库中的资源为个人文件，请勿用于商业用途。</li>
                                <li>上传文件或文件夹时，新文件会覆盖旧文件，会造成文件丢失。</li>
                                <li>资源库任何人都可以上传和删除文件，请不要用于文件备份，以免造成文件丢失。</li>
                                <li>请用户自行遵守相关法律法规，不得上传违法违规内容。</li>
                                <li>请用户不要随意更改和删除别人上传的文件，共同维护资源库的秩序。</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 统计数据区域 -->
            <div class="stats-section">
                <div class="section-header">
                    <h2>平台数据</h2>
                    <p>见证我们的成长</p>
                </div>
                <div class="stats-grid">
                    <div class="stat-item">
                        <div class="stat-number">10+</div>
                        <div class="stat-label">注册用户</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-number">500+</div>
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
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: "HomeView",
    methods: {
        goToResources() { 
            // 检查是否需要登录
            const token = localStorage.getItem("jwt_token");
            if (!token) {
                this.$router.push('/login?redirect=' + encodeURIComponent('/resources'));
            } else {
                this.$router.push('/resources');
            }
        },
        goToCloudDrive() { 
            // 检查是否需要登录
            const token = localStorage.getItem("jwt_token");
            if (!token) {
                this.$router.push('/login?redirect=' + encodeURIComponent('/cloud-drive'));
            } else {
                this.$router.push('/cloud-drive');
            }
        },
        goToService() { this.$router.push('/service'); },
        goToAbout() { this.$router.push('/about'); },
        goToTeam() { this.$router.push('/team'); },
        goToSponsor() { this.$router.push('/sponsor'); },
        goToNotice() { this.$router.push('/notice'); },
    }
};
</script>

<style scoped>
@import "@/assets/styles/home.css";
</style>
