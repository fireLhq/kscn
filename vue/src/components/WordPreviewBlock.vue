<template>
    <div class="word-block">
        <p v-if="block.kind === 'paragraph'" class="word-paragraph" :style="block.style">
            <template v-if="block.runs && block.runs.length">
                <template v-for="(run, index) in block.runs">
                    <img v-if="run.image" :key="index" :src="run.image" :style="run.style" alt="正文图片" class="word-inline-image" />
                    <span v-else :key="index" :style="run.style">{{ run.text }}</span>
                </template>
            </template>
            <template v-else>{{ block.text || '\u00a0' }}</template>
        </p>
        <table v-else-if="block.kind === 'table'" class="word-table" :style="block.style">
            <tbody v-if="block.cells && block.cells.length"><tr v-for="(row, rowIndex) in block.cells" :key="rowIndex">
                <template v-for="(cell, column) in row"><td v-if="!cell.hidden" :key="column" :colspan="cell.colspan" :rowspan="cell.rowspan" :style="cell.style">
                    <WordPreviewBlock v-for="(child, index) in cell.blocks" :key="index" :block="child" />
                </td></template>
            </tr></tbody>
            <tbody v-else><tr v-for="(row, rowIndex) in block.rows" :key="rowIndex"><td v-for="(cell, column) in row" :key="column">{{ cell }}</td></tr></tbody>
        </table>
        <img v-else-if="block.kind === 'image'" :src="block.image" alt="文档内图片" class="word-inline-image" />
    </div>
</template>

<script>
export default { name: 'WordPreviewBlock', props: { block: { type: Object, required: true } } };
</script>

<style scoped>
.word-block { display: contents; }
.word-paragraph { margin: 0 0 8pt; white-space: pre-wrap; overflow-wrap: anywhere; }
.word-inline-image { max-width: 100%; vertical-align: baseline; object-fit: contain; }
.word-table { border-collapse: collapse; max-width: 100%; margin: 0 0 8pt; table-layout: auto; }
.word-table td { border: none; padding: 3pt 5pt; vertical-align: top; min-width: 0; overflow-wrap: anywhere; }
.word-table .word-paragraph { margin-bottom: 0; }
</style>
