<template>
    <div class="error-container">
        <!-- 使用 ElementUI 的图标和按钮 -->
        <el-empty description="页面走丢了">
            <template #image>
                <i class="el-icon-warning-outline" style="font-size: 100px; color: #e6a23c"></i>
            </template>
            <p class="error-code">404 - Not Found</p>
            <p class="error-message">您访问的页面不存在</p>
            <el-button type="primary" icon="el-icon-house" @click="$router.push('/')"> 返回首页 </el-button>
        </el-empty>
    </div>
</template>

<script>
export default {
    name: "NotFound",
};
</script>

<style scoped>
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
.error-message {
    color: #909399;
    margin-bottom: 20px;
}
</style>
