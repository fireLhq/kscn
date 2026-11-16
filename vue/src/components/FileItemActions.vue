<template>
    <div class="item-actions">
        <el-button size="mini" type="text" :title="item.type === 'folder' ? '打开文件夹' : '预览'" aria-label="预览" @click.stop="$emit('action', 'PREVIEW', item)"><i class="el-icon-view"></i></el-button>
        <el-button v-for="action in actions" :key="action.value" size="mini" type="text" :title="action.label" :aria-label="action.label"
            :class="{ 'login-required': !authenticated && action.write }" @click.stop="$emit('action', action.value, item)"><i :class="action.icon"></i></el-button>
    </div>
</template>

<script>
export default {
    name: 'FileItemActions',
    props: { item: { type: Object, required: true }, authenticated: { type: Boolean, default: true } },
    data() {
        return { actions: [
            { value: 'COPY', label: '复制', icon: 'el-icon-document-copy', write: true },
            { value: 'MOVE', label: '移动', icon: 'el-icon-rank', write: true },
            { value: 'DOWNLOAD', label: '下载', icon: 'el-icon-download' },
            { value: 'RENAME', label: '重命名', icon: 'el-icon-edit', write: true },
            { value: 'DELETE', label: '删除', icon: 'el-icon-delete', write: true },
            { value: 'PROPERTIES', label: '属性', icon: 'el-icon-info' },
        ] };
    },
};
</script>

<style scoped>
.item-actions { position: absolute; top: 0.5rem; right: 0.5rem; display: flex; flex-wrap: wrap; justify-content: flex-end; gap: 0.2rem; max-width: calc(100% - 2.5rem); opacity: 0; z-index: 10; }
.item-actions .el-button { margin: 0; padding: 0.3rem; width: 2rem; height: 2rem; min-width: auto; border-radius: 50%; background: rgba(255,255,255,0.95); border: 1px solid #e9ecef; box-shadow: 0 2px 4px #0001; }
.item-actions .el-button:hover { color: #fff; background: #667eea; border-color: #667eea; }
.item-actions .el-button.is-disabled, .item-actions .el-button.is-disabled:hover { color: #c0c4cc; background: #f4f4f5; border-color: #e4e7ed; }
.item-actions .el-button.login-required, .item-actions .el-button.login-required:hover { color: #909399; background: #f4f4f5; border-color: #d3d4d6; }
.item-actions .el-button i { font-size: 0.8rem; }
</style>
