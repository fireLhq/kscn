const {defineConfig} = require("@vue/cli-service");

module.exports = defineConfig({
    transpileDependencies: true,
    devServer: {
        proxy: {
            "/api": {
                target: "http://localhost:8848/kscn/api",
                changeOrigin: true,
                pathRewrite: {
                    "^/api": "",
                },
            },
        },
        historyApiFallback: true,
    },
});
