import FileOperations from '@/components/FileOperations.vue';
import FileItemActions from '@/components/FileItemActions.vue';
import NetworkFileUpload from '@/components/NetworkFileUpload.vue';

export default {
    components: { FileOperations, FileItemActions, NetworkFileUpload },
    data: () => ({ uploadMode: 'LOCAL', networkUploadBusy: false }),
    methods: {
        async onNetworkFileAdded() { this.showUploadDialog = false; await this.refreshAfterFileOperation(); },
        handleFileItemAction(action, item) {
            if (action === 'PREVIEW') return item.type === 'folder' ? this.handleItemClick(item) : this.$refs.fileOperations.openPreview(item);
            if (action === 'PROPERTIES') return this.$refs.fileOperations.openProperties(item);
            if (action === 'DOWNLOAD') return item.type === 'folder' ? this.downloadFolder(item) : this.downloadItem(item);
            if (action === 'RENAME') return this.renameItem(item);
            if (action === 'DELETE') return this.deleteItem(item);
            if (action === 'COPY' || action === 'MOVE' || action === 'TRANSFER') return this.openFileOperation(action, [item]);
        },
        handleFileMenuRequest(action, items) {
            if (action === 'DOWNLOAD') return this.batchDownload();
            if (action === 'DELETE') {
                if (this.requireLogin && !this.requireLogin()) return;
                return this.batchDelete();
            }
            return this.openFileOperation(action, items);
        },
        setFileSelection(items) {
            this.selectedItems = [...items];
            this.$nextTick(() => this.syncTableSelection());
        },
        selectAllFiles() { this.setFileSelection(this.displayItems); },
        clearFileSelection() { this.setFileSelection([]); },
        onFileContextMenu(event, item) {
            event.preventDefault(); event.stopPropagation();
            if (item && !this.isSelected(item)) this.setFileSelection([item]);
            this.$nextTick(() => this.$refs.fileOperations?.showMenu(event));
        },
        onFileRowContextMenu(row, column, event) { this.onFileContextMenu(event, row); },
        openFileOperation(action, items) {
            if (this.requireLogin && !this.requireLogin()) return;
            this.$refs.fileOperations.open(action, items);
        },
        async refreshAfterFileOperation() {
            this.setFileSelection([]);
            await this.loadDirectoryTree(true);
            this.breadcrumbPath = this.buildBreadcrumbFromTree(this.currentParentId);
            await this.loadCurrentDirectory();
            if (this.isSearchMode) await this.handleSearch();
            if (this.loadSpaceInfo) await this.loadSpaceInfo();
            this.updateTreeHighlight();
        },
    },
};
