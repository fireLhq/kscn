<template>
    <div class="service-view">
        <div class="service-body">
            <div class="services-container">
                <!-- 服务功能标题 -->
                <div class="service-features-title">
                    <h2>服务功能</h2>
                </div>
                <!-- 服务分类 -->
                <div class="service-categories">
                    <div class="category-card" v-for="category in categories" :key="category.id">
                        <div class="category-icon">
                            <i :class="category.icon"></i>
                        </div>
                        <h3>{{ category.name }}</h3>
                        <p>{{ category.description }}</p>
                        <div class="category-services">
                            <div 
                                v-for="service in category.services" 
                                :key="service.id"
                                class="service-item"
                                @click="openService(service)"
                            >
                                <i :class="service.icon"></i>
                                <span>{{ service.name }}</span>
                                <el-tag size="mini" :type="service.status === 'available' ? 'success' : 'info'">
                                    {{ service.status === 'available' ? '可用' : '开发中' }}
                                </el-tag>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 特色服务 -->
                <div class="featured-services">
                    <h2>特色服务</h2>
                    <div class="featured-grid">
                        <div 
                            v-for="service in featuredServices" 
                            :key="service.id"
                            class="featured-service"
                            @click="openService(service)"
                        >
                            <div class="service-banner">
                                <i :class="service.icon"></i>
                                <h3>{{ service.name }}</h3>
                            </div>
                            <div class="service-content">
                                <p>{{ service.description }}</p>
                                <div class="service-features">
                                    <span v-for="feature in service.features" :key="feature" class="feature-tag">
                                        {{ feature }}
                                    </span>
                                </div>
                                <el-button type="primary" size="small" :disabled="service.status !== 'available'">
                                    {{ service.status === 'available' ? '立即使用' : '敬请期待' }}
                                </el-button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 服务统计 -->
                <div class="service-stats">
                    <h2>服务统计</h2>
                    <div class="stats-grid">
                        <div class="stat-item">
                            <div class="stat-number">{{ stats.totalServices }}</div>
                            <div class="stat-label">总服务数</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-number">{{ stats.availableServices }}</div>
                            <div class="stat-label">可用服务</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-number">{{ stats.totalUsers }}</div>
                            <div class="stat-label">服务用户</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-number">{{ stats.uptime }}%</div>
                            <div class="stat-label">服务可用性</div>
                        </div>
                    </div>
                </div>

                <!-- 开发状态说明 -->
                <div class="usage-guide">
                    <h2>开发状态说明</h2>
                    <div class="guide-content">
                        <div class="guide-step">
                            <div class="step-number">1</div>
                            <div class="step-content">
                                <h4>功能开发中</h4>
                                <p>目前所有服务功能都处于开发阶段，暂时无法使用</p>
                            </div>
                        </div>
                        <div class="guide-step">
                            <div class="step-number">2</div>
                            <div class="step-content">
                                <h4>敬请期待</h4>
                                <p>我们正在努力开发各种实用的在线工具和服务</p>
                            </div>
                        </div>
                        <div class="guide-step">
                            <div class="step-number">3</div>
                            <div class="step-content">
                                <h4>持续更新</h4>
                                <p>请关注我们的更新动态，功能完成后将第一时间通知用户</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: "ServiceView",
    data() {
        return {
            categories: [
                {
                    id: 1,
                    name: "文档处理",
                    icon: "el-icon-document",
                    description: "在线文档编辑、格式转换、PDF处理等",
                    services: [
                        { id: 1, name: "在线文档编辑器", icon: "el-icon-edit", status: "development" },
                        { id: 2, name: "PDF转换器", icon: "el-icon-refresh", status: "development" },
                        { id: 3, name: "格式转换工具", icon: "el-icon-switch-button", status: "development" }
                    ]
                },
                {
                    id: 2,
                    name: "图像处理",
                    icon: "el-icon-picture",
                    description: "图片编辑、压缩、格式转换等",
                    services: [
                        { id: 4, name: "在线图片编辑器", icon: "el-icon-edit-outline", status: "development" },
                        { id: 5, name: "图片压缩工具", icon: "el-icon-zoom-out", status: "development" },
                        { id: 6, name: "批量重命名", icon: "el-icon-collection-tag", status: "development" }
                    ]
                },
                {
                    id: 3,
                    name: "开发工具",
                    icon: "el-icon-cpu",
                    description: "代码格式化、API测试、在线编译等",
                    services: [
                        { id: 7, name: "代码格式化", icon: "el-icon-s-operation", status: "development" },
                        { id: 8, name: "API测试工具", icon: "el-icon-connection", status: "development" },
                        { id: 9, name: "在线编译器", icon: "el-icon-monitor", status: "development" }
                    ]
                },
                {
                    id: 4,
                    name: "学习工具",
                    icon: "el-icon-reading",
                    description: "在线学习、知识管理、笔记工具等",
                    services: [
                        { id: 10, name: "在线笔记", icon: "el-icon-notebook-1", status: "development" },
                        { id: 11, name: "知识图谱", icon: "el-icon-share", status: "development" },
                        { id: 12, name: "学习计划", icon: "el-icon-date", status: "development" }
                    ]
                }
            ],
            featuredServices: [
                {
                    id: 1,
                    name: "在线文档编辑器",
                    icon: "el-icon-edit",
                    description: "功能强大的在线文档编辑器，支持多人协作编辑，实时同步，多种格式导出",
                    features: ["多人协作", "实时同步", "多种格式", "云端存储"],
                    status: "development"
                },
                {
                    id: 2,
                    name: "PDF转换器",
                    icon: "el-icon-refresh",
                    description: "支持PDF与其他格式之间的相互转换，操作简单，转换质量高",
                    features: ["多格式支持", "批量转换", "高质量", "快速处理"],
                    status: "development"
                },
                {
                    id: 3,
                    name: "图片压缩工具",
                    icon: "el-icon-zoom-out",
                    description: "智能图片压缩工具，在保持图片质量的同时大幅减小文件大小",
                    features: ["智能压缩", "质量保持", "批量处理", "多格式支持"],
                    status: "development"
                }
            ],
            stats: {
                totalServices: 12,
                availableServices: 0,
                totalUsers: 0,
                uptime: 0
            }
        };
    },
    methods: {
        openService(service) {
            this.$message.info(`${service.name} 正在开发中，敬请期待！`);
        }
    }
};
</script>

<style scoped>
/* 服务页面样式 */
.service-view {
    min-height: 100vh;
    background-color: #f8f9fa;
}

.service-body {
    padding: 2rem 0;
    max-width: 1400px;
    margin: 0 auto;
}

/* 页面头部 */
.page-header {
    text-align: center;
    margin-bottom: 3rem;
    padding: 0 2rem;
}

.page-header h1 {
    font-size: 2.5rem;
    font-weight: 700;
    color: #2c3e50;
    margin-bottom: 1rem;
    position: relative;
}

.page-header h1::after {
    content: '';
    position: absolute;
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 2px;
}

.page-header p {
    font-size: 1.2rem;
    color: #7f8c8d;
    max-width: 600px;
    margin: 0 auto;
    line-height: 1.6;
}

/* 服务容器 */
.services-container {
    padding: 0 2rem;
}

/* 服务分类 */
.service-categories {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 2rem;
    margin-bottom: 4rem;
}

.category-card {
    background: #fff;
    border-radius: 15px;
    padding: 2rem;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border: 1px solid #e9ecef;
    transition: all 0.3s ease;
}

.category-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
    border-color: #667eea;
}

.category-icon {
    width: 60px;
    height: 60px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 1.5rem;
    margin-bottom: 1.5rem;
}

.category-card h3 {
    font-size: 1.4rem;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 1rem;
}

.category-card p {
    color: #7f8c8d;
    line-height: 1.6;
    margin-bottom: 1.5rem;
}

.category-services {
    display: flex;
    flex-direction: column;
    gap: 0.8rem;
}

.service-item {
    display: flex;
    align-items: center;
    gap: 0.8rem;
    padding: 0.8rem;
    border-radius: 8px;
    background: #f8f9fa;
    cursor: pointer;
    transition: all 0.2s ease;
}

.service-item:hover {
    background: #e9ecef;
    transform: translateX(5px);
}

.service-item i {
    color: #667eea;
    font-size: 1.1rem;
}

.service-item span {
    flex: 1;
    color: #2c3e50;
    font-weight: 500;
}

/* 特色服务 */
.featured-services {
    margin-bottom: 4rem;
}

.featured-services h2 {
    font-size: 2rem;
    font-weight: 700;
    color: #2c3e50;
    text-align: center;
    margin-bottom: 2rem;
    position: relative;
}

.featured-services h2::after {
    content: '';
    position: absolute;
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 2px;
}

.featured-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
    gap: 2rem;
}

.featured-service {
    background: #fff;
    border-radius: 15px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border: 1px solid #e9ecef;
    transition: all 0.3s ease;
    cursor: pointer;
}

.featured-service:hover {
    transform: translateY(-5px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
    border-color: #667eea;
}

.service-banner {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    padding: 2rem;
    text-align: center;
}

.service-banner i {
    font-size: 3rem;
    margin-bottom: 1rem;
}

.service-banner h3 {
    font-size: 1.5rem;
    font-weight: 600;
    margin: 0;
}

.service-content {
    padding: 2rem;
}

.service-content p {
    color: #7f8c8d;
    line-height: 1.6;
    margin-bottom: 1.5rem;
}

.service-features {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1.5rem;
}

.feature-tag {
    background: #f0f2ff;
    color: #667eea;
    padding: 0.3rem 0.8rem;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 500;
}

/* 服务统计 */
.service-stats {
    margin-bottom: 4rem;
}

.service-stats h2 {
    font-size: 2rem;
    font-weight: 700;
    color: #2c3e50;
    text-align: center;
    margin-bottom: 2rem;
    position: relative;
}

.service-stats h2::after {
    content: '';
    position: absolute;
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 2px;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 2rem;
}

.stat-item {
    background: #fff;
    padding: 2rem;
    border-radius: 15px;
    text-align: center;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border: 1px solid #e9ecef;
    transition: all 0.3s ease;
}

.stat-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
    border-color: #667eea;
}

.stat-number {
    font-size: 2.5rem;
    font-weight: 700;
    color: #667eea;
    margin-bottom: 0.5rem;
}

.stat-label {
    color: #7f8c8d;
    font-size: 1rem;
    font-weight: 500;
}

/* 使用说明 */
.usage-guide {
    margin-bottom: 2rem;
}

.usage-guide h2 {
    font-size: 2rem;
    font-weight: 700;
    color: #2c3e50;
    text-align: center;
    margin-bottom: 2rem;
    position: relative;
}

.usage-guide h2::after {
    content: '';
    position: absolute;
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 2px;
}

.guide-content {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 2rem;
}

.guide-step {
    background: #fff;
    padding: 2rem;
    border-radius: 15px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border: 1px solid #e9ecef;
    display: flex;
    align-items: flex-start;
    gap: 1.5rem;
    transition: all 0.3s ease;
}

.guide-step:hover {
    transform: translateY(-3px);
    box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
    border-color: #667eea;
}

.step-number {
    width: 50px;
    height: 50px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
    font-weight: 700;
    flex-shrink: 0;
}

.step-content h4 {
    font-size: 1.2rem;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 0.8rem;
}

.step-content p {
    color: #7f8c8d;
    line-height: 1.6;
    margin: 0;
}

/* 服务功能标题 */
.service-features-title { text-align: center; margin: 0 0 1.5rem 0; }
.service-features-title h2 { font-size: 2rem; font-weight: 700; color: #2c3e50; position: relative; display: inline-block; }
.service-features-title h2::after { content: ''; position: absolute; bottom: -8px; left: 50%; transform: translateX(-50%); width: 60px; height: 3px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 2px; }

/* 响应式设计 */
@media (max-width: 1200px) {
    .featured-grid {
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    }

    .guide-content {
        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    }
}

@media (max-width: 992px) {
    .service-body {
        padding: 1.5rem 0;
    }

    .page-header h1 {
        font-size: 2rem;
    }

    .page-header p {
        font-size: 1rem;
    }

    .services-container {
        padding: 0 1rem;
    }

    .service-categories {
        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
        gap: 1.5rem;
    }

    .featured-grid {
        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
        gap: 1.5rem;
    }

    .stats-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 1.5rem;
    }

    .guide-content {
        grid-template-columns: 1fr;
        gap: 1.5rem;
    }
}

@media (max-width: 768px) {
    .service-body {
        padding: 1rem 0;
    }

    .page-header {
        margin-bottom: 2rem;
        padding: 0 1rem;
    }

    .page-header h1 {
        font-size: 1.8rem;
    }

    .services-container {
        padding: 0 0.5rem;
    }

    .service-categories {
        grid-template-columns: 1fr;
        gap: 1.5rem;
        margin-bottom: 3rem;
    }

    .category-card {
        padding: 1.5rem;
    }

    .featured-services {
        margin-bottom: 3rem;
    }

    .featured-services h2 {
        font-size: 1.6rem;
    }

    .featured-grid {
        grid-template-columns: 1fr;
        gap: 1.5rem;
    }

    .service-banner {
        padding: 1.5rem;
    }

    .service-banner i {
        font-size: 2.5rem;
    }

    .service-content {
        padding: 1.5rem;
    }

    .service-stats {
        margin-bottom: 3rem;
    }

    .service-stats h2 {
        font-size: 1.6rem;
    }

    .stats-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
    }

    .stat-item {
        padding: 1.5rem;
    }

    .usage-guide h2 {
        font-size: 1.6rem;
    }

    .guide-step {
        padding: 1.5rem;
        gap: 1rem;
    }

    .step-number {
        width: 40px;
        height: 40px;
        font-size: 1.2rem;
    }
}

@media (max-width: 480px) {
    .page-header h1 {
        font-size: 1.6rem;
    }

    .page-header p {
        font-size: 0.9rem;
    }

    .category-card {
        padding: 1rem;
    }

    .category-icon {
        width: 50px;
        height: 50px;
        font-size: 1.2rem;
    }

    .service-banner {
        padding: 1rem;
    }

    .service-banner i {
        font-size: 2rem;
    }

    .service-banner h3 {
        font-size: 1.3rem;
    }

    .service-content {
        padding: 1rem;
    }

    .stat-item {
        padding: 1rem;
    }

    .stat-number {
        font-size: 2rem;
    }

    .guide-step {
        padding: 1rem;
        flex-direction: column;
        text-align: center;
    }

    .step-number {
        align-self: center;
    }
}
</style>
