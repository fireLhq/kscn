<template>
    <button type="button" class="home-page-return" aria-label="返回上一页" @click="$router.back()">
        <i class="el-icon-arrow-left" aria-hidden="true"></i>
        <span>返回</span>
    </button>
</template>

<script>
export default { name: 'HomePageReturn' };
</script>

<style>
.home-page-return {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    min-height: 44px;
    padding: 10px 18px;
    border: 1px solid #dce3fa;
    border-radius: 24px;
    background: #fff;
    color: #465ed4;
    box-shadow: 0 4px 20px rgba(41, 62, 120, 0.16);
    text-decoration: none;
    font-size: 14px;
    font-weight: 600;
    font-family: inherit;
    cursor: pointer;
}
.home-page-return:hover { background: #eef2ff; }
.home-page-return:focus-visible { outline: 3px solid #667eea; outline-offset: 3px; }
.home-info-page .top-bar {
    position: sticky;
    top: 82px;
    z-index: 950;
    width: fit-content;
    max-width: 100%;
    margin: 0 0 16px;
}
.home-info-page :is(.about-header, .team-header, .sponsor-header, .notice-header) > h1 { color: #243653; letter-spacing: 0.06em; }
.home-info-page > div > :is(.about-content, .team-content, .sponsor-content, .notice-content) { border: 1px solid #e7ecf5; border-radius: 20px; }
.home-info-page [class$="-section"] > h2 { border-color: #dce4fb; color: #344c77; }
.home-info-page .payment-method, .home-info-page .team-member { min-width: 0; }
.home-info-page .account-info { overflow-wrap: anywhere; }
.home-info-page .qr-code img { max-width: 100%; height: auto; }
.home-info-page :is(.level-header.silver, .level-header.gold, .level-header.platinum) { color: #28364b; }
@media (max-width: 768px) {
    .home-info-page .top-bar { top: 72px; }
    .home-info-page > :is(.about-body, .team-body, .sponsor-body, .notice-body) { width: 92%; }
}
</style>
