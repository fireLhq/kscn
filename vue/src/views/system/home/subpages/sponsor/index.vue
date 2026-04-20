<template>
    <div class="sponsor-view">
        <div class="sponsor-body">
            <div class="top-bar">
                <el-button type="text" @click="$router.back()"><i class="el-icon-arrow-left"></i> 返回</el-button>
            </div>
            <div class="sponsor-header">
                <h1>赞助支持</h1>
                <p class="subtitle">您的支持是我们持续发展的动力</p>
            </div>
            
            <div class="sponsor-content">
                <div class="sponsor-section">
                    <h2>为什么需要赞助？</h2>
                    <div class="reasons-grid">
                        <div class="reason-item">
                            <div class="reason-icon">🚀</div>
                            <h3>持续开发</h3>
                            <p>支持项目的持续开发和功能更新</p>
                        </div>
                        <div class="reason-item">
                            <div class="reason-icon">🛠️</div>
                            <h3>技术升级</h3>
                            <p>保持技术栈的先进性和安全性</p>
                        </div>
                        <div class="reason-item">
                            <div class="reason-icon">🌐</div>
                            <h3>服务器维护</h3>
                            <p>确保平台的稳定运行和访问速度</p>
                        </div>
                        <div class="reason-item">
                            <div class="reason-icon">📚</div>
                            <h3>内容建设</h3>
                            <p>丰富平台的知识内容和学习资源</p>
                        </div>
                    </div>
                </div>
                
                <div class="sponsor-section">
                    <h2>赞助方式</h2>
                    <div class="payment-methods">
                        <div class="payment-method">
                            <div class="payment-icon">
                                <img src="@/assets/images/system/home/subpages/sponsor/支付宝.svg" alt="支付宝" />
                            </div>
                            <h3>支付宝</h3>
                            <p>使用支付宝扫码或转账</p>
                            <div class="qr-code">
                                <img src="@/assets/images/system/home/subpages/sponsor/支付宝二维码.jpg" alt="支付宝二维码" />
                            </div>
                            <p class="account-info">huohua.lhq@outlook.com</p>
                        </div>
                        
                        <div class="payment-method">
                            <div class="payment-icon">
                                <img src="@/assets/images/system/home/subpages/sponsor/微信.svg" alt="微信" />
                            </div>
                            <h3>微信支付</h3>
                            <p>使用微信扫码支付</p>
                            <div class="qr-code">
                                <img src="@/assets/images/system/home/subpages/sponsor/微信二维码.png" alt="微信二维码" />
                            </div>
                            <p class="account-info">微信号：DiracEq</p>
                        </div>
                        
                        <div class="payment-method">
                            <div class="payment-icon">
                                <img src="@/assets/images/system/home/subpages/sponsor/PayPal.svg" alt="PayPal" />
                            </div>
                            <h3>PayPal</h3>
                            <p>国际用户可使用PayPal</p>
                            <div class="qr-code">
                                <img src="@/assets/images/system/home/subpages/sponsor/未填二维码.svg" alt="PayPal二维码" />
                            </div>
                            <p class="account-info">邮箱：huohua.lhq@gmail.com</p>
                        </div>

                        <div class="payment-method">
                            <div class="payment-icon">
                                <img src="@/assets/images/system/home/subpages/sponsor/银联.svg" alt="银行卡" />
                            </div>
                            <h3>银行转账</h3>
                            <p>支持各大银行转账（人民币）</p>
                            <div class="bank-info">
                                <p><strong>开户行：</strong>中国银行</p>
                                <p><strong>账号：</strong>62**************319</p>
                                <p><strong>户名：</strong>**骑</p>
                            </div>
                            <div class="bank-info">
                                <p><strong>开户行：</strong>中国农业银行</p>
                                <p><strong>账号：</strong>62**************173</p>
                                <p><strong>户名：</strong>**骑</p>
                            </div>
                        </div>
                        
                        <div class="payment-method">
                            <div class="payment-icon">
                                <img src="@/assets/images/system/home/subpages/sponsor/Visa.svg" alt="银行卡" />
                            </div>
                            <h3>银行转账</h3>
                            <p>支持各大银行转账（美元）</p>
                            <div class="bank-info">
                                <p><strong>开户行：</strong>中国银行</p>
                                <p><strong>账号：</strong>40***********810</p>
                                <p><strong>户名：</strong>**骑</p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="sponsor-section">
                    <h2>赞助等级</h2>
                    <div class="sponsor-levels">
                        <div class="sponsor-level">
                            <div class="level-header bronze">
                                <h3>青铜赞助者</h3>
                                <p class="amount">¥50-199</p>
                            </div>
                            <div class="level-benefits">
                                <ul>
                                    <li>每一份支持都会被我们认真珍惜</li>
                                    <li>帮助平台分担日常运行与维护成本</li>
                                    <li>为后续功能完善提供持续动力</li>
                                </ul>
                            </div>
                        </div>
                        
                        <div class="sponsor-level">
                            <div class="level-header silver">
                                <h3>白银赞助者</h3>
                                <p class="amount">¥200-499</p>
                            </div>
                            <div class="level-benefits">
                                <ul>
                                    <li>体现你对平台长期发展的认可与鼓励</li>
                                    <li>帮助我们投入更多精力完善核心功能</li>
                                    <li>让平台能够持续保持稳定更新</li>
                                </ul>
                            </div>
                        </div>
                        
                        <div class="sponsor-level">
                            <div class="level-header gold">
                                <h3>黄金赞助者</h3>
                                <p class="amount">¥500-999</p>
                            </div>
                            <div class="level-benefits">
                                <ul>
                                    <li>为平台优化体验与补充功能提供更大支持</li>
                                    <li>帮助我们覆盖更多开发与服务成本</li>
                                    <li>让 KSCN 能够更从容地持续成长</li>
                                </ul>
                            </div>
                        </div>
                        
                        <div class="sponsor-level">
                            <div class="level-header platinum">
                                <h3>铂金赞助者</h3>
                                <p class="amount">¥1000+</p>
                            </div>
                            <div class="level-benefits">
                                <ul>
                                    <li>是对平台建设与维护非常重要的支持力量</li>
                                    <li>帮助我们在稳定运行之外推进更多规划</li>
                                    <li>让这个项目能够陪伴更多用户长期使用</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="sponsor-section">
                    <h2>联系我们</h2>
                    <div class="contact-info">
                        <p>如果您有任何赞助相关的问题，或者希望了解更详细的赞助方案，请随时联系我们：</p>
                        <div class="contact-details">
                            <p><strong>邮箱：</strong>lab_c919@qq.com</p>
                            <p><strong>QID：</strong>AirC919</p>
                            <p><strong>微信：</strong>DiracEq</p>
                            <p><strong>Telegram：</strong>191-2268-5314</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: "SponsorView",
};
</script>

<style scoped>
.sponsor-view { width: 100%; min-height: 100vh; background-color: #f8f9fa; display: flex; flex-direction: column; }
.sponsor-body { flex: 1; max-width: 1200px; width: 90%; margin: 0 auto; padding: 40px 0; }
.top-bar { margin-bottom: 16px; }
.sponsor-header { text-align: center; margin-bottom: 48px; }
.sponsor-header h1 { font-size: 48px; color: #212529; margin-bottom: 16px; font-weight: 700; }
.subtitle { font-size: 20px; color: #6c757d; font-weight: 400; }
.sponsor-content { background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); padding: 40px; }
.sponsor-section { margin-bottom: 48px; }
.sponsor-section:last-child { margin-bottom: 0; }
.sponsor-section h2 { font-size: 32px; color: #212529; margin-bottom: 32px; font-weight: 600; border-bottom: 3px solid #6c757d; padding-bottom: 12px; }
.reasons-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 24px; }
.reason-item { text-align: center; padding: 24px; background: #f8f9fa; border-radius: 8px; border: 1px solid #e9ecef; }
.reason-icon { font-size: 48px; margin-bottom: 16px; }
.reason-item h3 { font-size: 20px; color: #212529; margin-bottom: 12px; font-weight: 600; }
.reason-item p { font-size: 14px; color: #6c757d; margin: 0; }
.payment-methods { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 24px; }
.payment-method { text-align: center; padding: 24px; background: #f8f9fa; border-radius: 8px; border: 1px solid #e9ecef; }
.payment-icon img { width: 80px; height: 80px; border-radius: 8px; margin-bottom: 16px; }
.payment-method h3 { font-size: 20px; color: #212529; margin-bottom: 8px; font-weight: 600; }
.payment-method p { font-size: 14px; color: #6c757d; margin-bottom: 16px; }
.qr-code img { width: 200px; height: 200px; border-radius: 8px; margin-bottom: 16px; }
.account-info { font-size: 14px; color: #495057; font-weight: 500; }
.bank-info { background: #fff; padding: 16px; border-radius: 6px; border: 1px solid #e9ecef; text-align: left; }
.bank-info p { margin-bottom: 8px; font-size: 14px; color: #495057; }
.bank-info p:last-child { margin-bottom: 0; }
.sponsor-levels { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 24px; }
.sponsor-level { border: 1px solid #e9ecef; border-radius: 8px; overflow: hidden; }
.level-header { padding: 20px; text-align: center; color: white; }
.level-header.bronze { background: linear-gradient(135deg, #CD7F32, #B8860B); }
.level-header.silver { background: linear-gradient(135deg, #C0C0C0, #A9A9A9); }
.level-header.gold { background: linear-gradient(135deg, #FFD700, #FFA500); }
.level-header.platinum { background: linear-gradient(135deg, #E5E4E2, #BCC6CC); }
.level-header h3 { font-size: 20px; margin-bottom: 8px; font-weight: 600; }
.amount { font-size: 24px; font-weight: 700; margin: 0; }
.level-benefits { padding: 20px; background: #fff; }
.level-benefits ul { list-style: none; padding: 0; margin: 0; }
.level-benefits li { font-size: 14px; color: #495057; margin-bottom: 8px; padding-left: 20px; position: relative; }
.level-benefits li::before { content: "✓"; position: absolute; left: 0; color: #28a745; font-weight: bold; }
.level-benefits li:last-child { margin-bottom: 0; }
.contact-info { background: #f8f9fa; padding: 32px; border-radius: 8px; border: 1px solid #e9ecef; }
.contact-info p { font-size: 16px; color: #495057; line-height: 1.8; margin-bottom: 24px; }
.contact-details { background: #fff; padding: 20px; border-radius: 6px; border: 1px solid #e9ecef; }
.contact-details p { margin-bottom: 8px; color: #495057; }
@media (max-width: 768px) { .sponsor-body { width: 95%; padding: 20px 0; } .sponsor-header h1 { font-size: 36px; } .subtitle { font-size: 18px; } .sponsor-content { padding: 24px; } .sponsor-section h2 { font-size: 28px; margin-bottom: 24px; } .reasons-grid { grid-template-columns: 1fr; gap: 16px; } .payment-methods { grid-template-columns: 1fr; gap: 20px; } .sponsor-levels { grid-template-columns: 1fr; gap: 20px; } .contact-info { padding: 24px; } }
@media (max-width: 480px) { .sponsor-body { width: 98%; padding: 16px 0; } .sponsor-header h1 { font-size: 28px; } .subtitle { font-size: 16px; } .sponsor-content { padding: 16px; } .sponsor-section h2 { font-size: 24px; margin-bottom: 20px; } .reason-item { padding: 20px; } .reason-icon { font-size: 36px; } .reason-item h3 { font-size: 18px; } .payment-method { padding: 20px; } .payment-icon img { width: 60px; height: 60px; } .qr-code img { width: 150px; height: 150px; } .level-header { padding: 16px; } .level-header h3 { font-size: 18px; } .amount { font-size: 20px; } .level-benefits { padding: 16px; } .contact-info { padding: 20px; } .contact-info p { font-size: 14px; } }
</style>
