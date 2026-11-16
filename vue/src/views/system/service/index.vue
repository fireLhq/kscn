<template>
    <div class="service-view">
        <div class="service-body">
            <div class="services-container">
                <section class="service-section" aria-labelledby="tools-heading">
                    <div class="section-header">
                        <span class="section-tag">EVERYDAY TOOLS</span>
                        <h2 id="tools-heading">服务功能</h2>
                    </div>
                    <div class="service-categories">
                        <div class="category-card" v-for="category in categories" :key="category.id">
                            <div class="category-heading">
                                <div class="category-icon">
                                    <i :class="category.icon" aria-hidden="true"></i>
                                </div>
                                <h3>{{ category.name }}</h3>
                            </div>
                            <p>{{ category.description }}</p>
                            <div class="category-services">
                                <div
                                    v-for="service in category.services"
                                    :key="service.id"
                                    class="service-item" :class="{ 'is-pending': service.status !== 'available' }" role="button" tabindex="0" @keydown.enter="openService(service)" @keydown.space.prevent="openService(service)"
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
                </section>

                <!-- 特色服务 -->
                <section class="service-section featured-services" aria-labelledby="featured-heading">
                    <div class="section-header">
                        <span class="section-tag">EXPLORE MORE</span>
                        <h2 id="featured-heading">特色服务</h2>
                    </div>
                    <div class="featured-grid">
                        <div
                            v-for="service in featuredServices"
                            :key="service.id"
                            class="featured-service" role="button" tabindex="0"
                            @keydown.enter="openService(service)"
                            @keydown.space.prevent="openService(service)"
                            @click="openService(service)"
                        >
                            <div class="service-banner">
                                <i :class="service.icon" aria-hidden="true"></i>
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
                </section>

                <!-- 服务统计 -->
                <section class="service-section service-stats" aria-labelledby="stats-heading">
                    <div class="section-header">
                        <span class="section-tag">AT A GLANCE</span>
                        <h2 id="stats-heading">服务统计</h2>
                    </div>
                    <div class="stats-grid">
                        <div class="stat-item">
                            <div class="stat-number">{{ stats.totalServices }}</div>
                            <div class="stat-label">总服务数</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-number">{{ stats.availableServices }}</div>
                            <div class="stat-label">可用服务</div>
                        </div>
                        <div class="stat-item"><div class="stat-number">本地</div><div class="stat-label">浏览器处理，无需登录</div></div>
                        <div class="stat-item"><div class="stat-number">手动</div><div class="stat-label">下载或保存，不自动留存</div></div>
                    </div>
                </section>

                <!-- 开发状态说明 -->
                <section class="service-section usage-guide" aria-labelledby="guide-heading">
                    <div class="section-header">
                        <span class="section-tag">GET STARTED</span>
                        <h2 id="guide-heading">使用说明</h2>
                    </div>
                    <div class="guide-content">
                        <div class="guide-step">
                            <div class="step-number">1</div>
                            <div class="step-content">
                                <h4>导入内容</h4>
                                <p>无需登录即可选择本地文件。登录后可从云盘或公共目录导入文件，文件完整导入后即可离线处理。</p>
                            </div>
                        </div>
                        <div class="guide-step">
                            <div class="step-number">2</div>
                            <div class="step-content">
                                <h4>浏览器本地处理</h4>
                                <p>可用工具在浏览器内运行，处理过程不向服务器上传内容。未完成的工具仍标为开发中。</p>
                            </div>
                        </div>
                        <div class="guide-step">
                            <div class="step-number">3</div>
                            <div class="step-content">
                                <h4>主动保存结果</h4>
                                <p>结果可直接下载，登录后也可选择服务器目录保存。断网时仍可下载；关闭前请保存，本系统不会自动保留工作内容。</p>
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </div>
        <LocalToolWorkbench ref="workbench" />
    </div>
</template>

<script>
import LocalToolWorkbench from '@/components/LocalToolWorkbench.vue';
export default {
    name: "ServiceView",
    components: { LocalToolWorkbench },
    async beforeRouteLeave(to, from, next) { next(await this.$refs.workbench.canLeave()); },
    data() {
        return {
            categories: [
                {
                    id: 1,
                    name: "文档处理",
                    icon: "el-icon-document",
                    description: "在线文档编辑、格式转换、PDF处理等",
                    services: [
                        { id: 1, name: "在线文档编辑器", icon: "el-icon-edit", status: "available" },
                        { id: 2, name: "PDF转换器", icon: "el-icon-refresh", status: "development" },
                        { id: 3, name: "格式转换工具", icon: "el-icon-switch-button", status: "available" }
                    ]
                },
                {
                    id: 2,
                    name: "图像处理",
                    icon: "el-icon-picture",
                    description: "图片编辑、压缩、格式转换等",
                    services: [
                        { id: 4, name: "在线图片编辑器", icon: "el-icon-edit-outline", status: "available" },
                        { id: 5, name: "图片压缩工具", icon: "el-icon-zoom-out", status: "available" },
                        { id: 6, name: "批量重命名", icon: "el-icon-collection-tag", status: "available" }
                    ]
                },
                {
                    id: 3,
                    name: "开发工具",
                    icon: "el-icon-cpu",
                    description: "代码格式化、API测试、在线编译等",
                    services: [
                        { id: 7, name: "代码格式化（JSON）", icon: "el-icon-s-operation", status: "available" },
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
                        { id: 10, name: "在线笔记", icon: "el-icon-notebook-1", status: "available" },
                        { id: 11, name: "知识图谱", icon: "el-icon-share", status: "development" },
                        { id: 12, name: "学习计划", icon: "el-icon-date", status: "available" }
                    ]
                }
            ],
            featuredServices: [
                {
                    id: "hash-deduplication",
                    name: "Hash 文件去重",
                    icon: "el-icon-files",
                    description: "对用户选择的文件计算 Hash，查找内容相同的重复文件，辅助整理个人文件。",
                    features: ["本地 Hash 计算", "重复文件分组", "规则保留与导出"],
                    status: "available",
                    route: "/service/hash-deduplication"
                },
                {
                    id: "algorithm-visualization",
                    name: "算法演示",
                    icon: "el-icon-data-analysis",
                    description: "通过动画展示常见算法的执行过程，直观理解数据变化与算法思路，让抽象知识更容易掌握。",
                    features: ["常见算法", "动画演示", "过程可视化"],
                    status: "available",
                    route: "/service/algorithms"
                },
                {
                    id: "formulas-and-functions",
                    name: "公式与函数",
                    icon: "el-icon-coordinate",
                    description: "通过点选数学符号构造公式，导出矢量图、位图或 LaTeX 代码，并将函数与方程绘制为图像。",
                    features: ["符号构建公式", "SVG / PNG 导出", "LaTeX 代码", "函数与方程绘图"],
                    status: "available",
                    route: "/service/formulas-and-functions"
                }
            ]
        };
    },
    computed: {
        stats() {
            const services = [...this.categories.flatMap(category => category.services), ...this.featuredServices];
            return { totalServices: services.length, availableServices: services.filter(service => service.status === 'available').length };
        }
    },
    methods: {
        openService(service) {
            if (service.route && service.status === 'available') this.$router.push(service.route);
            else if (service.status === 'available') this.$refs.workbench.open(service);
            else this.$message.info(`${service.name} 正在开发中，敬请期待！`);
        }
    }
};
</script>

<style scoped>
.service-view {
    --section-gap: 56px;
    --heading-gap: 28px;
    --card-gap: 22px;
    --card-radius: 24px;
    --text-primary: #223248;
    --text-secondary: #6b7d92;
    min-height: 100vh;
    background: #f8f9fa;
    color: var(--text-primary);
}

.service-body {
    max-width: 1400px;
    margin: 0 auto;
    padding: 40px 2rem 88px;
    box-sizing: border-box;
}

.service-section {
    margin: 0 0 var(--section-gap);
}

.service-section:last-child {
    margin-bottom: 0;
}

/* Every section uses the same title, rule and content spacing. */
.section-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin: 0 0 var(--heading-gap);
    text-align: center;
}

.section-tag {
    color: #667eea;
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.24em;
    line-height: 1.5;
}

.section-header h2 {
    margin: 12px 0 0;
    color: var(--text-primary);
    font-size: 2.45rem;
    font-weight: 700;
    line-height: 1.3;
    letter-spacing: 0.02em;
}

.section-header::after {
    content: '';
    width: 48px;
    height: 3px;
    margin-top: 16px;
    border-radius: 3px;
    background: linear-gradient(135deg, #667eea, #764ba2);
}

.service-categories,
.featured-grid,
.stats-grid,
.guide-content {
    display: grid;
    gap: var(--card-gap);
}

.service-categories,
.stats-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
}

.featured-grid,
.guide-content {
    grid-template-columns: repeat(3, minmax(0, 1fr));
}

.category-card,
.featured-service,
.stat-item,
.guide-step {
    min-width: 0;
    box-sizing: border-box;
    border: 1px solid #dfe6f0;
    border-radius: var(--card-radius);
    background: #fff;
    box-shadow: 0 18px 50px rgba(31, 41, 55, 0.06);
}

.category-card,
.featured-service {
    display: flex;
    flex-direction: column;
    padding: 26px;
    transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.category-card:hover,
.featured-service:hover {
    transform: translateY(-4px);
    border-color: #d2d8ff;
    box-shadow: 0 24px 54px rgba(31, 41, 55, 0.1);
}

.category-heading,
.service-banner {
    display: flex;
    align-items: center;
    gap: 14px;
}

.category-icon,
.service-banner > i {
    display: flex;
    flex: 0 0 52px;
    align-items: center;
    justify-content: center;
    width: 52px;
    height: 52px;
    border-radius: 17px;
    background: linear-gradient(135deg, #667eea, #764ba2);
    box-shadow: 0 10px 24px rgba(102, 126, 234, 0.2);
    color: #fff;
    font-size: 24px;
}

.category-heading h3,
.service-banner h3 {
    margin: 0;
    font-size: 1.35rem;
    font-weight: 700;
    line-height: 1.5;
    overflow-wrap: anywhere;
}

.category-card > p {
    min-height: 3.6em;
    margin: 18px 0 22px;
    color: var(--text-secondary);
    font-size: 14px;
    line-height: 1.8;
}

.category-services {
    display: grid;
    grid-template-rows: repeat(3, 1fr);
    gap: 10px;
    margin-top: auto;
}

.service-item {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
    min-height: 54px;
    padding: 10px 12px;
    border: 1px solid transparent;
    border-radius: 12px;
    background: #f5f7fc;
    box-sizing: border-box;
    cursor: pointer;
    transition: background 0.2s ease, border-color 0.2s ease;
}

.service-item:hover {
    border-color: #dce1ff;
    background: #eef1ff;
}

.service-item > i {
    flex-shrink: 0;
    color: #667eea;
    font-size: 17px;
}

.service-item > span:first-of-type {
    flex: 1;
    min-width: 0;
    color: #34465c;
    font-size: 14px;
    font-weight: 500;
    line-height: 1.6;
    overflow-wrap: anywhere;
}

.service-item .el-tag {
    flex-shrink: 0;
    margin: 0;
    padding: 0 6px;
    border-radius: 6px;
    font-size: 11px;
}

.service-item.is-pending > i,
.service-item.is-pending > span:first-of-type {
    color: #8794a6;
}

.service-item:focus-visible,
.featured-service:focus-visible {
    outline: 3px solid #667eea;
    outline-offset: 3px;
}

.featured-service {
    cursor: pointer;
}

.service-banner > i {
    flex-basis: 60px;
    width: 60px;
    height: 60px;
    border-radius: 19px;
    font-size: 28px;
}

.service-content {
    display: flex;
    flex: 1;
    flex-direction: column;
    align-items: flex-start;
    padding-top: 22px;
}

.service-content p {
    flex: 1;
    margin: 0 0 22px;
    color: var(--text-secondary);
    font-size: 15px;
    line-height: 1.8;
}

.service-features {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 24px;
}

.feature-tag {
    padding: 5px 10px;
    border-radius: 8px;
    background: #f0f2ff;
    color: #6576c6;
    font-size: 12px;
    line-height: 1.6;
}

.service-content .el-button {
    min-width: 104px;
    margin: 0;
    border-radius: 10px;
}

.service-content .el-button.is-disabled {
    border-color: #e5e9f4;
    background: #f1f3f9;
    color: #8490a6;
}

.stat-item {
    padding: 28px 20px;
    text-align: center;
}

.stat-number {
    margin-bottom: 10px;
    color: #667eea;
    font-size: 2.5rem;
    font-weight: 700;
    line-height: 1.2;
    font-variant-numeric: tabular-nums;
}

.stat-label {
    color: var(--text-secondary);
    font-size: 14px;
    line-height: 1.7;
}

.guide-step {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 26px;
}

.step-number {
    display: flex;
    flex: 0 0 40px;
    align-items: center;
    justify-content: center;
    height: 40px;
    border-radius: 13px;
    background: #eef0ff;
    color: #667eea;
    font-size: 18px;
    font-weight: 700;
}

.step-content {
    min-width: 0;
}

.step-content h4 {
    margin: 6px 0 12px;
    font-size: 1.1rem;
    line-height: 1.5;
}

.step-content p {
    margin: 0;
    color: var(--text-secondary);
    font-size: 14px;
    line-height: 1.8;
}

@media (max-width: 1200px) {
    .service-categories,
    .stats-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .section-header h2 {
        font-size: 2.1rem;
    }
}

@media (max-width: 992px) {
    .featured-grid,
    .guide-content {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 768px) {
    .service-view {
        --section-gap: 40px;
        --card-gap: 18px;
        --card-radius: 22px;
    }

    .service-body {
        padding: 28px 1rem 56px;
    }

    .section-header h2 {
        font-size: 1.85rem;
    }

    .service-categories {
        grid-template-columns: 1fr;
    }

    .category-card > p {
        min-height: 0;
    }

    .category-card,
    .featured-service,
    .guide-step {
        padding: 22px;
    }
}

@media (max-width: 480px) {
    .section-header h2 {
        font-size: 1.6rem;
    }

    .stat-item {
        padding: 24px 12px;
    }

    .stat-number {
        font-size: 2rem;
    }

    .guide-step {
        gap: 12px;
    }
}

@media (prefers-reduced-motion: reduce) {
    .category-card,
    .featured-service,
    .service-item {
        transition: none;
    }

    .category-card:hover,
    .featured-service:hover {
        transform: none;
    }
}
</style>
