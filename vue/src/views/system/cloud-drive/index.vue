<template>
    <div class="encyclopedia-view">
        <div class="encyclopedia-body">
            <div class="file-explorer">
                <!-- 顶部工具栏 -->
                <div class="toolbar">
                    <div class="search-box">
                        <el-input
                            v-model="searchQuery"
                            placeholder="搜索您的文件或文件夹..."
                            prefix-icon="el-icon-search"
                            clearable
                            @input="handleSearch"
                        />
                    </div>
                    <div class="toolbar-actions">
                        <el-button type="primary" @click="showUploadDialog = true">
                            <i class="el-icon-upload"></i> 上传文件
                        </el-button>
                        <el-button @click="createFolder">
                            <i class="el-icon-folder-add"></i> 新建文件夹
                        </el-button>
                        <el-button @click="goBack" :disabled="!canGoBack">
                            <i class="el-icon-back"></i> 返回上级
                        </el-button>
                        <el-button @click="goToRoot">
                            <i class="el-icon-house"></i> 返回根目录
                        </el-button>
                    </div>
                </div>

                <!-- 存储空间信息 -->
                <div class="storage-info">
                    <div class="storage-bar">
                        <div class="storage-text">
                            <span>已使用: {{ formatFileSize(usedStorage) }}</span>
                            <span>总空间: {{ formatFileSize(totalStorage) }}</span>
                        </div>
                        <div class="storage-progress">
                            <el-progress 
                                :percentage="storagePercentage" 
                                :color="storageColor"
                                :stroke-width="8"
                            ></el-progress>
                        </div>
                    </div>
                </div>

                <!-- 面包屑导航 -->
                <div class="breadcrumb">
                    <el-breadcrumb separator="/">
                        <el-breadcrumb-item 
                            v-for="(item, index) in breadcrumbItems" 
                            :key="index"
                            @click.native="navigateToBreadcrumb(index)"
                            :class="{ 'clickable': index < breadcrumbItems.length - 1 }"
                        >
                            {{ item.name }}
                        </el-breadcrumb-item>
                    </el-breadcrumb>
                </div>

                <div class="explorer-content">
                    <!-- 左侧路径树 -->
                    <div class="path-tree">
                        <h3>我的目录</h3>
                        <el-tree
                            :data="treeData"
                            :props="treeProps"
                            @node-click="handleTreeNodeClick"
                            :default-expanded-keys="['root']"
                            node-key="id"
                        >
                            <span class="custom-tree-node" slot-scope="{ node, data }">
                                <i :class="data.type === 'folder' ? 'el-icon-folder' : 'el-icon-document'"></i>
                                <span>{{ node.label }}</span>
                            </span>
                        </el-tree>
                    </div>

                    <!-- 右侧文件列表 -->
                    <div class="file-list">
                        <div class="list-header">
                            <h3>我的文件</h3>
                            <div class="view-options">
                                <el-radio-group v-model="viewMode" size="small">
                                    <el-radio-button label="grid">
                                        <i class="el-icon-s-grid"></i>
                                    </el-radio-button>
                                    <el-radio-button label="list">
                                        <i class="el-icon-s-order"></i>
                                    </el-radio-button>
                                </el-radio-group>
                            </div>
                        </div>

                        <!-- 网格视图 -->
                        <div v-if="viewMode === 'grid'" class="grid-view">
                            <div 
                                v-for="item in currentItems" 
                                :key="item.id"
                                class="grid-item"
                                @click="handleItemClick(item)"
                                @dblclick="handleItemDoubleClick(item)"
                            >
                                <div class="item-icon">
                                    <i :class="getItemIcon(item)"></i>
                                </div>
                                <div class="item-name">{{ item.name }}</div>
                                <div class="item-info">
                                    <span v-if="item.type === 'file'">{{ formatFileSize(item.size) }}</span>
                                    <span v-else>{{ item.itemCount }} 项</span>
                                </div>
                                <div class="item-actions">
                                    <el-button size="mini" type="text" @click.stop="shareItem(item)">
                                        <i class="el-icon-share"></i>
                                    </el-button>
                                    <el-button size="mini" type="text" @click.stop="deleteItem(item)">
                                        <i class="el-icon-delete"></i>
                                    </el-button>
                                </div>
                            </div>
                        </div>

                        <!-- 列表视图 -->
                        <div v-else class="list-view">
                            <el-table :data="currentItems" style="width: 100%">
                                <el-table-column prop="name" label="名称" min-width="200">
                                    <template slot-scope="scope">
                                        <i :class="getItemIcon(scope.row)" style="margin-right: 8px;"></i>
                                        {{ scope.row.name }}
                                    </template>
                                </el-table-column>
                                <el-table-column prop="type" label="类型" width="100">
                                    <template slot-scope="scope">
                                        {{ scope.row.type === 'folder' ? '文件夹' : '文件' }}
                                    </template>
                                </el-table-column>
                                <el-table-column prop="size" label="大小" width="120">
                                    <template slot-scope="scope">
                                        <span v-if="scope.row.type === 'file'">{{ formatFileSize(scope.row.size) }}</span>
                                        <span v-else>{{ scope.row.itemCount }} 项</span>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="modifiedTime" label="修改时间" width="180">
                                    <template slot-scope="scope">
                                        {{ formatDate(scope.row.modifiedTime) }}
                                    </template>
                                </el-table-column>
                                <el-table-column label="操作" width="200">
                                    <template slot-scope="scope">
                                        <el-button size="mini" @click="downloadItem(scope.row)" v-if="scope.row.type === 'file'">
                                            下载
                                        </el-button>
                                        <el-button size="mini" type="success" @click="shareItem(scope.row)">
                                            分享
                                        </el-button>
                                        <el-button size="mini" type="danger" @click="deleteItem(scope.row)">
                                            删除
                                        </el-button>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>

                        <!-- 空状态 -->
                        <div v-if="currentItems.length === 0" class="empty-state">
                            <i class="el-icon-folder-opened"></i>
                            <p>您的云盘还是空的</p>
                            <el-button type="primary" @click="showUploadDialog = true">上传第一个文件</el-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: "EncyclopediaView",
    data() {
        return {
            searchQuery: "",
            viewMode: "grid",
            currentPath: "/",
            showUploadDialog: false,
            showFolderDialog: false,
            showShareDialog: false,
            fileList: [],
            folderForm: { name: "" },
            shareForm: { type: "link", expiry: "7", email: "" },
            shareItemData: {},
            folderRules: {
                name: [
                    { required: true, message: "请输入文件夹名称", trigger: "blur" },
                    { min: 1, max: 50, message: "长度在 1 到 50 个字符", trigger: "blur" }
                ]
            },
            // 存储空间信息
            usedStorage: 2147483648, // 2GB
            totalStorage: 10737418240, // 10GB
            // 模拟数据
            treeData: [{
                id: "root",
                label: "我的文件",
                type: "folder",
                children: [
                    {
                        id: "documents",
                        label: "文档",
                        type: "folder",
                        children: [
                            { id: "work", label: "工作文档", type: "folder", itemCount: 8 },
                            { id: "personal", label: "个人文档", type: "folder", itemCount: 12 }
                        ]
                    },
                    {
                        id: "photos",
                        label: "照片",
                        type: "folder",
                        children: [
                            { id: "family", label: "家庭照片", type: "folder", itemCount: 156 },
                            { id: "travel", label: "旅行照片", type: "folder", itemCount: 89 }
                        ]
                    },
                    {
                        id: "music",
                        label: "音乐",
                        type: "folder",
                        itemCount: 45
                    },
                    {
                        id: "videos",
                        label: "视频",
                        type: "folder",
                        itemCount: 23
                    }
                ]
            }],
            treeProps: {
                children: "children",
                label: "label"
            },
            currentItems: [
                {
                    id: "documents",
                    name: "文档",
                    type: "folder",
                    itemCount: 20,
                    modifiedTime: "2024-01-15 10:30:00"
                },
                {
                    id: "photos",
                    name: "照片",
                    type: "folder",
                    itemCount: 245,
                    modifiedTime: "2024-01-14 16:45:00"
                },
                {
                    id: "music",
                    name: "音乐",
                    type: "folder",
                    itemCount: 45,
                    modifiedTime: "2024-01-13 09:20:00"
                },
                {
                    id: "videos",
                    name: "视频",
                    type: "folder",
                    itemCount: 23,
                    modifiedTime: "2024-01-12 14:15:00"
                },
                {
                    id: "resume",
                    name: "个人简历.pdf",
                    type: "file",
                    size: 1048576,
                    modifiedTime: "2024-01-10 11:30:00"
                }
            ]
        };
    },
    computed: {
        breadcrumbItems() {
            const paths = this.currentPath.split("/").filter(p => p);
            return [
                { name: "我的文件", path: "/" },
                ...paths.map((path, index) => ({
                    name: path,
                    path: "/" + paths.slice(0, index + 1).join("/")
                }))
            ];
        },
        canGoBack() {
            return this.currentPath !== "/";
        },
        uploadUrl() {
            return "/api/cloud/upload"; // 云盘上传接口
        },
        storagePercentage() {
            return Math.round((this.usedStorage / this.totalStorage) * 100);
        },
        storageColor() {
            const percentage = this.storagePercentage;
            if (percentage < 70) return "#67C23A";
            if (percentage < 90) return "#E6A23C";
            return "#F56C6C";
        }
    },
    methods: {
        handleSearch() {
            console.log("搜索:", this.searchQuery);
        },
        createFolder() {
            this.showFolderDialog = true;
        },
        confirmCreateFolder() {
            this.$refs.folderForm.validate((valid) => {
                if (valid) {
                    this.$message.success("文件夹创建成功");
                    this.showFolderDialog = false;
                    this.folderForm.name = "";
                }
            });
        },
        goBack() {
            if (this.canGoBack) {
                const paths = this.currentPath.split("/").filter(p => p);
                paths.pop();
                this.currentPath = "/" + paths.join("/");
                this.loadCurrentDirectory();
            }
        },
        goToRoot() {
            this.currentPath = "/";
            this.loadCurrentDirectory();
        },
        navigateToBreadcrumb(index) {
            if (index < this.breadcrumbItems.length - 1) {
                this.currentPath = this.breadcrumbItems[index].path;
                this.loadCurrentDirectory();
            }
        },
        handleTreeNodeClick(data) {
            if (data.type === "folder") {
                this.currentPath = "/" + data.id;
                this.loadCurrentDirectory();
            }
        },
        handleItemClick(item) {
            console.log("选中:", item);
        },
        handleItemDoubleClick(item) {
            if (item.type === "folder") {
                this.currentPath = this.currentPath + "/" + item.id;
                this.loadCurrentDirectory();
            } else {
                this.downloadItem(item);
            }
        },
        getItemIcon(item) {
            if (item.type === "folder") {
                return "el-icon-folder";
            }
            const ext = item.name.split(".").pop().toLowerCase();
            const iconMap = {
                pdf: "el-icon-document",
                doc: "el-icon-document",
                docx: "el-icon-document",
                jpg: "el-icon-picture",
                jpeg: "el-icon-picture",
                png: "el-icon-picture",
                mp4: "el-icon-video-camera",
                mp3: "el-icon-headphones"
            };
            return iconMap[ext] || "el-icon-document";
        },
        formatFileSize(bytes) {
            if (bytes === 0) return "0 B";
            const k = 1024;
            const sizes = ["B", "KB", "MB", "GB"];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
        },
        formatDate(dateString) {
            return new Date(dateString).toLocaleString("zh-CN");
        },
        downloadItem(item) {
            this.$message.success(`开始下载: ${item.name}`);
        },
        shareItem(item) {
            this.$message.info("分享功能开发中...");
        },
        deleteItem(item) {
            this.$confirm(`确定要删除 "${item.name}" 吗？`, "确认删除", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning"
            }).then(() => {
                this.$message.success("删除成功");
            }).catch(() => {});
        },
        loadCurrentDirectory() {
            console.log("加载目录:", this.currentPath);
        },
        handlePreview(file) {
            console.log("预览文件:", file);
        },
        handleRemove(file, fileList) {
            console.log("移除文件:", file);
        },
        beforeRemove(file, fileList) {
            return this.$confirm(`确定移除 ${file.name}？`);
        },
        handleExceed(files, fileList) {
            this.$message.warning(`当前限制选择 10 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`);
        },
        handleUploadDialogClose() {
            this.fileList = [];
            this.showUploadDialog = false;
        },
        confirmUpload() {
            this.$message.success("文件上传成功");
            this.showUploadDialog = false;
        },
        confirmShare() {
            this.$message.success("分享成功");
            this.showShareDialog = false;
        }
    }
};
</script>
