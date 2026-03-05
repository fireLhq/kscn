<template>
    <div class="admin-container">
        <!-- 权限检查中 -->
        <div v-if="checkingPermission" class="permission-checking">
            <el-loading :loading="true" text="权限检查中..."></el-loading>
        </div>
        
        <!-- 无权限提示 -->
        <div v-else-if="!hasPermission" class="no-permission">
            <el-empty description="访问被拒绝">
                <template #image>
                    <i class="el-icon-lock" style="font-size: 100px; color: #f56c6c"></i>
                </template>
                <p class="error-code">403 - Forbidden</p>
                <p class="error-message">您没有权限访问管理页面</p>
                <p class="error-message">只有管理员才能访问此页面</p>
                <el-button-group>
                    <el-button type="primary" icon="el-icon-back" @click="goBack">返回</el-button>
                    <el-button icon="el-icon-house" @click="goHome">首页</el-button>
                </el-button-group>
            </el-empty>
        </div>
        
        <!-- 管理页面内容 -->
        <div v-else class="admin-layout">
            <!-- 左侧菜单 -->
            <div class="admin-sidebar">
                <div class="sidebar-header">
                    <h3>后台管理</h3>
                </div>
                <div class="sidebar-menu">
                    <div 
                        class="menu-item" 
                        :class="{ active: currentMenu === 'user' }"
                        @click="switchMenu('user')"
                    >
                        <i class="el-icon-user"></i>
                        <span>用户管理</span>
                    </div>
                    <div 
                        class="menu-item" 
                        :class="{ active: currentMenu === 'projectMember' }"
                        @click="switchMenu('projectMember')"
                    >
                        <i class="el-icon-user-solid"></i>
                        <span>项目成员</span>
                    </div>
                    <div 
                        class="menu-item" 
                        :class="{ active: currentMenu === 'system' }"
                        @click="switchMenu('system')"
                    >
                        <i class="el-icon-setting"></i>
                        <span>系统管理</span>
                    </div>
                </div>
            </div>

            <!-- 右侧内容区 -->
            <div class="admin-content">
                <div class="content-header">
                    <h2>{{ getMenuTitle() }}</h2>
                </div>
                <div class="content-body">
                    <!-- 用户管理组件 -->
                    <UserManagement v-if="currentMenu === 'user'" />
                    
                    <!-- 项目成员管理组件 -->
                    <ProjectMemberManagement v-else-if="currentMenu === 'projectMember'" />
                    
                    <!-- 系统管理组件 -->
                    <div v-else-if="currentMenu === 'system'" class="coming-soon">
                        <i class="el-icon-setting"></i>
                        <p>系统管理功能开发中...</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import UserManagement from './components/UserManagement.vue';
import ProjectMemberManagement from './components/ProjectMemberManagement.vue';
import { getUserInfo } from "@/api/system/user";

export default {
    name: "AdminView",
    components: {
        UserManagement,
        ProjectMemberManagement
    },
    data() {
        return {
            currentMenu: 'user',
            checkingPermission: true,
            hasPermission: false,
            previousRoute: null
        };
    },
    async mounted() {
        await this.checkAdminPermission();
    },
    methods: {
        async checkAdminPermission() {
            try {
                const token = this.$store.state.token;
                if (!token) {
                    this.hasPermission = false;
                    this.checkingPermission = false;
                    this.$message.error('请先登录');
                    this.$router.push('/login');
                    return;
                }

                // 尝试获取用户信息来验证权限
                const response = await getUserInfo(token);
                if (response.data.code === 200) {
                    const userRole = response.data.data.role;
                    this.hasPermission = userRole === 0; // 0表示管理员
                    
                    if (!this.hasPermission) {
                        this.$message.warning('您没有权限访问管理页面');
                    }
                } else {
                    this.hasPermission = false;
                    this.$message.error('获取用户信息失败');
                }
            } catch (error) {
                console.error('权限检查失败:', error);
                
                // 检查是否是403错误
                if (error.response && error.response.status === 403) {
                    this.hasPermission = false;
                    this.$message.warning('您没有权限访问管理页面');
                } else {
                    this.hasPermission = false;
                    this.$message.error('权限检查失败，请重试');
                }
            } finally {
                this.checkingPermission = false;
            }
        },
        goBack() {
            this.$router.go(-1);
        },
        goHome() {
            this.$router.push('/');
        },
        switchMenu(menu) {
            this.currentMenu = menu;
        },
        getMenuTitle() {
            const titles = {
                'user': '用户管理',
                'projectMember': '项目成员管理',
                'system': '系统管理'
            };
            return titles[this.currentMenu] || '后台管理';
        }
    }
};
</script>

<style scoped>
.admin-container {
    min-height: 100vh;
    background-color: #f5f5f5;
}

/* 权限检查样式 */
.permission-checking {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-color: #f5f5f5;
}

.no-permission {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
}

.error-code {
    font-size: 24px;
    margin: 10px 0;
}

.error-message {
    color: #909399;
    margin-bottom: 20px;
}

.admin-layout {
    display: flex;
    min-height: 100vh;
}

.admin-sidebar {
    width: 250px;
    background: #2c3e50;
    color: white;
    box-shadow: 2px 0 5px rgba(0,0,0,0.1);
}

.sidebar-header {
    padding: 20px;
    border-bottom: 1px solid #34495e;
    text-align: center;
}

.sidebar-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
}

.sidebar-menu {
    padding: 10px 0;
}

.menu-item {
    display: flex;
    align-items: center;
    padding: 15px 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    border-left: 3px solid transparent;
}

.menu-item:hover {
    background-color: #34495e;
}

.menu-item.active {
    background-color: #3498db;
    border-left-color: #2980b9;
}

.menu-item i {
    margin-right: 10px;
    font-size: 16px;
}

.menu-item span {
    font-size: 14px;
}

.admin-content {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.content-header {
    background: white;
    padding: 20px 30px;
    border-bottom: 1px solid #e9ecef;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.content-header h2 {
    margin: 0;
    color: #2c3e50;
    font-size: 24px;
    font-weight: 600;
}

.content-body {
    flex: 1;
    padding: 30px;
    overflow-y: auto;
}

.coming-soon {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 400px;
    color: #7f8c8d;
}

.coming-soon i {
    font-size: 64px;
    margin-bottom: 20px;
    color: #bdc3c7;
}

.coming-soon p {
    font-size: 18px;
    margin: 0;
}

@media (max-width: 768px) {
    .admin-layout {
        flex-direction: column;
    }
    
    .admin-sidebar {
        width: 100%;
        height: auto;
    }
    
    .sidebar-menu {
        display: flex;
        overflow-x: auto;
    }
    
    .menu-item {
        white-space: nowrap;
        min-width: 120px;
    }
}
</style>
