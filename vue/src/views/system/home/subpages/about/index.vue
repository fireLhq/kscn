<template>
    <div class="about-view">
        <div class="about-body">
            <div class="top-bar">
                <el-button type="text" @click="$router.back()"><i class="el-icon-arrow-left"></i> 返回</el-button>
            </div>
            
            <div class="about-header">
                <h1>关于我们</h1>
                <p class="subtitle">KSCN - 知识共享与协作网络</p>
            </div>
            
            <div class="about-content">
                <div class="about-section">
                    <h2>项目简介</h2>
                    <p>KSCN（Knowledge Sharing and Collaboration Network）是一个致力于知识共享与协作的开源项目。我们相信知识的力量，希望通过技术手段构建一个开放、包容、高效的知识共享平台。</p>
                    <p>项目成立于2024年，由一群热爱技术、追求创新的开发者共同发起。我们致力于为用户提供优质的知识管理工具，促进知识的传播与创新。</p>
                    <div class="open-source-notice">
                        <i class="el-icon-star-on"></i>
                        <div class="notice-content">
                            <h4>开源项目</h4>
                            <p>本项目完全开源，欢迎访问我们的 GitHub 仓库查看源码、提交问题或参与贡献。您可以自由使用、修改和分发本项目代码。</p>
                        </div>
                    </div>
                </div>
                
                <div class="about-section">
                    <h2>我们的使命</h2>
                    <div class="mission-grid">
                        <div class="mission-item">
                            <div class="mission-icon">
                                <i class="el-icon-share"></i>
                            </div>
                            <h3>知识共享</h3>
                            <p>打破信息壁垒，让知识自由流动，为每个人提供学习成长的机会。</p>
                        </div>
                        <div class="mission-item">
                            <div class="mission-icon">
                                <i class="el-icon-connection"></i>
                            </div>
                            <h3>协作创新</h3>
                            <p>通过团队协作，激发创新思维，共同解决复杂问题。</p>
                        </div>
                        <div class="mission-item">
                            <div class="mission-icon">
                                <i class="el-icon-open"></i>
                            </div>
                            <h3>开放包容</h3>
                            <p>保持开放的心态，欢迎各种观点和想法的碰撞与融合。</p>
                        </div>
                    </div>
                </div>
                
                <div class="about-section">
                    <h2>核心价值</h2>
                    <div class="values-list">
                        <div class="value-item">
                            <h4>开放透明</h4>
                            <p>所有代码和文档都开源公开，接受社区的监督和建议。</p>
                        </div>
                        <div class="value-item">
                            <h4>用户至上</h4>
                            <p>始终以用户需求为中心，不断优化产品体验。</p>
                        </div>
                        <div class="value-item">
                            <h4>持续创新</h4>
                            <p>紧跟技术发展趋势，持续改进和创新产品功能。</p>
                        </div>
                        <div class="value-item">
                            <h4>社区驱动</h4>
                            <p>重视社区反馈，让用户参与产品设计和开发过程。</p>
                        </div>
                    </div>
                </div>
                
                <div class="about-section">
                    <h2>技术特色</h2>
                    <div class="tech-features">
                        <div class="tech-feature">
                            <h4>现代化架构</h4>
                            <p>采用Vue.js、Element UI等现代前端技术栈，确保良好的用户体验和开发效率。</p>
                        </div>
                        <div class="tech-feature">
                            <h4>响应式设计</h4>
                            <p>支持多种设备访问，无论是桌面端还是移动端都能提供一致的使用体验。</p>
                        </div>
                        <div class="tech-feature">
                            <h4>安全可靠</h4>
                            <p>采用JWT认证、HTTPS传输等安全措施，保护用户数据和隐私。</p>
                        </div>
                        <div class="tech-feature">
                            <h4>高性能</h4>
                            <p>优化数据库查询、缓存策略等，确保系统在高并发下的稳定运行。</p>
                        </div>
                    </div>
                </div>
                
                <div class="about-section">
                    <h2>联系我们</h2>
                    <div class="contact-info">
                        <div class="contact-item">
                            <i class="el-icon-message"></i>
                            <span>邮箱：lab_c919@qq.com</span>
                        </div>
                        <div class="contact-item">
                            <i class="el-icon-phone"></i>
                            <span>电话：191-2268-5314</span>
                        </div>
                        <div class="contact-item">
                            <i class="el-icon-location"></i>
                            <span>地址：重庆市渝中区较场口</span>
                        </div>
                        <div class="contact-item">
                            <i class="el-icon-chat-dot-round"></i>
                            <span>Telegram群：<a href="https://t.me/kscn_top" target="_blank">KSCN</a></span>
                        </div>
                    </div>
                    
                    <div class="github-section">
                        <div class="github-card">
                            <div class="github-icon">
                                <i class="el-icon-link"></i>
                            </div>
                            <div class="github-content">
                                <h3>开源仓库</h3>
                                <p>欢迎访问我们的 GitHub 仓库，查看完整源码、提交问题反馈或参与项目开发</p>
                                <el-button type="primary" icon="el-icon-link" @click="openGithub">
                                    访问 GitHub 仓库
                                </el-button>
                            </div>
                        </div>
                        <div class="github-tips">
                            <h4><i class="el-icon-info"></i> 如何参与贡献？</h4>
                            <ul>
                                <li>⭐ Star 项目以表示支持</li>
                                <li>🐛 提交 Issue 报告问题或建议</li>
                                <li>🔧 Fork 项目并提交 Pull Request</li>
                                <li>📖 完善项目文档和示例</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: "AboutView",
    data() {
        return {
            // 页面数据
        };
    },
    methods: {
        openGithub() {
            // 替换为实际的 GitHub 仓库地址
            window.open('https://github.com/fireLhq/kscn', '_blank');
        }
    }
};
</script>

<style scoped>
.about-view {
    width: 100%;
    min-height: 100vh;
    background-color: #f8f9fa;
    display: flex;
    flex-direction: column;
}

.about-body {
    flex: 1;
    max-width: 1200px;
    width: 90%;
    margin: 0 auto;
    padding: 40px 0;
}

.top-bar {
    margin-bottom: 16px;
}

.about-header {
    text-align: center;
    margin-bottom: 48px;
}

.about-header h1 {
    font-size: 48px;
    color: #212529;
    margin-bottom: 16px;
    font-weight: 700;
}

.subtitle {
    font-size: 20px;
    color: #6c757d;
    font-weight: 400;
}

.about-content {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    padding: 40px;
}

.about-section {
    margin-bottom: 48px;
}

.about-section:last-child {
    margin-bottom: 0;
}

.about-section h2 {
    font-size: 32px;
    color: #212529;
    margin-bottom: 24px;
    font-weight: 600;
    border-bottom: 3px solid #6c757d;
    padding-bottom: 12px;
}

.about-section p {
    font-size: 16px;
    line-height: 1.8;
    color: #495057;
    margin-bottom: 16px;
}

.mission-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 24px;
    margin-top: 24px;
}

.mission-item {
    text-align: center;
    padding: 24px;
    background: #f8f9fa;
    border-radius: 12px;
    border: 1px solid #e9ecef;
    transition: all 0.3s ease;
}

.mission-item:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.mission-icon {
    font-size: 48px;
    color: #007bff;
    margin-bottom: 16px;
}

.mission-item h3 {
    font-size: 20px;
    color: #212529;
    margin-bottom: 12px;
    font-weight: 600;
}

.mission-item p {
    font-size: 14px;
    color: #6c757d;
    line-height: 1.6;
}

.values-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-top: 24px;
}

.value-item {
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    border-left: 4px solid #007bff;
}

.value-item h4 {
    font-size: 18px;
    color: #212529;
    margin-bottom: 8px;
    font-weight: 600;
}

.value-item p {
    font-size: 14px;
    color: #6c757d;
    line-height: 1.6;
    margin-bottom: 0;
}

.tech-features {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-top: 24px;
}

.tech-feature {
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    border: 1px solid #e9ecef;
}

.tech-feature h4 {
    font-size: 18px;
    color: #212529;
    margin-bottom: 8px;
    font-weight: 600;
}

.tech-feature p {
    font-size: 14px;
    color: #6c757d;
    line-height: 1.6;
    margin-bottom: 0;
}

.contact-info {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-top: 24px;
}

.contact-item {
    display: flex;
    align-items: center;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 8px;
    border: 1px solid #e9ecef;
}

.contact-item i {
    font-size: 20px;
    color: #007bff;
    margin-right: 12px;
}

.contact-item span {
    font-size: 16px;
    color: #495057;
}

.contact-item a {
    color: #0088cc;
    text-decoration: none;
    font-weight: 500;
    padding: 2px 8px;
    border-radius: 4px;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
}

.contact-item a:hover {
    color: #006699;
    background: rgba(0, 136, 204, 0.1);
}

.contact-item a::before {
    content: '✈️';
    margin-right: 4px;
    font-size: 14px;
}

.open-source-notice {
    display: flex;
    align-items: flex-start;
    margin-top: 24px;
    padding: 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    color: white;
}

.open-source-notice i {
    font-size: 32px;
    margin-right: 16px;
    flex-shrink: 0;
}

.notice-content h4 {
    font-size: 20px;
    margin: 0 0 8px 0;
    font-weight: 600;
}

.notice-content p {
    font-size: 14px;
    line-height: 1.6;
    margin: 0;
    color: rgba(255, 255, 255, 0.95);
}

.github-section {
    margin-top: 32px;
    padding-top: 32px;
    border-top: 2px dashed #e9ecef;
}

.github-card {
    display: flex;
    align-items: center;
    padding: 24px;
    background: linear-gradient(135deg, #24292e 0%, #1a1e22 100%);
    border-radius: 12px;
    color: white;
    margin-bottom: 24px;
}

.github-icon {
    font-size: 48px;
    margin-right: 24px;
    flex-shrink: 0;
}

.github-content {
    flex: 1;
}

.github-content h3 {
    font-size: 24px;
    margin: 0 0 8px 0;
    font-weight: 600;
}

.github-content p {
    font-size: 14px;
    line-height: 1.6;
    margin: 0 0 16px 0;
    color: rgba(255, 255, 255, 0.85);
}

.github-content .el-button {
    background: #28a745;
    border-color: #28a745;
}

.github-content .el-button:hover {
    background: #218838;
    border-color: #1e7e34;
}

.github-tips {
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    border-left: 4px solid #28a745;
}

.github-tips h4 {
    font-size: 18px;
    color: #212529;
    margin: 0 0 12px 0;
    font-weight: 600;
}

.github-tips h4 i {
    color: #28a745;
    margin-right: 8px;
}

.github-tips ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.github-tips li {
    font-size: 14px;
    color: #495057;
    line-height: 2;
    padding-left: 8px;
}

@media (max-width: 768px) {
    .about-body {
        width: 95%;
        padding: 20px 0;
    }
    
    .about-content {
        padding: 24px;
    }
    
    .about-header h1 {
        font-size: 36px;
    }
    
    .subtitle {
        font-size: 18px;
    }
    
    .mission-grid,
    .values-list,
    .tech-features,
    .contact-info {
        grid-template-columns: 1fr;
    }
    
    .github-card {
        flex-direction: column;
        text-align: center;
    }
    
    .github-icon {
        margin-right: 0;
        margin-bottom: 16px;
    }
    
    .open-source-notice {
        flex-direction: column;
        text-align: center;
    }
    
    .open-source-notice i {
        margin-right: 0;
        margin-bottom: 12px;
    }
}
</style>
