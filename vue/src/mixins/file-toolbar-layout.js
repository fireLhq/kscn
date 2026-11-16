// Element UI compensates the body when dialogs hide its scrollbar. Fixed
// toolbars must use that same content width when centering on large screens.
export default {
    mounted() {
        const update = () => {
            const padding = parseFloat(window.getComputedStyle(document.body).paddingRight) || 0;
            this.$el.style.setProperty('--file-body-padding-right', `${padding}px`);
        };
        this.fileToolbarBodyObserver = new MutationObserver(update);
        this.fileToolbarBodyObserver.observe(document.body, {
            attributes: true,
            attributeFilter: ['style', 'class']
        });
        update();
    },
    beforeDestroy() {
        if (this.fileToolbarBodyObserver) this.fileToolbarBodyObserver.disconnect();
    }
};
