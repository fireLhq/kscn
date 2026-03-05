/**
 * 头像工具函数
 * 统一处理头像URL的拼接和格式化
 * 企业标准实现：通过后端API访问头像，不依赖静态资源路径
 */

// 头像基础路径配置
const AVATAR_CONFIG = {
    // 头像API基础路径
    BASE_PATH: '/api/user/auth/avatar/',
    // 默认头像文件名（存储在后端头像目录中）
    DEFAULT_AVATAR_FILENAME: 'default_avatar.png'
};

/**
 * 获取完整的头像URL
 * @param {string} avatarFilename - 后端返回的头像文件名（可能为空或null）
 * @param {string} fallbackText - 当使用默认头像时显示的文字（可选，已废弃）
 * @returns {string|null} 完整的头像URL，如果为空返回 null
 */
export function getAvatarUrl(avatarFilename, fallbackText = 'U') {
    // 只有当头像文件名为空或空字符串时，才返回 null 使用本地默认头像
    if (!avatarFilename || avatarFilename.trim() === '') {
        return null; // 返回 null，让调用方使用本地默认头像
    }
    
    // 如果已经是完整的URL（包含http://或https://），直接返回
    if (avatarFilename.startsWith('http://') || avatarFilename.startsWith('https://')) {
        return avatarFilename;
    }
    
    // 拼接API路径和头像文件名（包括 default_avatar.png），添加时间戳避免缓存
    const timestamp = new Date().getTime();
    return AVATAR_CONFIG.BASE_PATH + avatarFilename + '?t=' + timestamp;
}

/**
 * 获取用户头像URL（带用户信息）
 * @param {Object} user - 用户对象
 * @param {string} user.avatar - 用户头像文件名
 * @param {string} user.nickname - 用户昵称
 * @param {string} user.username - 用户名
 * @returns {string|null} 完整的头像URL，如果是默认头像返回 null
 */
export function getUserAvatarUrl(user) {
    if (!user) {
        return null; // 返回 null，让调用方使用本地默认头像
    }
    
    const { avatar } = user;
    
    return getAvatarUrl(avatar);
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
