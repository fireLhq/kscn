<template>
    <div class="footer-bottom">
        <div class="copyright">
            <div class="copyright-describe">
                <span>© 2025 KSCN 版权所有</span>
                <span>&nbsp;&nbsp;|&nbsp;&nbsp;</span>
                <img src="@/assets/images/components/footer/PoliceBadge.svg" alt="备案图标" />
                <span>&nbsp;</span>
                <a href="https://beian.mps.gov.cn/#/query/webSearch?code=50011902000523" rel="noreferrer" target="_blank">渝公网安备50011902000523</a>
                <span>&nbsp;&nbsp;</span>
                <a href="https://beian.miit.gov.cn/" target="_blank">渝ICP备2024044311号-1</a>
            </div>
            <div class="footer-icons">
                <a href="https://chatgpt.com/" target="_blank"><img src="@/assets/images/components/footer/ChatGPT.svg" alt="ChatGPT" /></a>
                <a href="https://cn.ubuntu.com/" target="_blank"><img src="@/assets/images/components/footer/Ubuntu.svg" alt="Ubuntu" /></a>
                <a href="https://www.deepseek.com/" target="_blank"><img src="@/assets/images/components/footer/DeepSeek.svg" alt="DeepSeek" /></a>
                <a href="https://www.mysql.com/" target="_blank"><img src="@/assets/images/components/footer/MySQL.svg" alt="MySQL" /></a>
                <a href="https://www.aliyun.com/" target="_blank"><img src="@/assets/images/components/footer/Aliyun.svg" alt="Aliyun" /></a>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: "Footer",
};
</script>

<style scoped>
/* 简洁响应式页脚样式 */
.footer-bottom {
    width: 100%;
    background-color: #495057;
    color: #fff;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    padding: 20px 0;
    margin-top: auto;
}

.copyright {
    max-width: 1400px;
    width: 90%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 0 auto;
    padding: 0 20px;
    flex-wrap: wrap;
    gap: 20px;
}

.copyright-describe {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    font-size: 14px;
    line-height: 1.6;
}

.copyright-describe span {
    color: #adb5bd;
}

.copyright-describe a {
    color: #ced4da;
    text-decoration: none;
    font-weight: 500;
    transition: color 0.2s ease;
    padding: 4px 8px;
    border-radius: 4px;
}

.copyright-describe a:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.1);
}

.copyright-describe img {
    height: 18px;
    width: auto;
    vertical-align: middle;
    opacity: 0.8;
}

.footer-icons {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
}

.footer-icons a {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    transition: all 0.2s ease;
    border: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-icons a:hover {
    background: rgba(255, 255, 255, 0.2);
}

.footer-icons img {
    height: 18px;
    width: auto;
    opacity: 0.8;
    transition: opacity 0.2s ease;
}

.footer-icons a:hover img {
    opacity: 1;
}

/* 大屏幕优化 (27寸及以上) */
@media (min-width: 1920px) {
    .copyright {
        max-width: 1600px;
        padding: 0 24px;
    }

    .copyright-describe {
        font-size: 15px;
        gap: 12px;
    }

    .copyright-describe img {
        height: 20px;
    }

    .footer-icons {
        gap: 16px;
    }

    .footer-icons a {
        width: 40px;
        height: 40px;
    }

    .footer-icons img {
        height: 20px;
    }
}

/* 中等屏幕 (1200px - 1919px) */
@media (max-width: 1919px) and (min-width: 1200px) {
    .copyright {
        max-width: 1400px;
        padding: 0 20px;
    }
}

/* 小屏幕 (768px - 1199px) */
@media (max-width: 1199px) and (min-width: 768px) {
    .copyright {
        width: 95%;
        padding: 0 16px;
    }

    .copyright-describe {
        font-size: 13px;
        gap: 8px;
    }

    .copyright-describe img {
        height: 16px;
    }

    .footer-icons {
        gap: 10px;
    }

    .footer-icons a {
        width: 32px;
        height: 32px;
    }

    .footer-icons img {
        height: 16px;
    }
}

/* 平板和手机横屏 (480px - 767px) */
@media (max-width: 767px) and (min-width: 480px) {
    .footer-bottom {
        padding: 16px 0;
    }

    .copyright {
        width: 98%;
        padding: 0 12px;
        flex-direction: column;
        text-align: center;
        gap: 16px;
    }

    .copyright-describe {
        justify-content: center;
        font-size: 12px;
        gap: 6px;
    }

    .copyright-describe img {
        height: 16px;
    }

    .footer-icons {
        gap: 8px;
    }

    .footer-icons a {
        width: 32px;
        height: 32px;
    }

    .footer-icons img {
        height: 16px;
    }
}

/* 手机竖屏 (< 480px) */
@media (max-width: 479px) {
    .footer-bottom {
        padding: 12px 0;
    }

    .copyright {
        width: 100%;
        padding: 0 8px;
        gap: 12px;
    }

    .copyright-describe {
        font-size: 11px;
        gap: 4px;
    }

    .copyright-describe img {
        height: 14px;
    }

    .footer-icons {
        gap: 6px;
    }

    .footer-icons a {
        width: 28px;
        height: 28px;
    }

    .footer-icons img {
        height: 14px;
    }
}
</style>
