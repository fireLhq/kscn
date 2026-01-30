/**
 * 头像工具函数
 * 统一处理头像URL的拼接和格式化
 */

// 头像基础路径配置
const AVATAR_CONFIG = {
    // 头像文件服务器基础路径
    BASE_PATH: '/api/static/image/avatar/',
    // 默认头像URL（当用户没有头像时使用）
    DEFAULT_AVATAR: 'https://via.placeholder.com/40x40/409EFF/FFFFFF?text=U'
};

/**
 * 获取完整的头像URL
 * @param {string} avatarPath - 后端返回的头像路径（可能为空或null）
 * @param {string} fallbackText - 当使用默认头像时显示的文字（可选）
 * @returns {string} 完整的头像URL
 */
export function getAvatarUrl(avatarPath, fallbackText = 'U') {
    // 如果没有头像路径或为空字符串，返回默认头像
    if (!avatarPath || avatarPath.trim() === '') {
        return fallbackText ? 
            `https://via.placeholder.com/40x40/409EFF/FFFFFF?text=${fallbackText}` : 
            AVATAR_CONFIG.DEFAULT_AVATAR;
    }
    
    // 如果已经是完整的URL（包含http://或https://），直接返回
    if (avatarPath.startsWith('http://') || avatarPath.startsWith('https://')) {
        return avatarPath;
    }
    
    // 如果是本地资源路径（以/开头或者是webpack处理的资源），直接返回
    if (avatarPath.startsWith('/') || avatarPath.includes('static/') || avatarPath.includes('assets/')) {
        return avatarPath;
    }
    
    // 拼接基础路径和头像文件名
    return AVATAR_CONFIG.BASE_PATH + avatarPath;
}

/**
 * 获取用户头像URL（带用户信息）
 * @param {Object} user - 用户对象
 * @param {string} user.avatar - 用户头像路径
 * @param {string} user.nickname - 用户昵称
 * @param {string} user.username - 用户名
 * @returns {string} 完整的头像URL
 */
export function getUserAvatarUrl(user) {
    if (!user) {
        return AVATAR_CONFIG.DEFAULT_AVATAR;
    }
    
    const { avatar, nickname, username } = user;
    const fallbackText = (nickname || username || 'U').charAt(0).toUpperCase();
    
    return getAvatarUrl(avatar, fallbackText);
}

/**
 * 获取头像基础路径（用于配置管理）
 * @returns {string} 头像基础路径
 */
export function getAvatarBasePath() {
    return AVATAR_CONFIG.BASE_PATH;
}

/**
 * 更新头像基础路径（用于配置变更）
 * @param {string} newBasePath - 新的基础路径
 */
export function setAvatarBasePath(newBasePath) {
    AVATAR_CONFIG.BASE_PATH = newBasePath;
}

export default {
    getAvatarUrl,
    getUserAvatarUrl,
    getAvatarBasePath,
    setAvatarBasePath
};
