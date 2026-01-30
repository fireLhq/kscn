const { defineConfig } = require("@vue/cli-service");

module.exports = defineConfig({
    transpileDependencies: true,
    devServer: {
        proxy: {
            "/api": {
                target: "http://localhost:8848/kscn/api", // 注意这里，和 nginx 保持一致
                changeOrigin: true,
                pathRewrite: {
                    "^/api": "", // 去掉前缀，使请求变成 /auth/login
                },
            },
        },
        historyApiFallback: true, // 解决浏览器刷新404问题
    },
});
