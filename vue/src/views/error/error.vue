<template>
    <div class="error-container">
        <el-empty :description="errorTitle">
            <template #image>
                <!-- 动态显示不同图标 -->
                <i :class="errorIcon" :style="{ fontSize: '100px', color: iconColor }"></i>
            </template>
            <p class="error-code">{{ errorCode }} - {{ errorMessage }}</p>
            <el-button-group>
                <el-button type="primary" icon="el-icon-refresh-left" @click="$router.go(-1)"> 返回上一页 </el-button>
                <el-button icon="el-icon-house" @click="$router.push('/')"> 返回首页 </el-button>
            </el-button-group>
        </el-empty>
    </div>
</template>

<script>
export default {
    name: "ErrorPage",
    props: {
        // 通过路由参数传递错误信息
        error: {
            type: Object,
            default: () => ({
                code: 500,
                message: "服务器开小差了",
            }),
        },
    },
    computed: {
        errorCode() {
            return this.error.code || 500;
        },
        errorMessage() {
            return this.error.message || "未知错误";
        },
        errorTitle() {
            return this.errorCode === 403 ? "访问被拒绝" : "系统异常";
        },
        errorIcon() {
            return (
                {
                    403: "el-icon-lock",
                    500: "el-icon-warning-outline",
                    404: "el-icon-question",
                }[this.errorCode] || "el-icon-warning-outline"
            );
        },
        iconColor() {
            return (
                {
                    403: "#F56C6C",
                    500: "#E6A23C",
                    404: "#909399",
                }[this.errorCode] || "#E6A23C"
            );
        },
    },
};
</script>

<style scoped>
/* 复用404页面的样式 */
.error-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
}
.error-code {
    font-size: 24px;
    margin: 10px 0;
}
</style>
