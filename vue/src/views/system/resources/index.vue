<template>
    <div class="information-view">
        <div class="information-body">
            <div class="file-explorer">
                <!-- 顶部工具栏 -->
                <div class="toolbar">
                    <div class="search-box">
                        <el-input
                            v-model="searchQuery"
                            placeholder="搜索文件或文件夹..."
                            prefix-icon="el-icon-search"
                            clearable
                            @input="handleSearch"
                        />
                    </div>
                    <div class="toolbar-actions">
                        <el-button type="primary" @click="showUploadDialog = true">
                            <i class="el-icon-upload"></i> 上传文件
                        </el-button>
                        <el-button @click="showFolderUploadDialog = true">
                            <i class="el-icon-folder-opened"></i> 上传文件夹
                        </el-button>
                        <el-button @click="createFolder">
                            <i class="el-icon-folder-add"></i> 新建文件夹
                        </el-button>
                        <el-button 
                            @click="batchDownload" 
                            :disabled="!hasSelectedItems"
                            type="success"
                        >
                            <i class="el-icon-download"></i> 批量下载
                        </el-button>
                        <el-button 
                            @click="batchDelete" 
                            :disabled="!hasSelectedItems"
                            type="danger"
                        >
                            <i class="el-icon-delete"></i> 批量删除
                        </el-button>
                        <el-button 
                            @click="renameSelectedItem" 
                            :disabled="selectedItems.length !== 1"
                        >
                            <i class="el-icon-edit"></i> 重命名
                        </el-button>
                        <el-button @click="goBack" :disabled="!canGoBack">
                            <i class="el-icon-back"></i> 返回上级
                        </el-button>
                        <el-button @click="goToRoot">
                            <i class="el-icon-house"></i> 返回根目录
                        </el-button>
                        <el-button @click="refreshAll" type="info">
                            <i class="el-icon-refresh"></i> 刷新
                        </el-button>
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
                    <!-- 左侧文件树 -->
                    <div class="path-tree">
                        <h3>目录结构</h3>
                        <el-tree
                            :data="treeData"
                            :props="treeProps"
                            @node-click="handleTreeNodeClick"
                            @node-expand="handleNodeExpand"
                            @node-collapse="handleNodeCollapse"
                            :default-expanded-keys="expandedKeys"
                            node-key="path"
                            :expand-on-click-node="false"
                            :highlight-current="true"
                            :current-node-key="currentPath"
                        >
                            <span class="custom-tree-node" slot-scope="{ node, data }">
                                <img 
                                    :src="getIconPath('folder')" 
                                    :alt="node.label"
                                    class="tree-icon"
                                />
                                <span>{{ node.label }}</span>
                                <span v-if="data.children && data.children.length > 0" class="item-count">
                                    ({{ data.children.length }})
                                </span>
                            </span>
                        </el-tree>
                    </div>

                    <!-- 右侧文件列表 -->
                    <div class="file-list">
                        <div class="list-header">
                            <h3>文件列表</h3>
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
                        
                        <!-- 加载状态 -->
                        <div v-if="loading" class="loading-container">
                            <el-loading :loading="loading" text="加载中...">
                                <div style="height: 200px;"></div>
                            </el-loading>
                        </div>

                        <!-- 网格视图 -->
                        <div v-if="viewMode === 'grid'" class="grid-view">
                            <div 
                                v-for="item in displayItems" 
                                :key="item.path"
                                class="grid-item"
                                :class="{ 'selected': isSelected(item) }"
                                @click="handleItemClick(item)"
                            >
                                <div class="item-checkbox">
                                    <el-checkbox 
                                        :value="isSelected(item)"
                                        @change="toggleSelection(item)"
                                        @click.native.stop
                                    ></el-checkbox>
                                </div>
                                <div class="item-icon">
                                    <img 
                                        :src="getIconPath(getItemIcon(item))" 
                                        :alt="item.name"
                                        class="file-icon"
                                    />
                                </div>
                                <div class="item-name">{{ item.name }}</div>
                                <div class="item-info">
                                    <span v-if="item.type === 'file'">{{ formatFileSize(item.size) }}</span>
                                    <span v-else>{{ item.itemCount }} 项</span>
                                </div>
                                <div class="item-actions">
                                    <el-button 
                                        size="mini" 
                                        type="text" 
                                        @click.stop="downloadItem(item)" 
                                        v-if="item.type === 'file'"
                                        title="下载文件"
                                    >
                                        <i class="el-icon-download"></i>
                                    </el-button>
                                    <el-button 
                                        size="mini" 
                                        type="text" 
                                        @click.stop="downloadFolderItem(item)" 
                                        v-if="item.type === 'folder'"
                                        title="下载文件夹"
                                    >
                                        <i class="el-icon-download"></i>
                                    </el-button>
                                    <el-button 
                                        size="mini" 
                                        type="text" 
                                        @click.stop="renameItem(item)"
                                        title="重命名"
                                    >
                                        <i class="el-icon-edit"></i>
                                    </el-button>
                                    <el-button 
                                        size="mini" 
                                        type="text" 
                                        @click.stop="deleteItem(item)"
                                        title="删除"
                                    >
                                        <i class="el-icon-delete"></i>
                                    </el-button>
                                </div>
                            </div>
                        </div>

                        <!-- 列表视图 -->
                        <div v-else class="list-view">
                            <el-table 
                                :data="displayItems" 
                                style="width: 100%"
                                @selection-change="handleSelectionChange"
                                @row-click="handleTableRowClick"
                                ref="fileTable"
                            >
                                <el-table-column type="selection" width="55"></el-table-column>
                                <el-table-column prop="name" label="名称" min-width="200">
                                    <template slot-scope="scope">
                                        <img 
                                            :src="getIconPath(getItemIcon(scope.row))" 
                                            :alt="scope.row.name"
                                            class="file-icon-small"
                                        />
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
                                        <el-button size="mini" @click="downloadFolderItem(scope.row)" v-if="scope.row.type === 'folder'">
                                            下载
                                        </el-button>
                                        <el-button size="mini" @click="renameItem(scope.row)">
                                            重命名
                                        </el-button>
                                        <el-button size="mini" type="danger" @click="deleteItem(scope.row)">
                                            删除
                                        </el-button>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>

                        <!-- 空状态 -->
                        <div v-if="displayItems.length === 0" class="empty-state">
                            <i class="el-icon-folder-opened" v-if="!isSearchMode"></i>
                            <i class="el-icon-search" v-else></i>
                            <p v-if="!isSearchMode">当前目录为空</p>
                            <p v-else>未找到匹配的文件或文件夹</p>
                            <el-button type="primary" @click="showUploadDialog = true" v-if="!isSearchMode">上传文件</el-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 上传文件对话框 -->
        <el-dialog
            title="上传文件"
            :visible.sync="showUploadDialog"
            width="600px"
            :before-close="handleUploadDialogClose"
        >
            <div class="upload-content">
                <el-upload
                    ref="upload"
                    :auto-upload="false"
                    :on-preview="handlePreview"
                    :on-remove="handleRemove"
                    :before-remove="beforeRemove"
                    multiple
                    :limit="50"
                    :on-exceed="handleExceed"
                    :file-list="fileList"
                    drag
                >
                    <i class="el-icon-upload"></i>
                    <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                    <div class="el-upload__tip" slot="tip">支持单个或多个文件上传，最多50个文件，每个文件不超过500MB</div>
                </el-upload>
            </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showUploadDialog = false">取消</el-button>
                <el-button type="primary" @click="confirmUpload">确认上传</el-button>
            </span>
        </el-dialog>

        <!-- 新建文件夹对话框 -->
        <el-dialog
            title="新建文件夹"
            :visible.sync="showFolderDialog"
            width="400px"
        >
            <el-form :model="folderForm" :rules="folderRules" ref="folderForm">
                <el-form-item label="文件夹名称" prop="name">
                    <el-input v-model="folderForm.name" placeholder="请输入文件夹名称"></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showFolderDialog = false">取消</el-button>
                <el-button type="primary" @click="confirmCreateFolder">确认创建</el-button>
            </span>
        </el-dialog>

        <!-- 重命名对话框 -->
        <el-dialog
            title="重命名"
            :visible.sync="showRenameDialog"
            width="400px"
        >
            <el-form :model="renameForm" :rules="renameRules" ref="renameForm">
                <el-form-item label="新名称" prop="name">
                    <el-input v-model="renameForm.name" placeholder="请输入新名称"></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showRenameDialog = false">取消</el-button>
                <el-button type="primary" @click="confirmRename">确认重命名</el-button>
            </span>
        </el-dialog>


        <!-- 文件夹上传对话框 -->
        <el-dialog
            title="上传文件夹"
            :visible.sync="showFolderUploadDialog"
            width="600px"
            :before-close="handleFolderUploadDialogClose"
        >
            <div class="upload-content">
                <div class="upload-notice">
                    <el-alert
                        title="文件夹上传说明"
                        type="info"
                        :closable="false"
                        show-icon
                    >
                        <p>由于技术限制，请先将文件夹压缩为ZIP格式，然后上传压缩包。</p>
                        <p>压缩包上传后会自动解压到当前目录。</p>
                    </el-alert>
                </div>
                <el-upload
                    ref="folderUpload"
                    :auto-upload="false"
                    :on-preview="handlePreview"
                    :on-remove="handleRemove"
                    :before-remove="beforeRemove"
                    :file-list="folderUploadList"
                    :limit="1"
                    accept=".zip"
                    drag
                >
                    <i class="el-icon-folder-opened"></i>
                    <div class="el-upload__text">选择ZIP压缩包上传</div>
                    <div class="el-upload__tip" slot="tip">请选择ZIP格式的压缩包文件</div>
                </el-upload>
            </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showFolderUploadDialog = false">取消</el-button>
                <el-button type="primary" @click="confirmFolderUpload">确认上传</el-button>
            </span>
        </el-dialog>

    </div>
</template>

<script>
import { 
    getFileList, 
    createFolder, 
    downloadFile, 
    downloadFolder,
    deleteFile,
    uploadBatch,
    uploadFolder,
    downloadBatch,
    deleteBatch,
    renameFile,
    searchFiles,
    getFileTree
} from "@/api/system/files";

export default {
    name: "InformationView",
    data() {
        return {
            searchQuery: "",
            viewMode: "grid",
            currentPath: "/",
            showUploadDialog: false,
            showFolderDialog: false,
            showRenameDialog: false,
            showFolderUploadDialog: false,
            loading: false,
            isSearchMode: false,
            selectedItems: [],
            folderForm: {
                name: ""
            },
            renameForm: {
                name: ""
            },
            folderRules: {
                name: [
                    { required: true, message: "请输入文件夹名称", trigger: "blur" },
                    { min: 1, max: 50, message: "长度在 1 到 50 个字符", trigger: "blur" }
                ]
            },
            renameRules: {
                name: [
                    { required: true, message: "请输入新名称", trigger: "blur" },
                    { min: 1, max: 50, message: "长度在 1 到 50 个字符", trigger: "blur" }
                ]
            },
            treeData: [],
            treeProps: {
                children: "children",
                label: "name"
            },
            expandedKeys: ["/"],
            currentItems: [],
            searchResults: [],
            fileList: [],
            folderUploadList: [],
            currentRenameItem: null
        };
    },
    computed: {
        breadcrumbItems() {
            const paths = this.currentPath.split("/").filter(p => p);
            return [
                { name: "根目录", path: "/" },
                ...paths.map((path, index) => ({
                    name: path,
                    path: "/" + paths.slice(0, index + 1).join("/")
                }))
            ];
        },
        canGoBack() {
            return this.currentPath !== "/";
        },
        displayItems() {
            return this.isSearchMode ? this.searchResults : this.currentItems;
        },
        hasSelectedItems() {
            return this.selectedItems.length > 0;
        },
        selectedFiles() {
            return this.selectedItems.filter(item => item.type === 'file');
        },
        selectedFolders() {
            return this.selectedItems.filter(item => item.type === 'folder');
        }
    },
    watch: {
        viewMode(newMode, oldMode) {
            // 当切换视图时，同步选中状态
            this.$nextTick(() => {
                if (newMode === 'list' && this.$refs.fileTable) {
                    // 切换到列表视图时，同步表格选中状态
                    this.syncTableSelection();
                } else if (newMode === 'grid') {
                    // 切换到网格视图时，网格视图的选中状态通过isSelected计算属性自动处理
                    this.syncGridSelection();
                }
            });
        }
    },
    async mounted() {
        await this.loadCurrentDirectory();
        await this.loadFileTree();
    },
    methods: {
        async handleSearch() {
            if (this.searchQuery.trim()) {
                this.isSearchMode = true;
                this.loading = true;
                try {
                    const token = this.$store.state.token;
                    const response = await searchFiles(this.searchQuery, this.currentPath, token);
                    if (response.data.code === 200) {
                        this.searchResults = response.data.data || [];
                        this.$message.success(`找到 ${this.searchResults.length} 个结果`);
                    } else {
                        this.$message.error(response.data.message || "搜索失败");
                        this.searchResults = [];
                    }
                } catch (error) {
                    console.error("搜索失败:", error);
                    this.$message.error("搜索失败，请重试");
                    this.searchResults = [];
                } finally {
                    this.loading = false;
                }
            } else {
                this.isSearchMode = false;
                this.searchResults = [];
            }
        },
        createFolder() {
            this.showFolderDialog = true;
        },
        async confirmCreateFolder() {
            this.$refs.folderForm.validate(async (valid) => {
                if (valid) {
                    try {
                        const token = this.$store.state.token;
                        const response = await createFolder(this.currentPath, this.folderForm.name, token);
                        if (response.data.code === 200) {
                            this.$message.success(response.data.message || "文件夹创建成功");
                            this.showFolderDialog = false;
                            this.folderForm.name = "";
                            await this.loadCurrentDirectory();
                            await this.loadFileTree(); // 刷新文件树
                        } else {
                            this.$message.error(response.data.message || "创建文件夹失败");
                        }
                    } catch (error) {
                        console.error("创建文件夹失败:", error);
                        this.$message.error("创建文件夹失败，请重试");
                    }
                }
            });
        },
        async loadCurrentDirectory() {
            this.loading = true;
            try {
                const token = this.$store.state.token;
                const response = await getFileList(this.currentPath, token);
                if (response.data.code === 200) {
                    this.currentItems = response.data.data || [];
                    // 更新展开状态
                    this.updateExpandedKeys();
                    // 如果是列表视图，同步表格选中状态
                    if (this.viewMode === 'list' && this.$refs.fileTable) {
                        this.$nextTick(() => {
                            this.syncTableSelection();
                        });
                    }
                } else {
                    this.$message.error(response.data.message || "加载目录失败");
                    this.currentItems = [];
                }
            } catch (error) {
                console.error("加载目录失败:", error);
                this.$message.error("加载目录失败，请重试");
                this.currentItems = [];
            } finally {
                this.loading = false;
            }
        },
        goBack() {
            if (this.canGoBack) {
                // 清除选中状态
                this.selectedItems = [];
                if (this.$refs.fileTable) {
                    this.$refs.fileTable.clearSelection();
                }
                const paths = this.currentPath.split("/").filter(p => p);
                paths.pop();
                this.currentPath = "/" + paths.join("/");
                this.loadCurrentDirectory();
            }
        },
        goToRoot() {
            // 清除选中状态
            this.selectedItems = [];
            if (this.$refs.fileTable) {
                this.$refs.fileTable.clearSelection();
            }
            this.currentPath = "/";
            this.loadCurrentDirectory();
        },
        navigateToBreadcrumb(index) {
            if (index < this.breadcrumbItems.length - 1) {
                // 清除选中状态
                this.selectedItems = [];
                if (this.$refs.fileTable) {
                    this.$refs.fileTable.clearSelection();
                }
                this.currentPath = this.breadcrumbItems[index].path;
                this.loadCurrentDirectory();
            }
        },
        handleTreeNodeClick(data) {
            // 清除选中状态
            this.selectedItems = [];
            if (this.$refs.fileTable) {
                this.$refs.fileTable.clearSelection();
            }
            this.currentPath = data.path;
            this.loadCurrentDirectory();
            // 更新展开状态
            this.updateExpandedKeys();
        },
        handleItemClick(item) {
            // 如果是文件夹，进入文件夹；如果是文件，选中项目
            if (item.type === "folder") {
                // 清除选中状态
                this.selectedItems = [];
                if (this.$refs.fileTable) {
                    this.$refs.fileTable.clearSelection();
                }
                this.currentPath = item.path;
                this.loadCurrentDirectory();
            } else {
                // 选中文件
                this.toggleSelection(item);
            }
        },
        handleTableRowClick(row, column, event) {
            // 如果是文件夹，进入文件夹；如果是文件，不产生任何操作
            if (row.type === "folder") {
                // 清除选中状态
                this.selectedItems = [];
                if (this.$refs.fileTable) {
                    this.$refs.fileTable.clearSelection();
                }
                this.currentPath = row.path;
                this.loadCurrentDirectory();
            }
            // 文件在列表视图中单击不产生任何操作
        },
        handleItemDoubleClick(item) {
            if (item.type === "folder") {
                this.currentPath = item.path;
                this.loadCurrentDirectory();
            } else {
                this.downloadItem(item);
            }
        },
        getItemIcon(item) {
            if (item.type === "folder") {
                return "folder";
            }
            // 根据文件扩展名返回对应的图标文件名
            const ext = item.name.split(".").pop().toLowerCase();
            const iconMap = {
                // 文档类型
                pdf: "pdf",
                doc: "doc",
                docx: "doc",
                txt: "txt",
                rtf: "doc",
                
                // 表格类型
                xls: "xls",
                xlsx: "xls",
                csv: "csv",
                
                // 演示文稿
                ppt: "ppt",
                pptx: "ppt",
                
                // 图片类型
                jpg: "picture",
                jpeg: "picture",
                png: "picture",
                gif: "picture",
                bmp: "picture",
                svg: "picture",
                webp: "picture",
                psd: "psd",
                
                // 视频类型
                mp4: "video",
                avi: "video",
                mov: "video",
                wmv: "video",
                flv: "video",
                mkv: "video",
                webm: "video",
                
                // 音频类型
                mp3: "music",
                wav: "music",
                flac: "music",
                aac: "music",
                ogg: "music",
                
                // 压缩文件
                zip: "compress",
                rar: "compress",
                "7z": "compress",
                tar: "compress",
                gz: "compress",
                
                // 可执行文件
                exe: "exe",
                msi: "exe",
                app: "exe",
                
                // 代码文件
                js: "javascript",
                ts: "javascript",
                html: "html",
                htm: "html",
                css: "css",
                scss: "css",
                sass: "css",
                less: "css",
                json: "json",
                xml: "xml",
                java: "java",
                class: "java",
                c: "c",
                cpp: "cpp",
                cc: "cpp",
                cxx: "cpp",
                py: "python",
                pyc: "python",
                sql: "sql",
                jsp: "jsp",
                
                // 其他类型
                torrent: "torrent",
                bin: "binary",
                dat: "binary",
                other: "other"
            };
            return iconMap[ext] || "other";
        },
        
        getIconPath(iconName) {
            return require(`@/assets/images/resources/${iconName}.png`);
        },
        formatFileSize(bytes) {
            if (bytes === 0) return "0 B";
            const k = 1024;
            const sizes = ["B", "KB", "MB", "GB"];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
        },
        formatDate(timestamp) {
            if (!timestamp) return '未知';
            // 处理时间戳（毫秒）
            const date = new Date(timestamp);
            return date.toLocaleString("zh-CN");
        },
        async downloadItem(item) {
            try {
                const token = this.$store.state.token;
                const response = await downloadFile(item.path, token);
                // 创建下载链接
                const url = window.URL.createObjectURL(new Blob([response.data]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', item.name);
                document.body.appendChild(link);
                link.click();
                link.remove();
                window.URL.revokeObjectURL(url);
                this.$message.success(`开始下载: ${item.name}`);
            } catch (error) {
                console.error("下载文件失败:", error);
                this.$message.error("下载文件失败，请重试");
            }
        },
        
        async downloadFolderItem(item) {
            try {
                const token = this.$store.state.token;
                const response = await downloadFolder(item.path, token);
                // 创建下载链接
                const url = window.URL.createObjectURL(new Blob([response.data]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', `${item.name}.zip`);
                document.body.appendChild(link);
                link.click();
                link.remove();
                window.URL.revokeObjectURL(url);
                this.$message.success(`开始下载文件夹: ${item.name}`);
            } catch (error) {
                console.error("下载文件夹失败:", error);
                this.$message.error("下载文件夹失败，请重试");
            }
        },
        deleteItem(item) {
            this.$confirm(`确定要删除 "${item.name}" 吗？`, "确认删除", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning"
            }).then(async () => {
                try {
                    const token = this.$store.state.token;
                    const response = await deleteFile(item.path, token);
                    if (response.data.code === 200) {
                        this.$message.success(response.data.message || "删除成功");
                        await this.loadCurrentDirectory();
                        await this.loadFileTree(); // 刷新文件树
                    } else {
                        this.$message.error(response.data.message || "删除失败");
                    }
                } catch (error) {
                    console.error("删除失败:", error);
                    this.$message.error("删除失败，请重试");
                }
            }).catch(() => {});
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
            this.$refs.upload.clearFiles(); // 清空上传组件的文件列表
            this.showUploadDialog = false;
        },
        async confirmUpload() {
            try {
                const token = this.$store.state.token;
                const uploadFiles = this.$refs.upload.uploadFiles;
                
                if (uploadFiles.length === 0) {
                    this.$message.warning("请选择要上传的文件");
                    return;
                }
                
                const files = uploadFiles.map(file => file.raw);
                const response = await uploadBatch(this.currentPath, files, token);
                if (response.data.code === 200) {
                    this.$message.success(response.data.message || "文件上传成功");
                    this.showUploadDialog = false;
                    this.$refs.upload.clearFiles();
                    await this.loadCurrentDirectory();
                } else {
                        this.$message.error(response.data.message || "文件上传失败");
                }
            } catch (error) {
                console.error("上传失败:", error);
                this.$message.error("文件上传失败，请重试");
            }
        },
        
        // 选择相关方法
        isSelected(item) {
            return this.selectedItems.some(selected => selected.path === item.path);
        },
        
        // 同步表格选中状态
        syncTableSelection() {
            if (!this.$refs.fileTable) return;
            
            // 清除所有选中状态
            this.$refs.fileTable.clearSelection();
            
            // 延迟执行，确保DOM已更新
            setTimeout(() => {
                if (!this.$refs.fileTable) return;
                
                // 根据selectedItems重新设置选中状态
                this.selectedItems.forEach(selectedItem => {
                    const tableData = this.displayItems;
                    const rowIndex = tableData.findIndex(item => item.path === selectedItem.path);
                    if (rowIndex !== -1) {
                        this.$refs.fileTable.toggleRowSelection(tableData[rowIndex], true);
                    }
                });
            }, 50);
        },
        
        // 同步网格视图选中状态
        syncGridSelection() {
            // 网格视图的选中状态通过isSelected计算属性自动处理
            // 这里可以添加其他需要的同步逻辑
        },
        
        toggleSelection(item) {
            const index = this.selectedItems.findIndex(selected => selected.path === item.path);
            if (index > -1) {
                this.selectedItems.splice(index, 1);
            } else {
                this.selectedItems.push(item);
            }
            
            // 如果当前是列表视图，同步表格选中状态
            if (this.viewMode === 'list' && this.$refs.fileTable) {
                this.$nextTick(() => {
                    this.syncTableSelection();
                });
            }
        },
        
        handleSelectionChange(selection) {
            this.selectedItems = selection;
        },
        
        // 重命名功能
        renameItem(item) {
            this.renameForm.name = item.name;
            this.currentRenameItem = item;
            this.showRenameDialog = true;
        },
        
        // 重命名选中的项目
        renameSelectedItem() {
            if (this.selectedItems.length === 1) {
                this.renameItem(this.selectedItems[0]);
            } else {
                this.$message.warning("请选择一个文件或文件夹进行重命名");
            }
        },
        
        async confirmRename() {
            this.$refs.renameForm.validate(async (valid) => {
                if (valid) {
                    try {
                        const token = this.$store.state.token;
                        const response = await renameFile(this.currentRenameItem.path, this.renameForm.name, token);
                        if (response.data.code === 200) {
                            this.$message.success(response.data.message || "重命名成功");
                            this.showRenameDialog = false;
                            this.renameForm.name = "";
                            this.currentRenameItem = null;
                            // 清除选中状态
                            this.selectedItems = [];
                            // 清除表格选中状态
                            if (this.$refs.fileTable) {
                                this.$refs.fileTable.clearSelection();
                            }
                            await this.loadCurrentDirectory();
                            await this.loadFileTree(); // 刷新文件树
                        } else {
                            this.$message.error(response.data.message || "重命名失败");
                        }
                    } catch (error) {
                        console.error("重命名失败:", error);
                        this.$message.error("重命名失败，请重试");
                    }
                }
            });
        },
        
        // 批量下载
        async batchDownload() {
            if (this.selectedItems.length === 0) {
                this.$message.warning("请选择要下载的文件或文件夹");
                return;
            }
            
            try {
                const token = this.$store.state.token;
                const paths = this.selectedItems.map(item => item.path);
                const response = await downloadBatch(paths, token);
                
                // 创建下载链接
                const url = window.URL.createObjectURL(new Blob([response.data]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', `批量下载_${new Date().getTime()}.zip`);
                document.body.appendChild(link);
                link.click();
                link.remove();
                window.URL.revokeObjectURL(url);
                
                const fileCount = this.selectedFiles.length;
                const folderCount = this.selectedFolders.length;
                let message = `开始下载 ${this.selectedItems.length} 个项目`;
                if (fileCount > 0 && folderCount > 0) {
                    message = `开始下载 ${fileCount} 个文件和 ${folderCount} 个文件夹`;
                } else if (fileCount > 0) {
                    message = `开始下载 ${fileCount} 个文件`;
                } else if (folderCount > 0) {
                    message = `开始下载 ${folderCount} 个文件夹`;
                }
                
                this.$message.success(message);
            } catch (error) {
                console.error("批量下载失败:", error);
                this.$message.error("批量下载失败，请重试");
            }
        },
        
        // 批量删除
        async batchDelete() {
            if (this.selectedItems.length === 0) {
                this.$message.warning("请选择要删除的项目");
                return;
            }
            
            this.$confirm(`确定要删除选中的 ${this.selectedItems.length} 个项目吗？`, "确认删除", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning"
            }).then(async () => {
                try {
                    const token = this.$store.state.token;
                    const paths = this.selectedItems.map(item => item.path);
                    const response = await deleteBatch(paths, token);
                    if (response.data.code === 200) {
                        this.$message.success(response.data.message || "批量删除成功");
                        this.selectedItems = [];
                        await this.loadCurrentDirectory();
                        await this.loadFileTree(); // 刷新文件树
                    } else {
                        this.$message.error(response.data.message || "批量删除失败");
                    }
                } catch (error) {
                    console.error("批量删除失败:", error);
                    this.$message.error("批量删除失败，请重试");
                }
            }).catch(() => {});
        },
        
        
        // 文件夹上传（ZIP压缩包）
        async confirmFolderUpload() {
            try {
                const token = this.$store.state.token;
                const uploadFiles = this.$refs.folderUpload.uploadFiles;
                
                if (uploadFiles.length === 0) {
                    this.$message.warning("请选择要上传的ZIP压缩包");
                    return;
                }
                
                if (uploadFiles.length > 1) {
                    this.$message.warning("文件夹上传只能选择一个ZIP压缩包");
                    return;
                }
                
                // 检查文件格式
                const file = uploadFiles[0];
                const fileName = file.name.toLowerCase();
                if (!fileName.endsWith('.zip')) {
                    this.$message.error("请选择ZIP格式的压缩包文件");
                    return;
                }
                
                const response = await uploadFolder(this.currentPath, file.raw, token);
                if (response.data.code === 200) {
                    this.$message.success(response.data.message || "ZIP压缩包上传成功，已自动解压");
                    this.showFolderUploadDialog = false;
                    this.$refs.folderUpload.clearFiles();
                    await this.loadCurrentDirectory();
                    await this.loadFileTree(); // 刷新文件树
                } else {
                    this.$message.error(response.data.message || "ZIP压缩包上传失败");
                }
            } catch (error) {
                console.error("ZIP压缩包上传失败:", error);
                this.$message.error("ZIP压缩包上传失败，请重试");
            }
        },
        
        // 对话框关闭处理
        
        handleFolderUploadDialogClose() {
            this.$refs.folderUpload.clearFiles();
            this.showFolderUploadDialog = false;
        },
        
        // 加载文件树
        async loadFileTree() {
            try {
                const token = this.$store.state.token;
                const response = await getFileTree("/", token);
                if (response.data.code === 200) {
                    this.treeData = this.convertTreeData(response.data.data || []);
                    // 默认展开根目录和当前路径的父级目录
                    this.updateExpandedKeys();
                }
            } catch (error) {
                console.error("加载文件树失败:", error);
            }
        },
        
        // 刷新所有数据
        async refreshAll() {
            this.loading = true;
            try {
                // 清除选中状态
                this.selectedItems = [];
                if (this.$refs.fileTable) {
                    this.$refs.fileTable.clearSelection();
                }
                
                // 退出搜索模式
                this.isSearchMode = false;
                this.searchQuery = "";
                this.searchResults = [];
                
                // 刷新文件列表和文件树
                await Promise.all([
                    this.loadCurrentDirectory(),
                    this.loadFileTree()
                ]);
                
                this.$message.success("刷新成功");
            } catch (error) {
                console.error("刷新失败:", error);
                this.$message.error("刷新失败，请重试");
            } finally {
                this.loading = false;
            }
        },
        
        convertTreeData(treeData) {
            return treeData.map(item => ({
                name: item.name,
                    path: item.path,
                children: item.children && item.children.length > 0 ? this.convertTreeData(item.children) : []
            }));
        },
        
        // 更新展开的节点键
        updateExpandedKeys() {
            const keys = ["/"];
            const pathParts = this.currentPath.split("/").filter(p => p);
            let currentPath = "/";
            
            for (let i = 0; i < pathParts.length; i++) {
                currentPath += (currentPath === "/" ? "" : "/") + pathParts[i];
                keys.push(currentPath);
            }
            
            this.expandedKeys = keys;
        },
        
        
        // 处理节点展开
        handleNodeExpand(data, node) {
            if (!this.expandedKeys.includes(data.path)) {
                this.expandedKeys.push(data.path);
            }
        },
        
        // 处理节点收起
        handleNodeCollapse(data, node) {
            const index = this.expandedKeys.indexOf(data.path);
            if (index > -1) {
                this.expandedKeys.splice(index, 1);
            }
        }
    }
};
</script>
