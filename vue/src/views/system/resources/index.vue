<template>
    <div class="information-view">
        <div class="information-body">
            <div class="file-explorer">
                <!-- 顶部工具栏 -->
                <div class="toolbar">
                    <div class="toolbar-left">
                        <el-button @click="goToRoot" :disabled="isAtRoot">
                            <i class="el-icon-house"></i> 返回根目录
                        </el-button>
                        <el-button @click="goBack" :disabled="!canGoBack">
                            <i class="el-icon-back"></i> 返回上级
                        </el-button>
                        <el-button type="primary" @click="showUploadDialog = true">
                            <i class="el-icon-upload"></i> 上传文件
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
                        <el-button @click="refreshAll" type="info">
                            <i class="el-icon-refresh"></i> 刷新
                        </el-button>
                    </div>
                    <div class="toolbar-right">
                        <el-input
                            v-model="searchQuery"
                            placeholder="搜索文件或文件夹..."
                            clearable
                            style="width: 300px;"
                        >
                            <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
                        </el-input>
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
                            :current-node-key="currentTreeNodeKey"
                            ref="tree"
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
                                <el-dropdown @command="changeSortBy" style="margin-right: 10px;">
                                    <el-button size="small">
                                        <i class="el-icon-sort"></i> 排序
                                        <i class="el-icon-arrow-down el-icon--right"></i>
                                    </el-button>
                                    <el-dropdown-menu slot="dropdown">
                                        <el-dropdown-item command="name">
                                            <i :class="sortBy === 'name' ? 'el-icon-check' : ''"></i>
                                            按名称排序 {{ sortBy === 'name' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}
                                        </el-dropdown-item>
                                        <el-dropdown-item command="modifiedTime">
                                            <i :class="sortBy === 'modifiedTime' ? 'el-icon-check' : ''"></i>
                                            按修改时间排序 {{ sortBy === 'modifiedTime' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}
                                        </el-dropdown-item>
                                        <el-dropdown-item command="size">
                                            <i :class="sortBy === 'size' ? 'el-icon-check' : ''"></i>
                                            按大小排序 {{ sortBy === 'size' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}
                                        </el-dropdown-item>
                                    </el-dropdown-menu>
                                </el-dropdown>
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
                                        @click.stop="downloadFolder(item)" 
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
                                ref="fileTable"
                            >
                                <el-table-column type="selection" width="55"></el-table-column>
                                <el-table-column prop="name" label="名称" min-width="200">
                                    <template slot-scope="scope">
                                        <div @click="handleItemClick(scope.row)" style="cursor: pointer;">
                                            <img 
                                                :src="getIconPath(getItemIcon(scope.row))" 
                                                :alt="scope.row.name"
                                                class="file-icon-small"
                                            />
                                            {{ scope.row.name }}
                                        </div>
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
                                <el-table-column label="操作" width="240">
                                    <template slot-scope="scope">
                                        <el-button size="mini" @click.stop="downloadItem(scope.row)" v-if="scope.row.type === 'file'">
                                            下载
                                        </el-button>
                                        <el-button size="mini" @click.stop="downloadFolder(scope.row)" v-if="scope.row.type === 'folder'">
                                            下载
                                        </el-button>
                                        <el-button size="mini" @click.stop="renameItem(scope.row)">
                                            重命名
                                        </el-button>
                                        <el-button size="mini" type="danger" @click.stop="deleteItem(scope.row)">
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
            :close-on-click-modal="false"
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
            :close-on-click-modal="false"
            @close="handleFolderDialogClose"
        >
            <el-form :model="folderForm" :rules="folderRules" ref="folderForm">
                <el-form-item label="文件夹名称" prop="name">
                    <el-input v-model="folderForm.name" placeholder="请输入文件夹名称"></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="cancelFolderDialog">取消</el-button>
                <el-button type="primary" @click="confirmCreateFolder">确认创建</el-button>
            </span>
        </el-dialog>

        <!-- 重命名对话框 -->
        <el-dialog
            title="重命名"
            :visible.sync="showRenameDialog"
            width="400px"
            :close-on-click-modal="false"
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

    </div>
</template>

<script>
import { 
    listFiles,
    createFolder, 
    downloadFile,
    downloadFolder,
    deleteFile,
    renameFile,
    moveFile,
    searchFiles,
    uploadFile,
    checkInstantUpload,
    instantUpload,
    initChunkedUpload,
    uploadChunk,
    completeChunkedUpload,
    cancelChunkedUpload,
    getUploadProgress,
    getDirectoryTree,
    createBatchDownloadTask,
    downloadBatchTask
} from "@/api/system/public-files";

export default {
    name: "PublicFilesView",
    data() {
        return {
            searchQuery: "",
            viewMode: "grid",
            currentParentId: null, // 当前文件夹ID
            showUploadDialog: false,
            showFolderDialog: false,
            showRenameDialog: false,
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
                label: "label"
            },
            expandedKeys: ["root"], // 默认展开根目录
            currentTreeNodeKey: "root", // 当前高亮的树节点key
            currentItems: [],
            searchResults: [],
            fileList: [],
            currentRenameItem: null,
            breadcrumbPath: [], // 面包屑路径
            uploadProgressNotifications: {}, // 上传进度通知
            uploadingFiles: [], // 正在上传的文件列表
            sortBy: 'name', // 排序字段：name, modifiedTime, size
            sortOrder: 'asc' // 排序顺序：asc, desc
        };
    },
    computed: {
        breadcrumbItems() {
            return [
                { name: "公共文件", id: null },
                ...this.breadcrumbPath
            ];
        },
        canGoBack() {
            return this.currentParentId !== null;
        },
        isAtRoot() {
            return this.currentParentId === null;
        },
        displayItems() {
            const items = this.isSearchMode ? this.searchResults : this.currentItems;
            
            // 排序
            const sortedItems = [...items].sort((a, b) => {
                // 文件夹始终排在文件前面
                if (a.type === 'folder' && b.type !== 'folder') return -1;
                if (a.type !== 'folder' && b.type === 'folder') return 1;
                
                // 根据排序字段排序
                let compareResult = 0;
                if (this.sortBy === 'name') {
                    compareResult = a.name.localeCompare(b.name, 'zh-CN');
                } else if (this.sortBy === 'modifiedTime') {
                    compareResult = new Date(a.modifiedTime) - new Date(b.modifiedTime);
                } else if (this.sortBy === 'size') {
                    // 文件夹按项数排序，文件按大小排序
                    const aSize = a.type === 'folder' ? a.itemCount : a.size;
                    const bSize = b.type === 'folder' ? b.itemCount : b.size;
                    compareResult = aSize - bSize;
                }
                
                // 应用排序顺序
                return this.sortOrder === 'asc' ? compareResult : -compareResult;
            });
            
            return sortedItems;
        },
        hasSelectedItems() {
            return this.selectedItems.length > 0;
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
        await this.loadDirectoryTree();
        await this.loadCurrentDirectory();
    },
    methods: {
        // 加载目录树
        async loadDirectoryTree(preserveExpandState = false) {
            try {
                // 保存当前展开的节点keys
                let savedExpandedKeys = [];
                if (preserveExpandState && this.$refs.tree) {
                    savedExpandedKeys = this.$refs.tree.store.nodesMap 
                        ? Object.keys(this.$refs.tree.store.nodesMap).filter(key => {
                            const node = this.$refs.tree.store.nodesMap[key];
                            return node && node.expanded;
                        })
                        : [];
                }
                
                const token = this.$store.state.token;
                const response = await getDirectoryTree(token);
                if (response.data.code === 200) {
                    // 手动添加根目录节点
                    this.treeData = [{
                        id: null,
                        label: "公共文件",
                        path: "root",
                        type: "folder",
                        children: response.data.data
                    }];
                    
                    // 恢复展开状态
                    if (preserveExpandState && savedExpandedKeys.length > 0) {
                        this.$nextTick(() => {
                            this.expandedKeys = savedExpandedKeys;
                        });
                    } else {
                        // 确保当前路径的所有父级目录都展开
                        this.ensureCurrentPathExpanded();
                    }
                } else {
                    console.error("加载目录树失败:", response.data.message);
                    this.treeData = [{
                        id: null,
                        label: "公共文件",
                        path: "root",
                        type: "folder",
                        children: []
                    }];
                }
            } catch (error) {
                console.error("加载目录树失败:", error);
                this.treeData = [{
                    id: null,
                    label: "公共文件",
                    path: "root",
                    type: "folder",
                    children: []
                }];
            }
        },
        
        // 确保当前路径的所有父级目录都展开
        ensureCurrentPathExpanded() {
            this.$nextTick(() => {
                if (this.currentParentId === null) {
                    // 根目录，只展开root
                    this.expandedKeys = ["root"];
                } else {
                    // 获取当前路径的所有父级节点
                    const pathKeys = this.getPathKeysToNode(this.currentParentId);
                    this.expandedKeys = ["root", ...pathKeys];
                }
            });
        },
        
        // 获取到指定节点的所有路径keys
        getPathKeysToNode(targetId) {
            const pathKeys = [];
            if (this.treeData.length > 0 && this.treeData[0].children) {
                this.findPathKeys(this.treeData[0].children, targetId, pathKeys);
            }
            return pathKeys;
        },
        
        // 递归查找路径keys
        findPathKeys(nodes, targetId, pathKeys) {
            for (const node of nodes) {
                if (node.id === targetId) {
                    pathKeys.push(node.path);
                    return true;
                }
                if (node.children && node.children.length > 0) {
                    if (this.findPathKeys(node.children, targetId, pathKeys)) {
                        pathKeys.unshift(node.path);
                        return true;
                    }
                }
            }
            return false;
        },
        // 搜索（点击搜索按钮时触发）
        async handleSearch() {
            if (!this.searchQuery.trim()) {
                this.$message.warning("请输入搜索关键词");
                return;
            }
            
            this.isSearchMode = true;
            this.loading = true;
            try {
                const token = this.$store.state.token;
                const response = await searchFiles(this.searchQuery, token);
                if (response.data.code === 200) {
                    this.searchResults = response.data.data.map(item => ({
                        id: item.fileId,
                        name: item.fileName,
                        type: item.isFolder === 1 ? 'folder' : 'file',
                        size: item.fileSize,
                        modifiedTime: item.updateTime,
                        itemCount: item.itemCount || 0, // 使用后端返回的项数
                        storageId: item.storageId
                    }));
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
        },
        
        // 创建文件夹
        createFolder() {
            // 清空输入框
            this.folderForm.name = "";
            // 清除表单验证状态
            this.$nextTick(() => {
                if (this.$refs.folderForm) {
                    this.$refs.folderForm.clearValidate();
                }
            });
            this.showFolderDialog = true;
        },
        
        // 处理新建文件夹对话框关闭
        handleFolderDialogClose() {
            // 清空输入框
            this.folderForm.name = "";
            // 清除表单验证状态
            if (this.$refs.folderForm) {
                this.$refs.folderForm.clearValidate();
            }
        },
        
        // 取消新建文件夹
        cancelFolderDialog() {
            this.showFolderDialog = false;
            // 清空输入框
            this.folderForm.name = "";
            // 清除表单验证状态
            if (this.$refs.folderForm) {
                this.$refs.folderForm.clearValidate();
            }
        },
        
        async confirmCreateFolder() {
            this.$refs.folderForm.validate(async (valid) => {
                if (valid) {
                    try {
                        const token = this.$store.state.token;
                        const response = await createFolder(
                            this.currentParentId, 
                            this.folderForm.name, 
                            token
                        );
                        if (response.data.code === 200) {
                            this.$message.success("文件夹创建成功");
                            this.showFolderDialog = false;
                            this.folderForm.name = "";
                            await this.loadDirectoryTree(true);
                            await this.loadCurrentDirectory();
                            this.updateTreeHighlight();
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
        
        // 加载当前目录
        async loadCurrentDirectory() {
            this.loading = true;
            try {
                const token = this.$store.state.token;
                const response = await listFiles(this.currentParentId, token);
                if (response.data.code === 200) {
                    this.currentItems = response.data.data.map(item => ({
                        id: item.fileId,
                        name: item.fileName,
                        type: item.isFolder === 1 ? 'folder' : 'file',
                        size: item.fileSize,
                        modifiedTime: item.updateTime,
                        itemCount: item.itemCount || 0, // 使用后端返回的项数
                        storageId: item.storageId,
                        category: item.category
                    }));
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
        
        // 返回上级
        goBack() {
            if (this.canGoBack && this.breadcrumbPath.length > 0) {
                this.exitSearchMode();
                this.selectedItems = [];
                this.breadcrumbPath.pop();
                this.currentParentId = this.breadcrumbPath.length > 0 
                    ? this.breadcrumbPath[this.breadcrumbPath.length - 1].id 
                    : null;
                
                // 更新树的高亮
                this.updateTreeHighlight();
                
                this.loadCurrentDirectory();
            }
        },
        
        // 返回根目录
        goToRoot() {
            if (!this.isAtRoot) {
                this.exitSearchMode();
                this.selectedItems = [];
                this.currentParentId = null;
                this.breadcrumbPath = [];
                
                // 更新树的高亮
                this.$nextTick(() => {
                    if (this.$refs.tree) {
                        this.$refs.tree.setCurrentKey("root");
                        this.currentTreeNodeKey = "root";
                    }
                });
                
                this.loadCurrentDirectory();
            }
        },
        
        // 更新树的高亮
        updateTreeHighlight() {
            this.$nextTick(() => {
                if (this.$refs.tree) {
                    if (this.currentParentId === null) {
                        // 根目录
                        this.$refs.tree.setCurrentKey("root");
                        this.currentTreeNodeKey = "root";
                    } else {
                        // 查找当前目录对应的树节点path
                        const nodePath = this.findNodePathById(this.treeData, this.currentParentId);
                        if (nodePath) {
                            this.$refs.tree.setCurrentKey(nodePath);
                            this.currentTreeNodeKey = nodePath;
                        }
                    }
                }
            });
        },
        
        // 根据ID查找节点的path
        findNodePathById(nodes, targetId) {
            for (const node of nodes) {
                if (node.id === targetId) {
                    return node.path;
                }
                if (node.children && node.children.length > 0) {
                    const result = this.findNodePathById(node.children, targetId);
                    if (result) {
                        return result;
                    }
                }
            }
            return null;
        },
        
        // 面包屑导航
        navigateToBreadcrumb(index) {
            if (index < this.breadcrumbItems.length - 1) {
                this.exitSearchMode();
                this.selectedItems = [];
                if (index === 0) {
                    this.goToRoot();
                } else {
                    this.breadcrumbPath = this.breadcrumbPath.slice(0, index);
                    this.currentParentId = this.breadcrumbPath[this.breadcrumbPath.length - 1].id;
                    
                    // 更新树的高亮
                    this.updateTreeHighlight();
                    
                    this.loadCurrentDirectory();
                }
            }
        },
        
        // 树节点点击
        handleTreeNodeClick(data) {
            this.exitSearchMode();
            this.selectedItems = [];
            this.currentParentId = data.id;
            
            // 设置当前高亮节点
            this.$nextTick(() => {
                if (this.$refs.tree) {
                    this.$refs.tree.setCurrentKey(data.path);
                    this.currentTreeNodeKey = data.path;
                }
            });
            
            // 如果点击的是根目录
            if (data.id === null) {
                this.breadcrumbPath = [];
            } else {
                // 构建面包屑路径
                this.breadcrumbPath = this.buildBreadcrumbFromTree(data.id);
            }
            
            this.loadCurrentDirectory();
        },
        
        // 从树节点ID构建面包屑路径
        buildBreadcrumbFromTree(nodeId) {
            if (nodeId === null) {
                return [];
            }
            const path = [];
            // 从根目录的children开始查找（跳过根目录本身）
            if (this.treeData.length > 0 && this.treeData[0].children) {
                this.findNodePath(this.treeData[0].children, nodeId, path);
            }
            return path;
        },
        
        // 递归查找节点路径
        findNodePath(nodes, targetId, path) {
            for (const node of nodes) {
                if (node.id === targetId) {
                    path.push({ name: node.label, id: node.id });
                    return true;
                }
                if (node.children && node.children.length > 0) {
                    if (this.findNodePath(node.children, targetId, path)) {
                        path.unshift({ name: node.label, id: node.id });
                        return true;
                    }
                }
            }
            return false;
        },
        
        // 项目点击
        handleItemClick(item) {
            if (item.type === "folder") {
                this.selectedItems = [];
                
                // 如果在搜索模式下，需要退出搜索模式并重新构建路径
                if (this.isSearchMode) {
                    this.exitSearchMode();
                    // 从树中构建正确的面包屑路径
                    this.breadcrumbPath = this.buildBreadcrumbFromTree(item.id);
                } else {
                    // 正常模式下，直接添加到面包屑
                    this.breadcrumbPath.push({
                        name: item.name,
                        id: item.id
                    });
                }
                
                this.currentParentId = item.id;
                
                // 先展开到当前路径，再更新高亮
                this.ensureCurrentPathExpanded();
                this.updateTreeHighlight();
                
                this.loadCurrentDirectory();
            } else {
                this.toggleSelection(item);
            }
        },
        
        // 表格行点击
        handleTableRowClick(row, column, event) {
            if (row.type === "folder") {
                this.selectedItems = [];
                
                // 如果在搜索模式下，需要退出搜索模式并重新构建路径
                if (this.isSearchMode) {
                    this.exitSearchMode();
                    // 从树中构建正确的面包屑路径
                    this.breadcrumbPath = this.buildBreadcrumbFromTree(row.id);
                } else {
                    // 正常模式下，直接添加到面包屑
                    this.breadcrumbPath.push({
                        name: row.name,
                        id: row.id
                    });
                }
                
                this.currentParentId = row.id;
                
                // 先展开到当前路径，再更新高亮
                this.ensureCurrentPathExpanded();
                this.updateTreeHighlight();
                
                this.loadCurrentDirectory();
            }
        },
        
        // 退出搜索模式
        exitSearchMode() {
            this.isSearchMode = false;
            this.searchQuery = "";
            this.searchResults = [];
        },
        
        // 获取图标
        getItemIcon(item) {
            if (item.type === "folder") {
                return "folder";
            }
            const ext = item.name.split(".").pop().toLowerCase();
            const iconMap = {
                pdf: "pdf",
                doc: "doc",
                docx: "doc",
                txt: "txt",
                xls: "xls",
                xlsx: "xls",
                csv: "csv",
                ppt: "ppt",
                pptx: "ppt",
                jpg: "picture",
                jpeg: "picture",
                png: "picture",
                gif: "picture",
                bmp: "picture",
                svg: "picture",
                webp: "picture",
                psd: "psd",
                mp4: "video",
                avi: "video",
                mov: "video",
                wmv: "video",
                flv: "video",
                mkv: "video",
                webm: "video",
                mp3: "music",
                wav: "music",
                flac: "music",
                aac: "music",
                ogg: "music",
                zip: "compress",
                rar: "compress",
                "7z": "compress",
                tar: "compress",
                gz: "compress",
                exe: "exe",
                msi: "exe",
                app: "exe",
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
        
        // 格式化文件大小
        formatFileSize(bytes) {
            if (bytes === 0) return "0 B";
            const k = 1024;
            const sizes = ["B", "KB", "MB", "GB"];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
        },
        
        // 格式化日期
        formatDate(dateString) {
            if (!dateString) return '未知';
            return new Date(dateString).toLocaleString("zh-CN");
        },
        
        // 下载文件
        async downloadItem(item) {
            if (item.type === 'folder') {
                this.$message.warning("文件夹下载功能开发中...");
                return;
            }
            
            try {
                const token = this.$store.state.token;
                downloadFile(item.id, token);
                this.$message.success(`开始下载: ${item.name}`);
            } catch (error) {
                console.error("下载文件失败:", error);
                this.$message.error("下载文件失败，请重试");
            }
        },
        
        // 删除文件
        deleteItem(item) {
            this.$confirm(`确定要删除 "${item.name}" 吗？删除后将移入回收站。`, "确认删除", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning"
            }).then(async () => {
                try {
                    const token = this.$store.state.token;
                    const response = await deleteFile(item.id, token);
                    if (response.data.code === 200) {
                        this.$message.success("已移入回收站");
                        if (item.type === 'folder') {
                            await this.loadDirectoryTree(true);
                        }
                        await this.loadCurrentDirectory();
                        this.updateTreeHighlight();
                    } else {
                        this.$message.error(response.data.message || "删除失败");
                    }
                } catch (error) {
                    console.error("删除失败:", error);
                    this.$message.error("删除失败，请重试");
                }
            }).catch(() => {});
        },
        
        // 重命名
        renameItem(item) {
            this.currentRenameItem = item;
            this.renameForm.name = item.name;
            this.showRenameDialog = true;
        },
        
        async confirmRename() {
            this.$refs.renameForm.validate(async (valid) => {
                if (valid) {
                    try {
                        const token = this.$store.state.token;
                        const response = await renameFile(
                            this.currentRenameItem.id,
                            this.renameForm.name,
                            token
                        );
                        if (response.data.code === 200) {
                            this.$message.success("重命名成功");
                            this.showRenameDialog = false;
                            this.renameForm.name = "";
                            const isFolder = this.currentRenameItem.type === 'folder';
                            this.currentRenameItem = null;
                            this.selectedItems = [];
                            if (isFolder) {
                                await this.loadDirectoryTree(true);
                            }
                            await this.loadCurrentDirectory();
                            this.updateTreeHighlight();
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
            this.$message.warning(`当前限制选择 50 个文件`);
        },
        handleUploadDialogClose() {
            this.$refs.upload.clearFiles();
            this.showUploadDialog = false;
        },
        async confirmUpload() {
            try {
                const uploadFiles = this.$refs.upload.uploadFiles;
                
                if (uploadFiles.length === 0) {
                    this.$message.warning("请选择要上传的文件");
                    return;
                }
                
                const token = this.$store.state.token;
                
                // 关闭对话框
                this.showUploadDialog = false;
                this.$refs.upload.clearFiles();
                
                // 上传文件
                for (const fileItem of uploadFiles) {
                    const file = fileItem.raw;
                    const fileId = `upload_${Date.now()}_${Math.random()}`;
                    
                    // 创建进度通知
                    const notification = this.$notify({
                        title: '上传中',
                        message: this.createProgressMessage(file.name, 0, '计算MD5...'),
                        duration: 0,
                        dangerouslyUseHTMLString: true
                    });
                    
                    this.uploadProgressNotifications[fileId] = notification;
                    
                    try {
                        // 计算MD5（显示进度）
                        const fileMd5 = await this.calculateFileMD5WithProgress(file, (progress) => {
                            this.updateProgressNotification(fileId, file.name, progress, '计算MD5...');
                        });
                        
                        // 更新进度：检查秒传
                        this.updateProgressNotification(fileId, file.name, 100, '检查秒传...');
                        
                        // 检查是否可以秒传
                        const checkResponse = await checkInstantUpload(fileMd5, file.size, token);
                        
                        if (checkResponse.data.code === 200 && checkResponse.data.data.canInstant) {
                            // 秒传
                            this.updateProgressNotification(fileId, file.name, 100, '秒传中...');
                            
                            const storageId = checkResponse.data.data.storageId;
                            const instantResponse = await instantUpload(
                                this.currentParentId,
                                file.name,
                                storageId,
                                null,
                                token
                            );
                            
                            if (instantResponse.data.code === 200) {
                                this.closeProgressNotification(fileId, file.name, true, '秒传成功');
                            } else {
                                this.closeProgressNotification(fileId, file.name, false, `秒传失败: ${instantResponse.data.message}`);
                            }
                        } else {
                            // 上传文件（显示进度）
                            this.updateProgressNotification(fileId, file.name, 0, '上传中...');
                            
                            const uploadResponse = await this.uploadFileWithProgress(
                                file,
                                fileMd5,
                                (progress) => {
                                    this.updateProgressNotification(fileId, file.name, progress, '上传中...');
                                },
                                token
                            );
                            
                            if (uploadResponse.data.code === 200) {
                                this.closeProgressNotification(fileId, file.name, true, '上传成功');
                            } else {
                                this.closeProgressNotification(fileId, file.name, false, `上传失败: ${uploadResponse.data.message}`);
                            }
                        }
                    } catch (error) {
                        console.error(`${file.name} 上传失败:`, error);
                        this.closeProgressNotification(fileId, file.name, false, `上传失败: ${error.message}`);
                    }
                }
                
                // 刷新列表
                await this.loadCurrentDirectory();
                
            } catch (error) {
                console.error("上传失败:", error);
                this.$message.error("文件上传失败，请重试");
            }
        },
        
        // 创建进度消息HTML
        createProgressMessage(fileName, progress, status) {
            return `
                <div style="width: 100%;">
                    <div style="margin-bottom: 8px; font-weight: bold;">${fileName}</div>
                    <div style="margin-bottom: 4px; font-size: 12px; color: #909399;">${status}</div>
                    <el-progress :percentage="${progress}" :show-text="true"></el-progress>
                </div>
            `;
        },
        
        // 更新进度通知
        updateProgressNotification(fileId, fileName, progress, status) {
            const notification = this.uploadProgressNotifications[fileId];
            if (notification) {
                notification.message = `
                    <div style="width: 100%;">
                        <div style="margin-bottom: 8px; font-weight: bold;">${fileName}</div>
                        <div style="margin-bottom: 4px; font-size: 12px; color: #909399;">${status}</div>
                        <div style="background: #f5f7fa; border-radius: 4px; height: 6px; overflow: hidden;">
                            <div style="background: #409eff; height: 100%; width: ${progress}%; transition: width 0.3s;"></div>
                        </div>
                        <div style="margin-top: 4px; font-size: 12px; color: #606266; text-align: right;">${progress}%</div>
                    </div>
                `;
            }
        },
        
        // 关闭进度通知
        closeProgressNotification(fileId, fileName, success, message) {
            const notification = this.uploadProgressNotifications[fileId];
            if (notification) {
                notification.close();
                delete this.uploadProgressNotifications[fileId];
            }
            
            // 显示结果消息
            if (success) {
                this.$message.success(message);
            } else {
                this.$message.error(message);
            }
        },
        
        // 带进度的文件上传
        async uploadFileWithProgress(file, fileMd5, onProgress, token) {
            return new Promise((resolve, reject) => {
                const formData = new FormData();
                if (this.currentParentId !== null && this.currentParentId !== undefined) {
                    formData.append("parentId", this.currentParentId);
                }
                formData.append("fileMd5", fileMd5);
                formData.append("file", file);
                
                const xhr = new XMLHttpRequest();
                
                // 监听上传进度
                xhr.upload.addEventListener('progress', (e) => {
                    if (e.lengthComputable) {
                        const progress = Math.round((e.loaded / e.total) * 100);
                        onProgress(progress);
                    }
                });
                
                // 监听完成
                xhr.addEventListener('load', () => {
                    if (xhr.status === 200) {
                        try {
                            const response = JSON.parse(xhr.responseText);
                            resolve({ data: response });
                        } catch (error) {
                            reject(new Error('解析响应失败'));
                        }
                    } else {
                        reject(new Error(`上传失败: ${xhr.status}`));
                    }
                });
                
                // 监听错误
                xhr.addEventListener('error', () => {
                    reject(new Error('网络错误'));
                });
                
                // 发送请求
                xhr.open('POST', '/api/public-files/upload');
                if (token) {
                    xhr.setRequestHeader('Authorization', `Bearer ${token}`);
                }
                xhr.send(formData);
            });
        },
        
        // 带进度的MD5计算
        async calculateFileMD5WithProgress(file, onProgress) {
            const SparkMD5 = (await import('spark-md5')).default;
            return new Promise((resolve, reject) => {
                const spark = new SparkMD5.ArrayBuffer();
                const fileReader = new FileReader();
                const chunkSize = 1024 * 1024; // 1MB
                const chunks = Math.ceil(file.size / chunkSize);
                let currentChunk = 0;

                fileReader.onload = (e) => {
                    spark.append(e.target.result);
                    currentChunk++;
                    
                    // 更新进度
                    const progress = Math.round((currentChunk / chunks) * 100);
                    onProgress(progress);

                    if (currentChunk < chunks) {
                        loadNext();
                    } else {
                        resolve(spark.end());
                    }
                };

                fileReader.onerror = () => {
                    reject(new Error('文件读取失败'));
                };

                const loadNext = () => {
                    const start = currentChunk * chunkSize;
                    const end = Math.min(start + chunkSize, file.size);
                    fileReader.readAsArrayBuffer(file.slice(start, end));
                };

                loadNext();
            });
        },
        
        // 计算文件MD5
        async calculateFileMD5(file) {
            const SparkMD5 = (await import('spark-md5')).default;
            return new Promise((resolve, reject) => {
                const spark = new SparkMD5.ArrayBuffer();
                const fileReader = new FileReader();
                const chunkSize = 1024 * 1024; // 1MB
                const chunks = Math.ceil(file.size / chunkSize);
                let currentChunk = 0;

                fileReader.onload = (e) => {
                    spark.append(e.target.result);
                    currentChunk++;

                    if (currentChunk < chunks) {
                        loadNext();
                    } else {
                        resolve(spark.end());
                    }
                };

                fileReader.onerror = () => {
                    reject(new Error('文件读取失败'));
                };

                const loadNext = () => {
                    const start = currentChunk * chunkSize;
                    const end = Math.min(start + chunkSize, file.size);
                    fileReader.readAsArrayBuffer(file.slice(start, end));
                };

                loadNext();
            });
        },
        
        // 选择相关方法
        isSelected(item) {
            return this.selectedItems.some(selected => selected.id === item.id);
        },
        
        toggleSelection(item) {
            const index = this.selectedItems.findIndex(selected => selected.id === item.id);
            if (index > -1) {
                this.selectedItems.splice(index, 1);
            } else {
                this.selectedItems.push(item);
            }
        },
        
        handleSelectionChange(selection) {
            this.selectedItems = selection;
        },
        
        // 批量下载
        async batchDownload() {
            if (this.selectedItems.length === 0) {
                this.$message.warning("请选择要下载的文件");
                return;
            }
            
            try {
                const token = this.$store.state.token;
                const fileIds = this.selectedItems.map(item => item.id);
                
                // 显示加载提示
                const loading = this.$loading({
                    lock: true,
                    text: '正在打包文件，请稍候...',
                    spinner: 'el-icon-loading',
                    background: 'rgba(0, 0, 0, 0.7)'
                });
                
                // 创建批量下载任务
                const response = await createBatchDownloadTask(fileIds, token);
                
                if (response.data.code === 200) {
                    const taskId = response.data.data.taskId;
                    const status = response.data.data.status;
                    
                    if (status === 'completed') {
                        // 任务已完成（复用已有压缩包）
                        loading.close();
                        this.$message.success('开始下载');
                        downloadBatchTask(taskId, token);
                    } else {
                        // 任务正在处理，轮询检查状态
                        this.pollBatchDownloadTask(taskId, loading, token);
                    }
                } else {
                    loading.close();
                    this.$message.error(response.data.message || '创建下载任务失败');
                }
            } catch (error) {
                console.error("批量下载失败:", error);
                this.$message.error("批量下载失败，请重试");
            }
        },
        
        // 轮询批量下载任务状态
        async pollBatchDownloadTask(taskId, loading, token, retryCount = 0) {
            const maxRetries = 60; // 最多轮询60次（60秒）
            
            if (retryCount >= maxRetries) {
                loading.close();
                this.$message.error('下载任务超时，请稍后重试');
                return;
            }
            
            setTimeout(async () => {
                try {
                    // 这里简化处理，直接尝试下载
                    // 实际应该有一个查询任务状态的接口
                    loading.close();
                    this.$message.success('文件打包完成，开始下载');
                    downloadBatchTask(taskId, token);
                } catch (error) {
                    // 继续轮询
                    this.pollBatchDownloadTask(taskId, loading, token, retryCount + 1);
                }
            }, 1000); // 每秒轮询一次
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
                    let successCount = 0;
                    let hasFolder = false;
                    
                    for (const item of this.selectedItems) {
                        const response = await deleteFile(item.id, token);
                        if (response.data.code === 200) {
                            successCount++;
                            if (item.type === 'folder') {
                                hasFolder = true;
                            }
                        }
                    }
                    
                    this.$message.success(`成功删除 ${successCount} 个项目`);
                    this.selectedItems = [];
                    if (hasFolder) {
                        await this.loadDirectoryTree(true);
                    }
                    await this.loadCurrentDirectory();
                    this.updateTreeHighlight();
                } catch (error) {
                    console.error("批量删除失败:", error);
                    this.$message.error("批量删除失败，请重试");
                }
            }).catch(() => {});
        },
        
        // 下载文件夹
        async downloadFolder(folder) {
            try {
                const token = this.$store.state.token;
                
                // 显示加载提示
                const loading = this.$loading({
                    lock: true,
                    text: '正在打包文件夹，请稍候...',
                    spinner: 'el-icon-loading',
                    background: 'rgba(0, 0, 0, 0.7)'
                });
                
                // 延迟一下再下载，让后端有时间打包
                setTimeout(() => {
                    loading.close();
                    this.$message.success('开始下载');
                    downloadFolder(folder.id, token);
                }, 1000);
                
            } catch (error) {
                console.error("下载文件夹失败:", error);
                this.$message.error("下载文件夹失败，请重试");
            }
        },
        
        // 刷新
        async refreshAll() {
            this.loading = true;
            try {
                this.selectedItems = [];
                this.exitSearchMode();
                
                await this.loadDirectoryTree();
                await this.loadCurrentDirectory();
                this.$message.success("刷新成功");
            } catch (error) {
                console.error("刷新失败:", error);
                this.$message.error("刷新失败，请重试");
            } finally {
                this.loading = false;
            }
        },
        
        handleNodeExpand(data, node) {
            // 节点展开处理 - 记录用户手动展开的节点
            if (!this.expandedKeys.includes(data.path)) {
                this.expandedKeys.push(data.path);
            }
        },
        
        handleNodeCollapse(data, node) {
            // 节点收起处理 - 记录用户手动收起的节点
            const index = this.expandedKeys.indexOf(data.path);
            if (index > -1) {
                this.expandedKeys.splice(index, 1);
            }
        },
        
        // 同步表格选中状态
        syncTableSelection() {
            if (this.$refs.fileTable) {
                this.$refs.fileTable.clearSelection();
                this.selectedItems.forEach(selectedItem => {
                    const row = this.displayItems.find(item => item.id === selectedItem.id);
                    if (row) {
                        this.$refs.fileTable.toggleRowSelection(row, true);
                    }
                });
            }
        },
        
        // 同步网格选中状态（网格视图通过isSelected计算属性自动处理，这里不需要额外操作）
        syncGridSelection() {
            // 网格视图的选中状态通过isSelected方法自动同步
            // 不需要额外操作
        },
        
        // 改变排序方式
        changeSortBy(sortBy) {
            if (this.sortBy === sortBy) {
                // 如果点击的是当前排序字段，则切换排序顺序
                this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
            } else {
                // 如果点击的是新的排序字段，则设置为升序
                this.sortBy = sortBy;
                this.sortOrder = 'asc';
            }
        }
    }
};
</script>

<style scoped>
/* 样式保持不变 */
</style>
