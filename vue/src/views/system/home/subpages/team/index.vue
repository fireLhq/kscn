<template>
    <div class="team-view">
        <div class="team-body">
            <div class="top-bar">
                <el-button type="text" @click="$router.back()"><i class="el-icon-arrow-left"></i> 返回</el-button>
            </div>
            <div class="team-header">
                <h1>核心人员</h1>
                <p class="subtitle">KSCN项目的核心开发团队</p>
            </div>
            
            <div class="team-content">
                <!-- 加载状态 -->
                <div v-if="loading" class="loading-container">
                    <el-loading :loading="true" text="加载团队成员中..."></el-loading>
                </div>
                
                <!-- 开发团队 -->
                <div v-else class="team-section">
                    <h2>开发团队</h2>
                    <div class="team-grid" v-if="developers.length > 0">
                        <div class="team-member" v-for="member in developers" :key="member.id">
                            <div class="member-avatar">
                                <img :src="getDisplayAvatarUrl(member.avatar)" :alt="member.name" />
                            </div>
                            <div class="member-info">
                                <h3 class="member-name">{{ member.name }}</h3>
                                <p class="member-role">{{ member.role }}</p>
                                <p class="member-description">{{ member.description }}</p>
                                <div class="member-skills">
                                    <span class="skill-tag" v-for="skill in member.skills" :key="skill">{{ skill }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div v-else class="no-data">
                        <p>暂无开发团队成员</p>
                    </div>
                </div>
                
                <!-- 管理团队 -->
                <div v-if="!loading" class="team-section">
                    <h2>管理团队</h2>
                    <div class="team-grid" v-if="managers.length > 0">
                        <div class="team-member" v-for="member in managers" :key="member.id">
                            <div class="member-avatar">
                                <img :src="getDisplayAvatarUrl(member.avatar)" :alt="member.name" />
                            </div>
                            <div class="member-info">
                                <h3 class="member-name">{{ member.name }}</h3>
                                <p class="member-role">{{ member.role }}</p>
                                <p class="member-description">{{ member.description }}</p>
                                <div class="member-skills">
                                    <span class="skill-tag" v-for="skill in member.skills" :key="skill">{{ skill }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div v-else class="no-data">
                        <p>暂无管理团队成员</p>
                    </div>
                </div>
                
                <div class="team-section">
                    <h2>加入我们</h2>
                    <div class="join-us">
                        <p>我们正在寻找优秀的开发者和管理者加入团队。如果你对知识共享、开源项目或技术创新充满热情，欢迎联系我们！</p>
                        <div class="contact-info">
                            <p><strong>联系方式：</strong></p>
                            <p>Telegram群：<a href="https://t.me/kscn_top" target="_blank">KSCN</a></p>
                            <p>邮箱：lab_c919@qq.com</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { getProjectMemberList } from "@/api/system/projectMember";
import { getUserAvatarUrl } from "@/utils/avatarUtils";

export default {
    name: "TeamView",
    data() {
        return {
            developers: [],
            managers: [],
            loading: true,
            error: false
        };
    },
    async mounted() {
        await this.loadProjectMembers();
    },
    methods: {
        async loadProjectMembers() {
            this.loading = true;
            this.error = false;
            try {
                // 无论是否登录都可以获取项目成员列表
                const token = this.$store.state.token || null;
                const response = await getProjectMemberList(token, { page: 1, size: 100 });
                if (response.data.code === 200) {
                    const members = response.data.data.records || response.data.data || [];
                    this.processMembers(members);
                } else {
                    console.warn('获取项目成员失败');
                    this.developers = [];
                    this.managers = [];
                }
            } catch (error) {
                console.error('获取项目成员失败:', error);
                this.developers = [];
                this.managers = [];
            } finally {
                this.loading = false;
            }
        },
        
        processMembers(members) {
            this.developers = [];
            this.managers = [];
            
            members.forEach(member => {
                const memberData = {
                    id: member.memberId,
                    name: member.user?.nickname || member.user?.username || '未知用户',
                    role: member.role,
                    description: member.description || '暂无描述',
                    avatar: member.user, // 传递整个 user 对象给 getDisplayAvatarUrl
                    skills: member.skills || []
                };
                
                if (member.type === 0) {
                    // 开发人员
                    this.developers.push(memberData);
                } else if (member.type === 1) {
                    // 管理人员
                    this.managers.push(memberData);
                }
            });
        },
        
        getDisplayAvatarUrl(user) {
            const url = getUserAvatarUrl(user);
            // 如果返回 null，使用本地默认头像
            return url || require("@/assets/images/avatar/user.png");
        }
    },
};
</script>

<style scoped>
.team-view { width: 100%; min-height: 100vh; background-color: #f8f9fa; display: flex; flex-direction: column; }
.team-body { flex: 1; max-width: 1200px; width: 90%; margin: 0 auto; padding: 40px 0; }
.top-bar { margin-bottom: 16px; }
.team-header { text-align: center; margin-bottom: 48px; }
.team-header h1 { font-size: 48px; color: #212529; margin-bottom: 16px; font-weight: 700; }
.subtitle { font-size: 20px; color: #6c757d; font-weight: 400; }
.team-content { background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); padding: 40px; }
.team-section { margin-bottom: 48px; }
.team-section:last-child { margin-bottom: 0; }
.team-section h2 { font-size: 32px; color: #212529; margin-bottom: 32px; font-weight: 600; border-bottom: 3px solid #6c757d; padding-bottom: 12px; }
.team-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); gap: 24px; }
.team-member { background: #f8f9fa; border-radius: 12px; padding: 24px; border: 1px solid #e9ecef; transition: all 0.3s ease; }
.team-member:hover { transform: translateY(-4px); box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1); }
.member-avatar { text-align: center; margin-bottom: 20px; }
.member-avatar img { width: 120px; height: 120px; border-radius: 50%; border: 4px solid #fff; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); }
.member-info { text-align: center; }
.member-name { font-size: 24px; color: #212529; margin-bottom: 8px; font-weight: 600; }
.member-role { font-size: 16px; color: #6c757d; margin-bottom: 16px; font-weight: 500; }
.member-description { font-size: 14px; color: #495057; line-height: 1.6; margin-bottom: 20px; text-align: left; }
.member-skills { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.skill-tag { background: #6c757d; color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.join-us { background: #f8f9fa; padding: 32px; border-radius: 8px; border: 1px solid #e9ecef; }
.join-us p { font-size: 16px; color: #495057; line-height: 1.8; margin-bottom: 24px; }
.contact-info { background: #fff; padding: 20px; border-radius: 6px; border: 1px solid #e9ecef; }
.contact-info p { margin-bottom: 8px; color: #495057; }
.contact-info p:last-child { margin-bottom: 0; }
.contact-info a {
    color: #0088cc;
    text-decoration: none;
    font-weight: 500;
    padding: 2px 8px;
    border-radius: 4px;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
}
.contact-info a:hover {
    color: #006699;
    background: rgba(0, 136, 204, 0.1);
}
.contact-info a::before {
    content: '✈️';
    margin-right: 4px;
    font-size: 14px;
}
.loading-container { display: flex; justify-content: center; align-items: center; min-height: 300px; }
.no-data { text-align: center; padding: 40px; color: #6c757d; font-size: 16px; }
@media (max-width: 768px) { .team-body { width: 95%; padding: 20px 0; } .team-header h1 { font-size: 36px; } .subtitle { font-size: 18px; } .team-content { padding: 24px; } .team-section h2 { font-size: 28px; margin-bottom: 24px; } .team-grid { grid-template-columns: 1fr; gap: 20px; } .team-member { padding: 20px; } .member-avatar img { width: 100px; height: 100px; } .member-name { font-size: 20px; } .join-us { padding: 24px; } }
@media (max-width: 480px) { .team-body { width: 98%; padding: 16px 0; } .team-header h1 { font-size: 28px; } .subtitle { font-size: 16px; } .team-content { padding: 16px; } .team-section h2 { font-size: 24px; margin-bottom: 20px; } .team-member { padding: 16px; } .member-avatar img { width: 80px; height: 80px; } .member-name { font-size: 18px; } .member-role { font-size: 14px; } .member-description { font-size: 13px; } .join-us { padding: 20px; } .join-us p { font-size: 14px; } }
</style>
