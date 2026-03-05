<template>
    <div class="project-member-management">
        <!-- 工具栏 -->
        <div class="toolbar">
            <div class="toolbar-left">
                <el-button type="primary" @click="showAddDialog">
                    <i class="el-icon-plus"></i>
                    添加成员
                </el-button>
                <el-button @click="refreshMemberList">
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
                        <el-input v-model="searchForm.role" placeholder="角色" style="width: 120px;" />
                    </el-form-item>
                    <el-form-item>
                        <el-select v-model="searchForm.type" placeholder="类型" clearable style="width: 100px;">
                            <el-option label="开发人员" :value="0" />
                            <el-option label="管理人员" :value="1" />
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="handleAdvancedSearch">搜索</el-button>
                        <el-button @click="resetSearchForm">重置</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>

        <!-- 成员列表 -->
        <div class="member-table">
            <el-table
                :data="filteredMemberList"
                v-loading="loading"
                stripe
                style="width: 100%"
            >
                <el-table-column prop="memberId" label="ID" width="80" align="center" />
                <el-table-column label="用户信息" width="200" align="center">
                    <template slot-scope="scope">
                        <div class="user-info">
                            <div class="user-avatar">
                                <img :src="getDisplayAvatarUrl(scope.row.user)" :alt="scope.row.user?.nickname" />
                            </div>
                            <div class="user-details">
                                <div class="user-name">{{ scope.row.user?.nickname || scope.row.user?.username || '-' }}</div>
                                <div class="user-email">{{ scope.row.user?.email || '-' }}</div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="role" label="项目角色" width="150" align="center" show-overflow-tooltip />
                <el-table-column prop="type" label="类型" width="100" align="center">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.type === 0 ? 'success' : 'warning'" size="small">
                            {{ scope.row.type === 0 ? '开发人员' : '管理人员' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="技能" width="200" align="center">
                    <template slot-scope="scope">
                        <div class="skills-container">
                            <el-tag 
                                v-for="skill in scope.row.skills" 
                                :key="skill" 
                                size="mini" 
                                class="skill-tag"
                            >
                                {{ skill }}
                            </el-tag>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
                <el-table-column prop="createTime" label="创建时间" width="180" align="center" show-overflow-tooltip>
                    <template slot-scope="scope">
                        {{ formatDateTime(scope.row.createTime) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                    <template slot-scope="scope">
                        <el-button size="mini" @click="showEditDialog(scope.row)">编辑</el-button>
                        <el-button 
                            size="mini" 
                            type="danger" 
                            @click="handleDelete(scope.row)"
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
                :total="totalMembers"
            />
        </div>

        <!-- 添加/编辑成员对话框 -->
        <el-dialog
            :title="dialogTitle"
            :visible.sync="dialogVisible"
            width="600px"
            @close="resetForm"
        >
            <el-form
                ref="memberForm"
                :model="memberForm"
                :rules="memberRules"
                label-width="100px"
            >
                <el-form-item label="关联用户" prop="userId">
                    <el-select 
                        v-model="memberForm.userId" 
                        placeholder="请选择用户" 
                        style="width: 100%"
                        filterable
                        remote
                        :remote-method="searchUsers"
                        :loading="userSearchLoading"
                        @change="handleUserChange"
                    >
                        <el-option
                            v-for="user in userOptions"
                            :key="user.userId"
                            :label="`${user.nickname || user.username} (${user.email})`"
                            :value="user.userId"
                        >
                            <div class="user-option">
                                <img :src="getDisplayAvatarUrl(user)" class="option-avatar" />
                                <span>{{ user.nickname || user.username }}</span>
                                <span class="option-email">({{ user.email }})</span>
                            </div>
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="项目角色" prop="role">
                    <el-input v-model="memberForm.role" placeholder="请输入项目角色" />
                </el-form-item>
                <el-form-item label="类型" prop="type">
                    <el-select v-model="memberForm.type" placeholder="请选择类型" style="width: 100%">
                        <el-option label="开发人员" :value="0" />
                        <el-option label="管理人员" :value="1" />
                    </el-select>
                </el-form-item>
                <el-form-item label="技能" prop="skills">
                    <el-select
                        v-model="memberForm.skills"
                        multiple
                        filterable
                        allow-create
                        placeholder="请选择或输入技能"
                        style="width: 100%"
                    >
                        <el-option
                            v-for="skill in commonSkills"
                            :key="skill"
                            :label="skill"
                            :value="skill"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="描述" prop="description">
                    <el-input 
                        v-model="memberForm.description" 
                        type="textarea" 
                        placeholder="请输入个人描述"
                        :rows="4"
                    />
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
import { getProjectMemberList, addProjectMember, updateProjectMember, deleteProjectMember } from "@/api/system/projectMember";
import { getUserList } from "@/api/system/admin";
import { getUserAvatarUrl } from "@/utils/avatarUtils";

export default {
    name: "ProjectMemberManagement",
    data() {
        return {
            loading: false,
            submitLoading: false,
            userSearchLoading: false,
            memberList: [],
            filteredMemberList: [],
            currentPage: 1,
            pageSize: 10,
            totalMembers: 0,
            dialogVisible: false,
            isEdit: false,
            memberForm: {
                memberId: null,
                userId: null,
                role: '',
                description: '',
                skills: [],
                type: 0
            },
            searchForm: {
                role: '',
                type: null
            },
            userOptions: [],
            commonSkills: [
                'Vue.js', 'React', 'Angular', 'JavaScript', 'TypeScript',
                'Java', 'Spring Boot', 'Python', 'Node.js', 'PHP',
                'MySQL', 'PostgreSQL', 'MongoDB', 'Redis',
                'Docker', 'Kubernetes', 'AWS', '阿里云',
                'HTML5', 'CSS3', 'Sass', 'Less',
                'Git', '项目管理', '团队协作', '产品规划',
                '架构设计', '系统设计', '性能优化', '敏捷开发'
            ],
            memberRules: {
                userId: [
                    { required: true, message: '请选择关联用户', trigger: 'change' }
                ],
                role: [
                    { required: true, message: '请输入项目角色', trigger: 'blur' }
                ],
                type: [
                    { required: true, message: '请选择类型', trigger: 'change' }
                ]
            }
        };
    },
    computed: {
        dialogTitle() {
            return this.isEdit ? '编辑项目成员' : '添加项目成员';
        }
    },
    async mounted() {
        this.loadMemberList();
        this.loadUserOptions();
    },
    methods: {
        async loadMemberList() {
            this.loading = true;
            try {
                const token = this.$store.state.token;
                
                // 构建查询参数
                const params = {
                    page: this.currentPage,
                    size: this.pageSize
                };
                
                // 添加搜索条件
                if (this.searchForm.role) params.role = this.searchForm.role;
                if (this.searchForm.type !== null && this.searchForm.type !== '') params.type = this.searchForm.type;
                
                const response = await getProjectMemberList(token, params);
                if (response.data.code === 200) {
                    this.memberList = response.data.data.records || response.data.data || [];
                    this.totalMembers = response.data.data.total || this.memberList.length;
                    this.filteredMemberList = [...this.memberList];
                } else {
                    this.$message.error(response.data.message || '获取项目成员列表失败');
                }
            } catch (error) {
                console.error('获取项目成员列表失败:', error);
                this.$message.error('获取项目成员列表失败，请重试');
            } finally {
                this.loading = false;
            }
        },
        
        async loadUserOptions() {
            try {
                const token = this.$store.state.token;
                const response = await getUserList(token, { page: 1, size: 1000 });
                if (response.data.code === 200) {
                    this.userOptions = response.data.data.records || response.data.data || [];
                }
            } catch (error) {
                console.error('获取用户列表失败:', error);
            }
        },
        
        async searchUsers(query) {
            if (!query) {
                this.loadUserOptions();
                return;
            }
            
            this.userSearchLoading = true;
            try {
                const token = this.$store.state.token;
                const response = await getUserList(token, { 
                    page: 1, 
                    size: 50,
                    username: query,
                    nickname: query,
                    email: query
                });
                if (response.data.code === 200) {
                    this.userOptions = response.data.data.records || response.data.data || [];
                }
            } catch (error) {
                console.error('搜索用户失败:', error);
            } finally {
                this.userSearchLoading = false;
            }
        },
        
        handleUserChange(userId) {
            const selectedUser = this.userOptions.find(user => user.userId === userId);
            if (selectedUser) {
                // 可以在这里自动填充一些信息
                console.log('选择的用户:', selectedUser);
            }
        },
        
        refreshMemberList() {
            this.loadMemberList();
        },
        
        showAddDialog() {
            this.isEdit = false;
            this.dialogVisible = true;
        },
        
        handleAdvancedSearch() {
            this.currentPage = 1;
            this.loadMemberList();
        },
        
        resetSearchForm() {
            this.searchForm = {
                role: '',
                type: null
            };
            this.currentPage = 1;
            this.loadMemberList();
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
        
        showEditDialog(member) {
            this.isEdit = true;
            this.memberForm = {
                memberId: member.memberId,
                userId: member.userId,
                role: member.role,
                description: member.description || '',
                skills: member.skills || [],
                type: member.type
            };
            this.dialogVisible = true;
        },
        
        async handleSubmit() {
            this.$refs.memberForm.validate(async (valid) => {
                if (valid) {
                    this.submitLoading = true;
                    try {
                        const token = this.$store.state.token;
                        const memberData = { ...this.memberForm };
                        
                        const response = this.isEdit 
                            ? await updateProjectMember(token, memberData)
                            : await addProjectMember(token, memberData);
                            
                        if (response.data.code === 200) {
                            this.$message.success(this.isEdit ? '项目成员更新成功' : '项目成员添加成功');
                            this.dialogVisible = false;
                            this.loadMemberList();
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
        
        async handleDelete(member) {
            try {
                await this.$confirm(`确定要删除项目成员 "${member.user?.nickname || member.user?.username}" 吗？`, '确认删除', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                });
                
                const token = this.$store.state.token;
                const response = await deleteProjectMember(token, member.memberId);
                if (response.data.code === 200) {
                    this.$message.success('项目成员删除成功');
                    this.loadMemberList();
                } else {
                    this.$message.error(response.data.message || '删除失败');
                }
            } catch (error) {
                if (error === 'cancel') {
                    return;
                }
                this.$message.error('删除失败，请重试');
                console.error('删除失败:', error);
            }
        },
        
        handleSizeChange(val) {
            this.pageSize = val;
            this.loadMemberList();
        },
        
        handleCurrentChange(val) {
            this.currentPage = val;
            this.loadMemberList();
        },
        
        resetForm() {
            this.memberForm = {
                memberId: null,
                userId: null,
                role: '',
                description: '',
                skills: [],
                type: 0
            };
            this.$refs.memberForm && this.$refs.memberForm.resetFields();
        },
        getDisplayAvatarUrl(user) {
            const url = getUserAvatarUrl(user);
            // 如果返回 null，使用本地默认头像
            return url || require("@/assets/images/avatar/user.png");
        }
    }
};
</script>

<style scoped>
.project-member-management {
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

.member-table {
    padding: 0;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 10px;
}

.user-avatar img {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
}

.user-details {
    text-align: left;
}

.user-name {
    font-weight: 500;
    color: #333;
    font-size: 14px;
}

.user-email {
    font-size: 12px;
    color: #666;
}

.skills-container {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    justify-content: center;
}

.skill-tag {
    margin: 1px;
}

.user-option {
    display: flex;
    align-items: center;
    gap: 8px;
}

.option-avatar {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    object-fit: cover;
}

.option-email {
    color: #999;
    font-size: 12px;
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
    
    .member-table {
        overflow-x: auto;
    }
}
</style>
