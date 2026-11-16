<template>
    <div class="sheet-preview">
        <table v-if="sheet" class="excel-table" :style="{ width: tableWidth + 'px' }">
            <colgroup><col style="width:42px" /><col v-for="(width, index) in widths" :key="index" :style="{ width: width + 'px' }" /></colgroup>
            <thead><tr><th class="corner">#</th><th v-for="(width, index) in widths" :key="index" :style="width ? {} : { display: 'none' }">{{ columnName(index) }}</th></tr></thead>
            <tbody><tr v-for="(row, index) in cells" :key="index" :style="rowStyle(index)">
                <th class="row-number">{{ index + 1 }}</th>
                <template v-for="(cell, column) in row"><td v-if="!cell.hidden" :key="column" :colspan="cell.colspan || 1" :rowspan="cell.rowspan || 1" :style="cell.style">
                    <div class="cell-content" :style="contentStyle(cell, index)">{{ cell.text }}</div>
                </td></template>
            </tr></tbody>
        </table>
    </div>
</template>

<script>
export default {
    name: 'ExcelSheetPreview',
    props: { sheet: { type: Object, default: null } },
    computed: {
        cells() { return this.sheet ? this.sheet.cells?.length ? this.sheet.cells : this.sheet.rows.map(row => row.map(text => ({ text, style: {}, textStyle: {} }))) : []; },
        widths() { return this.sheet?.columnWidths?.length ? this.sheet.columnWidths : Array.from({ length: Math.max(0, ...this.cells.map(row => row.length)) }, () => 80); },
        tableWidth() { return this.widths.reduce((sum, width) => sum + width, 42); },
    },
    methods: {
        columnName(index) { let name = ''; for (let value = index + 1; value; value = Math.floor((value - 1) / 26)) name = String.fromCharCode(65 + (value - 1) % 26) + name; return name; },
        rowStyle(index) { const height = this.sheet.rowHeights?.[index]; return height === 0 ? { display: 'none' } : height ? { height: height + 'pt' } : {}; },
        contentStyle(cell, index) {
            let height = 0;
            for (let row = index; row < index + (cell.rowspan || 1); row++) height += this.sheet.rowHeights?.[row] || 0;
            return { ...(height ? { maxHeight: height + 'pt' } : {}), ...cell.textStyle };
        },
    },
};
</script>

<style scoped>
.sheet-preview { flex: 1 1 0; min-height: 0; min-width: 0; overflow: auto; background: #fff; }
.excel-table { border-collapse: collapse; table-layout: fixed; font-family: Calibri,Arial,sans-serif; font-size: 11pt; color: #000; }
.excel-table td, .excel-table th { box-sizing: border-box; min-width: 0; border: 1px solid #e4e7ed; padding: 2px 4px; }
.excel-table td { overflow: hidden; vertical-align: bottom; font-weight: normal; padding: 0 2px; line-height: normal; }
.cell-content { overflow: hidden; }
.excel-table th { background: #f0f4fa; color: #64748b; font: 12px Arial,sans-serif; text-align: center; }
.excel-table thead th { position: sticky; top: 0; z-index: 2; height: 26px; }
.excel-table .row-number { position: sticky; left: 0; z-index: 1; }
.excel-table .corner { position: sticky; left: 0; z-index: 3; }
</style>
