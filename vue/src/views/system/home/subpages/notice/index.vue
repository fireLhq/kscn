<template>
    <div class="notice-view home-info-page">
        <div class="notice-body">
            <div class="top-bar">
                <HomePageReturn />
            </div>
            <div class="notice-header">
                <h1>使用须知</h1>
                <p class="subtitle">使用KSCN平台前请仔细阅读</p>
            </div>
            
            <div class="notice-content">
                <div class="notice-section">
                    <h2>文件使用提示</h2>
                    <div class="notice-text">
                        <ul>
                            <li>公共资源可浏览；上传及个人云盘等功能需要登录后使用。</li>
                            <li>常见文件支持小于 10 MB 的在线预览；大文件或暂不支持的格式请下载后使用相应软件打开。预览排版可能与原文件不同。</li>
                            <li>删除的文件进入回收站，保留 7 天；到期或手动永久删除后将无法恢复。重要文件请自行备份。</li>
                            <li>个人回收站仅本人可用，公共回收站仅管理员可用；个人回收站中的文件仍占用云盘空间。</li>
                        </ul>
                    </div>
                </div>
                <div class="notice-section">
                    <h2>平台使用规范</h2>
                    <div class="notice-text">
                        <p>欢迎使用KSCN平台！为了确保平台的健康发展和用户的良好体验，请您在使用过程中遵守以下规范：</p>
                        
                        <h3>1. 内容发布规范</h3>
                        <ul>
                            <li>发布的内容必须真实、准确，不得包含虚假信息</li>
                            <li>尊重知识产权，不得发布侵犯他人版权的材料</li>
                            <li>内容应当积极向上，不得包含违法、暴力、色情等不良信息</li>
                            <li>鼓励原创内容，支持知识分享和创新</li>
                        </ul>
                        
                        <h3>2. 用户行为规范</h3>
                        <ul>
                            <li>尊重其他用户，不得进行人身攻击或恶意评论</li>
                            <li>不得恶意刷屏、灌水或发布垃圾信息</li>
                            <li>不得利用平台进行商业推广或恶意营销</li>
                            <li>遵守相关法律法规，不得从事违法活动</li>
                        </ul>
                    </div>
                </div>
                
                <div class="notice-section">
                    <h2>隐私保护</h2>
                    <div class="notice-text">
                        <p>请妥善保管账号和个人资料，留意以下说明：</p>
                        
                        <h3>信息收集</h3>
                        <ul>
                            <li>仅收集必要的用户信息，用于平台功能实现</li>
                            <li>不会向第三方出售、出租或泄露用户个人信息</li>
                            <li>平台会记录必要的访问与操作日志，用于故障排查和安全管理</li>
                        </ul>
                        
                        <h3>信息使用</h3>
                        <ul>
                            <li>用户信息仅用于平台服务，不会用于其他商业目的</li>
                            <li>在获得用户明确同意前，不会向第三方分享信息</li>
                            <li>可在个人中心查看和修改已支持编辑的资料；其他资料处理需求请联系团队</li>
                        </ul>
                    </div>
                </div>
                
                <div class="notice-section">
                    <h2>免责声明</h2>
                    <div class="notice-text">
                        <p>KSCN平台作为知识共享平台，请注意以下免责条款：</p>
                        
                        <h3>内容责任</h3>
                        <ul>
                            <li>用户发布的内容仅代表个人观点，不代表平台立场</li>
                            <li>平台不对用户发布内容的真实性、准确性负责</li>
                            <li>如发现违规内容，平台有权进行删除或限制</li>
                        </ul>
                        
                        <h3>服务可用性</h3>
                        <ul>
                            <li>平台会尽力保证服务的稳定性和可用性</li>
                            <li>因技术维护、网络故障等原因造成的服务中断，平台不承担责任</li>
                            <li>平台保留因业务需要调整服务内容的权利</li>
                        </ul>
                    </div>
                </div>
                
                <div class="notice-section">
                    <h2>版权声明</h2>
                    <div class="notice-text">
                        <p>KSCN平台尊重知识产权，相关版权政策如下：</p>
                        
                        <h3>用户内容版权</h3>
                        <ul>
                            <li>内容版权归原权利人所有，上传或分享前请确认拥有相应权限</li>
                            <li>用户授权平台在服务范围内使用其内容</li>
                            <li>可按权限删除自己上传的内容；删除不会撤回其他用户已下载的副本</li>
                        </ul>
                        
                        <h3>平台内容版权</h3>
                        <ul>
                            <li>项目代码的使用、修改和分发请遵循仓库中的开源许可说明</li>
                            <li>代码许可不代表对用户上传内容、第三方图片或商标的使用授权</li>
                            <li>开源组件遵循相应的开源协议</li>
                        </ul>
                    </div>
                </div>
                
            </div>
        </div>
    </div>
</template>

<script>
import HomePageReturn from '@/components/HomePageReturn.vue';
export default { name: "NoticeView", components: { HomePageReturn } };
</script>

<style scoped>
.notice-view { width: 100%; min-height: 100vh; background-color: #f8f9fa; display: flex; flex-direction: column; }
.notice-body { flex: 1; max-width: 1200px; width: 90%; margin: 0 auto; padding: 40px 0; }
.top-bar { margin-bottom: 16px; }
.notice-header { text-align: center; margin-bottom: 48px; }
.notice-header h1 { font-size: 48px; color: #212529; margin-bottom: 16px; font-weight: 700; }
.subtitle { font-size: 20px; color: #6c757d; font-weight: 400; }
.notice-content { background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); padding: 40px; }
.notice-section { margin-bottom: 48px; }
.notice-section:last-child { margin-bottom: 0; }
.notice-section h2 { font-size: 32px; color: #212529; margin-bottom: 24px; font-weight: 600; border-bottom: 3px solid #6c757d; padding-bottom: 12px; }
.notice-text h3 { font-size: 20px; color: #212529; margin: 24px 0 16px 0; font-weight: 600; }
.notice-text p { font-size: 16px; color: #495057; line-height: 1.8; margin-bottom: 16px; }
.notice-text ul { margin-bottom: 20px; padding-left: 20px; }
.notice-text li { font-size: 16px; color: #495057; line-height: 1.6; margin-bottom: 8px; }
.contact-info { background: #f8f9fa; padding: 32px; border-radius: 8px; border: 1px solid #e9ecef; }
.contact-info p { font-size: 16px; color: #495057; line-height: 1.8; margin-bottom: 20px; }
.contact-details { background: #fff; padding: 20px; border-radius: 6px; border: 1px solid #e9ecef; margin-bottom: 20px; }
.contact-details p { margin-bottom: 8px; color: #495057; }
.contact-details p:last-child { margin-bottom: 0; }
.note { font-style: italic; color: #6c757d; margin-bottom: 0; }
@media (max-width: 768px) { .notice-body { width: 95%; padding: 20px 0; } .notice-header h1 { font-size: 36px; } .subtitle { font-size: 18px; } .notice-content { padding: 24px; } .notice-section h2 { font-size: 28px; margin-bottom: 20px; } .notice-text h3 { font-size: 18px; margin: 20px 0 12px 0; } .notice-text p { font-size: 15px; } .notice-text li { font-size: 15px; } .contact-info { padding: 24px; } }
@media (max-width: 480px) { .notice-body { width: 98%; padding: 16px 0; } .notice-header h1 { font-size: 28px; } .subtitle { font-size: 16px; } .notice-content { padding: 16px; } .notice-section h2 { font-size: 24px; margin-bottom: 16px; } .notice-text h3 { font-size: 16px; margin: 16px 0 10px 0; } .notice-text p { font-size: 14px; } .notice-text li { font-size: 14px; } .contact-info { padding: 20px; } .contact-info p { font-size: 14px; } }
</style>
