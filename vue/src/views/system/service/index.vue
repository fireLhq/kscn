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
