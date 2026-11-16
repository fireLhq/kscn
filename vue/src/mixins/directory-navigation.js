export default {
    methods: {
        async restoreDirectoryTreeState(savedExpandedKeys = []) {
            const exists = id => id == null || !!this.findNodePathById(this.treeData, id);
            if (!exists(this.currentParentId)) {
                const ancestor = [...this.breadcrumbPath].reverse().find(item => exists(item.id));
                this.currentParentId = ancestor ? ancestor.id : null;
            }
            this.breadcrumbPath = this.buildBreadcrumbFromTree(this.currentParentId);
            const validKeys = new Set();
            const collect = nodes => nodes.forEach(node => {
                validKeys.add(node.path);
                collect(node.children || []);
            });
            collect(this.treeData);
            this.expandedKeys = [...new Set([
                'root', ...savedExpandedKeys.filter(key => validKeys.has(key)),
                ...this.getPathKeysToNode(this.currentParentId),
            ])];
            const key = this.showRecycleBin ? 'recycle' : this.currentParentId == null
                ? 'root' : this.findNodePathById(this.treeData, this.currentParentId);
            this.currentTreeNodeKey = key;
            // data 重建会重置 Element Tree 的 currentNode，即使 key 未改变也需要显式恢复。
            await this.$nextTick();
            if (this.$refs.tree) this.$refs.tree.setCurrentKey(key);
        },
    },
};
