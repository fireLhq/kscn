<template>
    <div class="user-management">
        <!-- 工具栏 -->
        <div class="toolbar">
            <div class="toolbar-left">
                <el-button type="primary" @click="showAddDialog">
                    <i class="el-icon-plus"></i>
                    添加用户
                </el-button>
                <el-button @click="refreshUserList">
                    <i class="el-icon-refresh"></i>
                    刷新
                </el-button>
            </div>
            <div class="toolbar-right">
                <el-form
                    ref="searchForm"
                    :model="searchForm"
                    inline
                    class="search-form"
                >
                    <el-form-item>
                        <el-input v-model="searchForm.username" placeholder="用户名" style="width: 120px;" />
                    </el-form-item>
                    <el-form-item>
                        <el-input v-model="searchForm.email" placeholder="邮箱" style="width: 150px;" />
                    </el-form-item>
                    <el-form-item>
                        <el-input v-model="searchForm.nickname" placeholder="昵称" style="width: 120px;" />
                    </el-form-item>
                    <el-form-item>
                        <el-select v-model="searchForm.role" placeholder="角色" clearable style="width: 100px;">
                            <el-option label="管理员" :value="0" />
                            <el-option label="普通用户" :value="1" />
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-select v-model="searchForm.sex" placeholder="性别" clearable style="width: 100px;">
                            <el-option label="男" :value="0" />
                            <el-option label="女" :value="1" />
                            <el-option label="不愿透露" :value="2" />
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 100px;">
                            <el-option label="启用" :value="1" />
                            <el-option label="未启用" :value="0" />
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="handleAdvancedSearch">搜索</el-button>
                        <el-button @click="resetSearchForm">重置</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>

        <!-- 用户列表 -->
        <div class="user-table">
            <el-table
                :data="filteredUserList"
                v-loading="loading"
                stripe
                style="width: 100%"
            >
                <el-table-column prop="userId" label="ID" width="80" align="center" />
                <el-table-column label="头像" width="80" align="center">
                    <template slot-scope="scope">
                        <div class="user-avatar">
                            <img :src="getUserAvatarUrl(scope.row)" :alt="scope.row.nickname || scope.row.username" />
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="username" label="用户名" width="120" align="center" show-overflow-tooltip />
                <el-table-column prop="email" label="邮箱" width="200" align="center" show-overflow-tooltip />
                <el-table-column prop="nickname" label="昵称" width="120" align="center" show-overflow-tooltip />
                <el-table-column prop="role" label="角色" width="100" align="center">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.role === 0 ? 'danger' : 'success'" size="small">
                            {{ scope.row.role === 0 ? '管理员' : '普通用户' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="sex" label="性别" width="100" align="center">
                    <template slot-scope="scope">
                        <span v-if="scope.row.sex === 0">男</span>
                        <span v-else-if="scope.row.sex === 1">女</span>
                        <span v-else-if="scope.row.sex === 2">不愿透露</span>
                        <span v-else>-</span>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" align="center">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                            {{ scope.row.status === 1 ? '启用' : '未启用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" align="center" show-overflow-tooltip>
                    <template slot-scope="scope">
                        {{ formatDateTime(scope.row.createTime) }}
                    </template>
                </el-table-column>


                <el-table-column label="操作" width="200" fixed="right">
                    <template slot-scope="scope">
                        <el-button size="mini" @click="showEditDialog(scope.row)">编辑</el-button>
                        <el-button 
                            size="mini" 
                            type="danger" 
                            @click="handleDelete(scope.row)"
                            :disabled="scope.row.role === 0"
                        >
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 分页 -->
        <div class="pagination">
            <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="currentPage"
                :page-sizes="[10, 20, 50, 100]"
                :page-size="pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="totalUsers"
            />
        </div>



        <!-- 添加/编辑用户对话框 -->
        <el-dialog
            :title="dialogTitle"
            :visible.sync="dialogVisible"
            width="500px"
            @close="resetForm"
        >
            <el-form
                ref="userForm"
                :model="userForm"
                :rules="userRules"
                label-width="80px"
            >
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="userForm.username" placeholder="请输入用户名" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="userForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="密码" prop="password" v-if="!isEdit">
                    <el-input 
                        v-model="userForm.password" 
                        type="password" 
                        placeholder="请输入密码" 
                        show-password 
                    />
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="角色" prop="role">
                    <el-select 
                        v-model="userForm.role" 
                        placeholder="请选择角色" 
                        style="width: 100%"
                        :disabled="isEditingCurrentUser"
                    >
                        <el-option label="普通用户" :value="1" />
                        <el-option label="管理员" :value="0" />
                    </el-select>
                    <div v-if="isEditingCurrentUser" class="form-tip">
                        <i class="el-icon-info"></i>
                        不能修改自己的角色
                    </div>
                </el-form-item>
                <el-form-item label="性别" prop="sex">
                    <el-select v-model="userForm.sex" placeholder="请选择性别" style="width: 100%">
                        <el-option label="男" :value="0" />
                        <el-option label="女" :value="1" />
                        <el-option label="不愿透露" :value="2" />
                    </el-select>
                </el-form-item>
                <el-form-item label="生日" prop="birthday">
                    <el-date-picker
                        v-model="userForm.birthday"
                        type="date"
                        placeholder="请选择生日"
                        format="yyyy-MM-dd"
                        value-format="yyyy-MM-dd"
                        style="width: 100%"
                    />
                </el-form-item>
                <el-form-item label="说明" prop="synopsis">
                    <el-input 
                        v-model="userForm.synopsis" 
                        type="textarea" 
                        placeholder="请输入说明"
                        :rows="3"
                    />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-select 
                        v-model="userForm.status" 
                        placeholder="请选择状态" 
                        style="width: 100%"
                        :disabled="isEditingCurrentUser"
                    >
                        <el-option label="启用" :value="1" />
                        <el-option label="未启用" :value="0" />
                    </el-select>
                    <div v-if="isEditingCurrentUser" class="form-tip">
                        <i class="el-icon-info"></i>
                        不能修改自己的状态
                    </div>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
                    {{ isEdit ? '更新' : '添加' }}
                </el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import { getUserList, addUser, updateUser, deleteUser } from "@/api/system/admin";
import { getUserInfo } from "@/api/system/user";
import { getUserAvatarUrl } from "@/utils/avatarUtils";

export default {
    name: "UserManagement",
    data() {
        return {
            loading: false,
            submitLoading: false,
            userList: [],
            filteredUserList: [],
            searchKeyword: '',
            currentPage: 1,
            pageSize: 10,
            totalUsers: 0,
            dialogVisible: false,
            isEdit: false,
            userForm: {
                userId: null,
                username: '',
                email: '',
                password: '',
                nickname: '',
                role: 1,
                sex: null,
                birthday: '',
                synopsis: '',
                status: 1
            },
            searchForm: {
                username: '',
                email: '',
                nickname: '',
                role: null,
                sex: null,
                status: null
            },
            userRules: {
                username: [
                    { required: true, message: '请输入用户名', trigger: 'blur' },
                    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
                ],
                email: [
                    { required: true, message: '请输入邮箱', trigger: 'blur' },
                    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
                ],
                password: [
                    { required: true, message: '请输入密码', trigger: 'blur' },
                    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
                ],
                role: [
                    { required: true, message: '请选择角色', trigger: 'change' }
                ],
                status: [
                    { required: true, message: '请选择状态', trigger: 'change' }
                ]
            }
        };
    },
    computed: {
        dialogTitle() {
            return this.isEdit ? '编辑用户' : '添加用户';
        },
        isEditingCurrentUser() {
            // 检查是否正在编辑当前登录的管理员
            if (!this.isEdit || !this.userForm.userId) {
                return false;
            }
            
            // 获取当前登录用户的信息
            const currentUser = this.$store.state.currentUser;
            if (!currentUser) {
                return false;
            }
            
            // 比较用户ID
            return this.userForm.userId === currentUser.userId;
        }
    },
    async mounted() {
        await this.loadCurrentUser();
        this.loadUserList();
    },
    methods: {
        async loadCurrentUser() {
            try {
                const token = this.$store.state.token;
                if (!token) return;
                
                const response = await getUserInfo(token);
                if (response.data.code === 200) {
                    this.$store.commit('setCurrentUser', response.data.data);
                }
            } catch (error) {
                console.error('获取当前用户信息失败:', error);
            }
        },
        async loadUserList() {
            this.loading = true;
            try {
                const token = this.$store.state.token;
                
                // 构建查询参数
                const params = {
                    page: this.currentPage,
                    size: this.pageSize
                };
                
                // 添加搜索条件
                if (this.searchForm.username) params.username = this.searchForm.username;
                if (this.searchForm.email) params.email = this.searchForm.email;
                if (this.searchForm.nickname) params.nickname = this.searchForm.nickname;
                if (this.searchForm.role !== null && this.searchForm.role !== '') params.role = this.searchForm.role;
                if (this.searchForm.sex !== null && this.searchForm.sex !== '') params.sex = this.searchForm.sex;
                if (this.searchForm.status !== null && this.searchForm.status !== '') params.status = this.searchForm.status;
                

                
                const response = await getUserList(token, params);
                if (response.data.code === 200) {
                    this.userList = response.data.data.records || response.data.data || [];
                    this.totalUsers = response.data.data.total || this.userList.length;
                    this.filteredUserList = [...this.userList];
                } else {
                    this.$message.error(response.data.message || '获取用户列表失败');
                }
            } catch (error) {
                console.error('获取用户列表失败:', error);
                
                // 检查是否是403权限错误
                if (error.response && error.response.status === 403) {
                    this.$message.error('您没有权限访问用户管理功能');
                    // 可以选择重定向到首页或上一页
                    this.$router.go(-1);
                } else {
                    this.$message.error('获取用户列表失败，请重试');
                }
            } finally {
                this.loading = false;
            }
        },
        
        refreshUserList() {
            this.loadUserList();
        },
        showAddDialog() {
            this.isEdit = false;
            this.dialogVisible = true;
        },
        handleAdvancedSearch() {
            this.currentPage = 1; // 重置到第一页
            this.loadUserList();
        },
        resetSearchForm() {
            this.searchForm = {
                username: '',
                email: '',
                nickname: '',
                role: null,
                sex: null,
                status: null
            };
            this.currentPage = 1;
            this.loadUserList();
        },
        formatDateTime(dateTime) {
            if (!dateTime) return '-';
            const date = new Date(dateTime);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            const seconds = String(date.getSeconds()).padStart(2, '0');
            return `${year}/${month}/${day} ${hours}:${minutes}:${seconds}`;
        },
        showEditDialog(user) {
            this.isEdit = true;
            this.userForm = {
                userId: user.userId,
                username: user.username,
                email: user.email,
                password: '',
                nickname: user.nickname || '',
                role: user.role,
                sex: user.sex,
                birthday: user.birthday || '',
                synopsis: user.synopsis || '',
                status: user.status
            };
            this.dialogVisible = true;
        },
        async handleSubmit() {
            this.$refs.userForm.validate(async (valid) => {
                if (valid) {
                    this.submitLoading = true;
                    try {
                        const token = this.$store.state.token;
                        const userData = { ...this.userForm };
                        if (this.isEdit) {
                            delete userData.password; // 编辑时不发送密码
                        }
                        
                        const response = this.isEdit 
                            ? await updateUser(token, userData)
                            : await addUser(token, userData);
                            
                        if (response.data.code === 200) {
                            this.$message.success(this.isEdit ? '用户更新成功' : '用户添加成功');
                            this.dialogVisible = false;
                            this.loadUserList();
                        } else {
                            this.$message.error(response.data.message || '操作失败');
                        }
                    } catch (error) {
                        this.$message.error('操作失败，请重试');
                        console.error('操作失败:', error);
                    } finally {
                        this.submitLoading = false;
                    }
                }
            });
        },
        async handleDelete(user) {
            try {
                await this.$confirm(`确定要删除用户 "${user.username}" 吗？`, '确认删除', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                });
                
                // 用户确认删除
                const token = this.$store.state.token;
                const response = await deleteUser(token, user.userId);
                if (response.data.code === 200) {
                    this.$message.success('用户删除成功');
                    this.loadUserList();
                } else {
                    this.$message.error(response.data.message || '删除失败');
                }
            } catch (error) {
                // 用户取消删除或其他错误
                if (error === 'cancel') {
                    // 用户点击了取消，不需要显示错误信息
                    return;
                }
                this.$message.error('删除失败，请重试');
                console.error('删除失败:', error);
            }
        },
        handleSizeChange(val) {
            this.pageSize = val;
            this.loadUserList();
        },
        handleCurrentChange(val) {
            this.currentPage = val;
            this.loadUserList();
        },
        resetForm() {
            this.userForm = {
                userId: null,
                username: '',
                email: '',
                password: '',
                nickname: '',
                role: 1,
                sex: null,
                birthday: '',
                synopsis: '',
                status: 1
            };
            this.$refs.userForm && this.$refs.userForm.resetFields();
        },
        getUserAvatarUrl
    }
};
</script>

<style scoped>
.user-management {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    overflow: hidden;
}

.toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #e9ecef;
    background: #f8f9fa;
}

.toolbar-left {
    display: flex;
    gap: 10px;
}

.toolbar-right {
    display: flex;
    align-items: center;
}

.search-form {
    display: flex;
    align-items: center;
    gap: 10px;
}

.search-form .el-form-item {
    margin-bottom: 0;
    margin-right: 0;
}

.user-table {
    padding: 0;
}

.pagination {
    padding: 20px;
    text-align: right;
    border-top: 1px solid #e9ecef;
    background: #f8f9fa;
}

.dialog-footer {
    text-align: right;
}

/* 表格样式优化 */
::v-deep .el-table th {
    background-color: #f8f9fa;
    color: #495057;
    font-weight: 600;
    text-align: center;
    padding: 12px 8px;
    border-bottom: 1px solid #e9ecef;
}

::v-deep .el-table td {
    padding: 12px 8px;
    text-align: center;
    border-bottom: 1px solid #f0f0f0;
}

::v-deep .el-table--striped .el-table__body tr.el-table__row--striped td {
    background: #f8f9fa;
}

/* 确保表格内容居中对齐 */
::v-deep .el-table .cell {
    text-align: center;
    padding: 0 8px;
}

/* 标签样式优化 */
::v-deep .el-table .el-tag {
    margin: 0;
}

/* 操作按钮样式 */
::v-deep .el-button--mini {
    padding: 5px 10px;
    font-size: 12px;
    margin-right: 5px;
}

/* 标签样式 */
::v-deep .el-tag {
    border: none;
    font-weight: 500;
    font-size: 12px;
}

/* 表单样式 */
::v-deep .el-form-item {
    margin-bottom: 18px;
}

::v-deep .el-form-item__label {
    font-weight: 500;
    color: #333;
}

/* 表单提示样式 */
.form-tip {
    margin-top: 5px;
    font-size: 12px;
    color: #909399;
    display: flex;
    align-items: center;
}

.form-tip i {
    margin-right: 4px;
    color: #409EFF;
}

/* 用户头像样式 */
.user-avatar {
    display: flex;
    justify-content: center;
    align-items: center;
}

.user-avatar img {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid #e9ecef;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .toolbar {
        flex-direction: column;
        gap: 15px;
        align-items: stretch;
    }
    
    .toolbar-left {
        justify-content: center;
    }
    
    .user-table {
        overflow-x: auto;
    }
}
</style>
