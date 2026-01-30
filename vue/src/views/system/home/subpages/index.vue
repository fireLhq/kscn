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
                    <p>KSCN（Knowledge Sharing and Collaboration Network）是一个致力于知识共享与协作的开源平台。我们相信知识的力量，希望通过技术手段连接全球的学习者、研究者和知识工作者。</p>
                </div>
                
                <div class="about-section">
                    <h2>我们的使命</h2>
                    <p>构建一个开放、包容、高效的知识生态系统，让每个人都能便捷地获取、分享和创造知识，推动人类文明的进步。</p>
                </div>
                
                <div class="about-section">
                    <h2>核心价值</h2>
                    <div class="values-grid">
                        <div class="value-item">
                            <div class="value-icon">🌐</div>
                            <h3>开放共享</h3>
                            <p>打破知识壁垒，促进信息自由流动</p>
                        </div>
                        <div class="value-item">
                            <div class="value-icon">🤝</div>
                            <h3>协作创新</h3>
                            <p>汇聚集体智慧，激发创新灵感</p>
                        </div>
                        <div class="value-item">
                            <div class="value-icon">📚</div>
                            <h3>终身学习</h3>
                            <p>支持持续学习，适应时代变化</p>
                        </div>
                        <div class="value-item">
                            <div class="value-icon">💡</div>
                            <h3>技术驱动</h3>
                            <p>运用先进技术，提升用户体验</p>
                        </div>
                    </div>
                </div>
                
                <div class="about-section">
                    <h2>技术特色</h2>
                    <ul class="tech-features">
                        <li>现代化的响应式设计，支持多设备访问</li>
                        <li>基于Vue.js的前端框架，提供流畅的用户体验</li>
                        <li>模块化的系统架构，便于功能扩展和维护</li>
                        <li>安全可靠的身份认证和权限管理系统</li>
                        <li>高效的数据存储和检索机制</li>
                    </ul>
                </div>
                
                <div class="about-section">
                    <h2>联系我们</h2>
                    <div class="contact-info">
                        <p><strong>开发者QQ：</strong>3011499075</p>
                        <p><strong>项目地址：</strong><a href="https://github.com/your-repo" target="_blank">GitHub</a></p>
                        <p><strong>邮箱：</strong>contact@kscn.com</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: "AboutView",
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

.about-section { margin-bottom: 40px; }
.about-section:last-child { margin-bottom: 0; }
.about-section h2 { font-size: 28px; color: #212529; margin-bottom: 20px; font-weight: 600; border-bottom: 3px solid #6c757d; padding-bottom: 8px; }
.about-section p { font-size: 16px; line-height: 1.8; color: #495057; margin-bottom: 16px; }

.values-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 24px; margin-top: 24px; }
.value-item { text-align: center; padding: 24px; background: #f8f9fa; border-radius: 8px; border: 1px solid #e9ecef; transition: all 0.3s ease; }
.value-item:hover { transform: translateY(-4px); box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1); }
.value-icon { font-size: 48px; margin-bottom: 16px; }
.value-item h3 { font-size: 20px; color: #212529; margin-bottom: 12px; font-weight: 600; }
.value-item p { font-size: 14px; color: #6c757d; margin: 0; }

.tech-features { list-style: none; padding: 0; }
.tech-features li { font-size: 16px; color: #495057; margin-bottom: 12px; padding-left: 24px; position: relative; }
.tech-features li::before { content: "✓"; position: absolute; left: 0; color: #28a745; font-weight: bold; }

.contact-info { background: #f8f9fa; padding: 24px; border-radius: 8px; border: 1px solid #e9ecef; }
.contact-info p { margin-bottom: 12px; }
.contact-info a { color: #007bff; text-decoration: none; }
.contact-info a:hover { text-decoration: underline; }

@media (max-width: 768px) {
    .about-body { width: 95%; padding: 20px 0; }
    .about-header h1 { font-size: 36px; }
    .subtitle { font-size: 18px; }
    .about-content { padding: 24px; }
    .about-section h2 { font-size: 24px; }
    .values-grid { grid-template-columns: 1fr; gap: 16px; }
    .value-item { padding: 20px; }
}

@media (max-width: 480px) {
    .about-body { width: 98%; padding: 16px 0; }
    .about-header h1 { font-size: 28px; }
    .subtitle { font-size: 16px; }
    .about-content { padding: 16px; }
    .about-section h2 { font-size: 20px; }
    .about-section p { font-size: 14px; }
    .value-item { padding: 16px; }
    .value-icon { font-size: 36px; }
    .value-item h3 { font-size: 18px; }
    .value-item p { font-size: 13px; }
}
</style>
