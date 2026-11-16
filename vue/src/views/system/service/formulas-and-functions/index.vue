<template>
    <main class="algorithm-page math-page">
        <div class="algorithm-container">
            <header class="math-header">
                <button type="button" class="back-link" aria-label="返回上一页" @click="$router.back()"><i class="el-icon-arrow-left" /> 返回</button>
                <div><h1>公式与函数</h1><p>点选数学符号构建公式，绘制函数与方程，导出适合文档和教学的图像。</p></div>
            </header>
            <el-tabs v-model="tab" class="math-tabs">
                <el-tab-pane label="公式编辑与导出" name="formula">
                    <div class="formula-layout">
                        <section class="math-panel" aria-label="公式编辑器">
                            <div class="section-heading"><h2>构建公式</h2><el-select size="mini" placeholder="载入示例" :value="null" @change="loadFormula"><el-option v-for="example in formulaExamples" :key="example[0]" :label="example[0]" :value="example[1]" /></el-select></div>
                            <el-radio-group v-model="symbolGroup" size="mini"><el-radio-button v-for="(group,i) in symbolGroups" :key="group.name" :label="i">{{ group.name }}</el-radio-button></el-radio-group>
                            <div class="symbol-palette">
                                <button v-for="symbol in symbolGroups[symbolGroup].symbols" :key="symbol[0]" type="button" :title="symbol[1]" :aria-label="'插入' + symbol[0]" @mousedown.prevent @click="insertFormula(symbol[1])">{{ symbol[0] }}</button>
                            </div>
                            <label class="input-label" for="latex-source">LaTeX 源码（可直接编辑；符号插入到光标位置，或包裹选中内容）</label>
                            <textarea id="latex-source" ref="latexEditor" v-model="latex" rows="7" spellcheck="false" maxlength="4000" />
                            <div class="math-toolbar">
                                <label>字号 <el-input-number v-model="fontSize" size="mini" :min="12" :max="120" controls-position="right" /></label>
                                <label>颜色 <input v-model="formulaColor" type="color" aria-label="公式颜色" /></label>
                                <el-button size="mini" @click="latex = ''">清空</el-button>
                            </div>
                            <p class="math-note">支持分数、根式、上下标、积分、求和、矩阵及分段公式。输入可带 $…$ 或 $$…$$，导出时按所选格式处理。内容在浏览器中本地处理。</p>
                        </section>
                        <section class="math-panel" aria-label="公式预览和导出">
                            <div class="section-heading"><h2>公式预览</h2><span class="math-note">{{ formulaRendering ? '正在排版…' : '透明背景' }}</span></div>
                            <el-alert v-if="formulaError" :title="formulaError" type="error" :closable="false" />
                            <div class="image-board formula-board"><img v-if="formulaUrl && !formulaError" :src="formulaUrl" alt="公式预览" /><p v-else class="empty-hint">在左侧输入公式或点选数学符号</p></div>
                            <p v-if="formulaResult" class="math-note">原始尺寸 {{ formulaResult.width }} × {{ formulaResult.height }} px；SVG 可无损缩放。</p>
                            <div class="math-toolbar">
                                <el-button size="mini" type="primary" :disabled="!formulaReady || exporting" @click="exportFormula('svg')">保存 SVG</el-button>
                                <el-button size="mini" :disabled="!formulaReady || exporting" @click="exportFormula('png')">保存透明 PNG</el-button>
                                <label>PNG 倍率 <el-select v-model="pngScale" size="mini" class="short-select"><el-option v-for="n in [1,2,3,4]" :key="n" :label="n + '×'" :value="n" /></el-select></label>
                            </div>
                            <div class="math-toolbar"><el-radio-group v-model="latexWrapped" size="mini"><el-radio-button :label="false">LaTeX</el-radio-button><el-radio-button :label="true">$LaTeX$</el-radio-button></el-radio-group><el-button size="mini" :disabled="!formulaReady || exporting" @click="exportFormula('tex')">保存源码</el-button><el-button size="mini" :disabled="!formulaReady" @click="copyLatex">复制源码</el-button></div>
                            <el-input :value="exportLatex" type="textarea" :rows="3" readonly aria-label="待导出 LaTeX" />
                        </section>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="函数与方程绘图" name="plot">
                    <section class="math-panel curve-panel" aria-label="图像表达式">
                        <div class="section-heading"><h2>图像表达式</h2><div class="math-toolbar"><el-button size="mini" :disabled="plotBusy || curves.length >= 12" @click="addCurve">添加图像</el-button><el-select size="mini" :disabled="plotBusy || curves.length >= 12" :value="null" placeholder="添加示例" @change="addExample"><el-option label="抛物线 y=x²" value="parabola" /><el-option label="正弦 y=sin(x)" value="sine" /><el-option label="圆 x²+y²=4" value="circle" /><el-option label="双曲线 x²−y²=1" value="hyperbola" /><el-option label="竖线 x=2" value="vertical" /></el-select></div></div>
                        <el-form size="mini" :disabled="plotBusy">
                            <div v-for="(curve,i) in curves" :key="curve.id" class="curve-row">
                                <el-checkbox v-model="curve.visible" :aria-label="'显示第' + (i+1) + '条图像'" />
                                <input v-model="curve.color" type="color" :disabled="plotBusy" :aria-label="'第' + (i+1) + '条图像颜色'" />
                                <el-select v-model="curve.mode" class="mode-select" :aria-label="'第' + (i+1) + '条绘图类型'"><el-option label="y = f(x)" value="y" /><el-option label="x = f(y)" value="x" /><el-option label="隐式方程" value="implicit" /></el-select>
                                <el-input :ref="'curveInput' + curve.id" v-model="curve.expression" class="curve-expression" placeholder="如 sin(x) 或 x^2+y^2=4" maxlength="500" :aria-label="'第' + (i+1) + '条表达式'" @focus="activeCurveId = curve.id" />
                                <label>线宽 <el-input-number v-model="curve.width" :min="0.5" :max="8" :step="0.5" controls-position="right" /></label>
                                <el-button icon="el-icon-delete" :aria-label="'移除第' + (i+1) + '条图像'" @click="removeCurve(curve.id)" />
                                <div class="curve-range"><span>曲线显示范围</span><label>x <el-input-number v-model="curve.xMin" :min="-1000000" :max="1000000" :controls="false" aria-label="曲线定义域最小值" /> 至 <el-input-number v-model="curve.xMax" :min="-1000000" :max="1000000" :controls="false" aria-label="曲线定义域最大值" /></label><label>y <el-input-number v-model="curve.yMin" :min="-1000000" :max="1000000" :controls="false" aria-label="曲线值域最小值" /> 至 <el-input-number v-model="curve.yMax" :min="-1000000" :max="1000000" :controls="false" aria-label="曲线值域最大值" /></label></div>
                            </div>
                        </el-form>
                        <div class="plot-symbols"><span>插入到当前表达式：</span><el-button v-for="symbol in plotSymbols" :key="symbol[0]" size="mini" :disabled="plotBusy || !curves.length" @mousedown.native.prevent @click="insertPlot(symbol[1])">{{ symbol[0] }}</el-button></div>
                        <details class="math-note"><summary>表达式语法与绘图精度</summary><p>支持 + − * / ^、隐式乘法 2x / 2(x+1)、pi、e、sin、cos、tan、sqrt、abs、exp、ln、log 等；三角函数使用弧度，ln 为自然对数，log 为常用对数。绘图输入使用上述语法，不直接解析 LaTeX。含 x、y 的一般方程请选择“隐式方程”，例如 x^2+y^2=4。</p><p>隐式方程通过数值等值线近似绘制；提高网格精度可改善结果，孤立点、相切零点和高频细节可能漏绘。显式函数在不连续处尽量断开，不替代严格的数学分析。每条曲线的 x、y 范围均用于裁切该曲线。</p></details>
                    </section>
                    <div class="plot-layout">
                        <section class="math-panel plot-settings" aria-label="绘图设置">
                            <el-form size="mini" :disabled="plotBusy" @submit.native.prevent>
                                <el-collapse v-model="settingsOpen">
                                    <el-collapse-item title="画布视窗与坐标轴范围" name="ranges">
                                        <p class="math-note">视窗决定画面范围；坐标轴和曲线范围分别控制，不互相覆盖。</p>
                                        <div v-for="range in rangeFields" :key="range[0]" class="range-row"><span>{{ range[0] }}</span><el-input-number v-model="plot[range[1]]" :controls="false" :min="-1000000" :max="1000000" :aria-label="range[0] + '最小值'" /><span>至</span><el-input-number v-model="plot[range[2]]" :controls="false" :min="-1000000" :max="1000000" :aria-label="range[0] + '最大值'" /></div>
                                        <el-checkbox v-model="plot.equalScale">横纵轴等比例（圆形保持为圆）</el-checkbox>
                                    </el-collapse-item>
                                    <el-collapse-item title="刻度、精度与教学样式" name="axes">
                                        <div class="setting-row"><label>X 刻度间隔</label><el-input-number v-model="plot.xStep" :min="0.000001" :max="1000000" :controls="false" /></div>
                                        <div class="setting-row"><label>Y 刻度间隔</label><el-input-number v-model="plot.yStep" :min="0.000001" :max="1000000" :controls="false" /></div>
                                        <div class="setting-row"><label>刻度最多小数位</label><el-input-number v-model="plot.decimals" :min="0" :max="8" controls-position="right" /></div>
                                        <div class="setting-row"><label>函数采样点数</label><el-input-number v-model="plot.samples" :min="100" :max="2400" :step="100" controls-position="right" /></div>
                                        <div class="setting-row"><label>方程网格精度</label><el-input-number v-model="plot.gridSize" :min="30" :max="220" :step="10" controls-position="right" /></div>
                                        <el-checkbox v-model="plot.extend">坐标轴超出末端刻度</el-checkbox>
                                        <div class="setting-row"><label>超出长度 / 刻度间隔</label><el-input-number v-model="plot.extension" :disabled="!plot.extend" :min="0" :max="1" :step="0.05" controls-position="right" /></div>
                                        <div class="visibility-options"><el-checkbox v-for="item in displayFields" :key="item[0]" v-model="plot[item[0]]">{{ item[1] }}</el-checkbox></div>
                                        <el-checkbox v-model="plot.transparent">透明背景（取消则为白色）</el-checkbox>
                                    </el-collapse-item>
                                    <el-collapse-item title="输出尺寸与导出范围" name="output">
                                        <div class="setting-row"><label>宽度 px</label><el-input-number v-model="plot.width" :min="320" :max="2400" :step="80" controls-position="right" /></div>
                                        <div class="setting-row"><label>高度 px</label><el-input-number v-model="plot.height" :min="240" :max="1800" :step="80" controls-position="right" /></div>
                                        <el-checkbox v-model="crop.enabled">只导出指定定义域 / 值域</el-checkbox>
                                        <div v-if="crop.enabled"><p class="math-note">下列范围用于重新取景导出，坐标轴和每条曲线仍遵守各自范围。</p><div v-for="range in cropFields" :key="range[0]" class="range-row"><span>{{ range[0] }}</span><el-input-number v-model="crop[range[1]]" :controls="false" :min="-1000000" :max="1000000" /><span>至</span><el-input-number v-model="crop[range[2]]" :controls="false" :min="-1000000" :max="1000000" /></div></div>
                                    </el-collapse-item>
                                </el-collapse>
                            </el-form>
                        </section>
                        <section class="math-panel plot-preview" aria-label="绘图预览和导出">
                            <div class="section-heading"><h2>图像预览</h2><el-tag v-if="plotStale" size="mini" type="warning">设置已变更</el-tag></div>
                            <div class="math-toolbar"><el-button size="mini" type="primary" :loading="plotBusy" @click="draw">{{ plotBusy ? '绘制中' : '绘制 / 更新图像' }}</el-button><el-button size="mini" :disabled="!plotReady || exporting" @click="exportPlot('svg')">保存 SVG</el-button><el-button size="mini" :disabled="!plotReady || exporting" @click="exportPlot('png')">保存 PNG</el-button><label>PNG <el-select v-model="pngScale" size="mini" class="short-select"><el-option v-for="n in [1,2,3,4]" :key="n" :label="n + '×'" :value="n" /></el-select></label></div>
                            <el-alert v-if="plotError" :title="plotError" type="error" :closable="false" />
                            <el-alert v-if="plotWarnings.length" :title="plotWarnings.join('；')" type="warning" :closable="false" />
                            <div class="image-board plot-board"><img v-if="plotUrl" :src="plotUrl" alt="函数与方程绘图预览" /><p v-else class="empty-hint">添加表达式后点击绘制</p></div>
                            <p class="math-note">棋盘格仅用于提示透明区域，不会导出。保存图像包含当前勾选的坐标轴、曲线、表达式、刻度与网格。{{ crop.enabled ? '已开启指定范围导出，导出取景与上方预览范围可能不同。' : '导出范围与预览一致。' }}</p>
                        </section>
                    </div>
                </el-tab-pane>
            </el-tabs>
            <el-alert v-if="exportError" :title="exportError" type="error" :closable="false" />
            <p class="page-note">{{ exporting ? '正在生成下载文件，请稍候…' : '所有内容在浏览器本地处理；请及时导出，刷新或离开页面不会自动保存。' }}</p>
        </div>
    </main>
</template>
<script>
import '../algorithm/algorithm.css';
import { symbolGroups, formulaExamples, plotSymbols } from './symbols';
import { defaultPlot, buildPlot, validateRange } from './plot';
import { downloadBlob, svgBlob, svgPng } from './export';

const colors = ['#667eea','#e66d55','#28a58d','#ad67ca','#d59b28','#347ec1'];
export default {
    name: 'FormulasAndFunctionsIndex',
    data() { return {
        tab: 'formula', symbolGroups, formulaExamples, plotSymbols, symbolGroup: 0,
        latex: formulaExamples[0][1], fontSize: 32, formulaColor: '#223248', latexWrapped: false,
        formulaResult: null, formulaUrl: '', formulaError: '', formulaRendering: false, formulaStale: true, formulaVersion: 0, formulaTimer: null,
        pngScale: 2, exporting: false, exportError: '', disposed: false, dirty: false,
        plot: defaultPlot(), curves: [this.newCurve(1,'y','x^2','#667eea'),this.newCurve(2,'implicit','x^2+y^2=4','#e66d55')],
        nextId: 3, activeCurveId: 1, plotUrl: '', plotResult: null, plotBusy: false, plotStale: true, plotError: '', plotWarnings: [],
        settingsOpen: ['ranges','axes'], crop: {enabled: false,xMin: -3,xMax: 3,yMin: -3,yMax: 3},
        rangeFields: [['视窗 X','xMin','xMax'],['视窗 Y','yMin','yMax'],['X 轴','axisXMin','axisXMax'],['Y 轴','axisYMin','axisYMax']],
        cropFields: [['定义域','xMin','xMax'],['值域','yMin','yMax']],
        displayFields: [['axes','坐标轴'],['curves','图像'],['expressions','表达式'],['ticks','刻度线'],['numbers','刻度数字'],['arrows','箭头'],['grid','网格']]
    }; },
    computed: {
        formulaReady() { return !!this.formulaResult && !this.formulaStale && !this.formulaRendering && !this.formulaError; },
        plotReady() { return !!this.plotResult && !this.plotStale && !this.plotBusy && !this.plotError; },
        exportLatex() { const value=this.formulaResult?.latex || ''; return value && this.latexWrapped ? '$'+value+'$' : value; }
    },
    watch: {
        latex() { this.scheduleFormula(); }, fontSize() { this.scheduleFormula(); }, formulaColor() { this.scheduleFormula(); },
        plot: { deep:true, handler() { this.plotStale=true; this.dirty=true; } },
        curves: { deep:true, handler() { this.plotStale=true; this.dirty=true; } },
        crop: {deep:true, handler() { this.dirty=true; }}
    },
    mounted() { this.renderFormula(); this.draw(); window.addEventListener('beforeunload',this.unload); },
    beforeDestroy() { this.disposed=true; this.formulaVersion++; clearTimeout(this.formulaTimer); for(const url of [this.formulaUrl,this.plotUrl]) if(url) URL.revokeObjectURL(url); window.removeEventListener('beforeunload',this.unload); },
    async beforeRouteLeave(to,from,next) {
        if(this.plotBusy||this.exporting) { this.$message.warning('请等待绘图或导出完成'); next(false); return; }
        if(!this.dirty) { next(); return; }
        try { await this.$confirm('离开后编辑内容将丢失，请先导出需要的公式或图像。','离开页面',{type:'warning',confirmButtonText:'离开',cancelButtonText:'继续编辑',closeOnClickModal:false}); next(); } catch(_) { next(false); }
    },
    methods: {
        newCurve(id,mode='y',expression='sin(x)',color=colors[(id-1)%colors.length]) { return {id,mode,expression,color,visible:true,width:2,xMin:-6,xMax:6,yMin:-4,yMax:4}; },
        unload(event) { if(this.dirty||this.exporting||this.plotBusy) {event.preventDefault();event.returnValue='';} },
        setPreview(kind,svg) { const old=this[kind+'Url']; this[kind+'Url']=URL.createObjectURL(svgBlob(svg)); if(old) URL.revokeObjectURL(old); },
        loadFormula(value) { this.latex=value; },
        insertAt(value,template,element) {
            const start=element?.selectionStart ?? value.length,end=element?.selectionEnd ?? start,selected=value.slice(start,end);
            const placeholder=template.indexOf('□'),insert=template.replace('□',selected||'x');
            return {value:value.slice(0,start)+insert+value.slice(end),start:placeholder<0?start+insert.length:start+placeholder,end:placeholder<0?start+insert.length:start+placeholder+(selected||'x').length};
        },
        insertFormula(template) { if (/\\[a-zA-Z]+$/.test(template)) template += ' '; const element=this.$refs.latexEditor,result=this.insertAt(this.latex,template,element); this.latex=result.value; this.$nextTick(()=>{element.focus();element.setSelectionRange(result.start,result.end);}); },
        insertPlot(template) {
            const curve=this.curves.find(c=>c.id===this.activeCurveId)||this.curves[0]; if(!curve) return;
            const ref=this.$refs['curveInput'+curve.id],component=Array.isArray(ref)?ref[0]:ref,element=component?.$el.querySelector('input');
            const result=this.insertAt(curve.expression,' '+template+' ',element); curve.expression=result.value;
            this.$nextTick(()=>{element?.focus();element?.setSelectionRange(result.start,result.end);});
        },
        scheduleFormula() { this.dirty=true;this.formulaStale=true;this.formulaVersion++;clearTimeout(this.formulaTimer);this.formulaTimer=setTimeout(()=>this.renderFormula(),250); },
        async renderFormula() {
            const version=++this.formulaVersion;this.formulaRendering=true;this.formulaError='';
            try { const {formulaSvg}=await import(/* webpackChunkName: "formula-renderer" */ './formula'); const result=formulaSvg(this.latex,{fontSize:this.fontSize,color:this.formulaColor}); if(this.disposed||version!==this.formulaVersion)return;this.formulaResult=result;this.setPreview('formula',result.svg);this.formulaStale=false; }
            catch(e) { if(!this.disposed&&version===this.formulaVersion)this.formulaError=e.message||'公式渲染失败'; }
            finally { if(version===this.formulaVersion)this.formulaRendering=false; }
        },
        async exportFormula(type) {
            if(!this.formulaReady||this.exporting)return;this.exporting=true;this.exportError='';
            try { const blob=type==='tex'?new Blob([this.exportLatex],{type:'text/plain;charset=utf-8'}):type==='svg'?svgBlob(this.formulaResult.svg):await svgPng(this.formulaResult.svg,this.pngScale); downloadBlob(blob,'公式.'+type); } catch(e) {this.exportError=e.message||'导出失败';} finally {this.exporting=false;}
        },
        async copyLatex() { if(!this.formulaReady)return; try { if(navigator.clipboard?.writeText)await navigator.clipboard.writeText(this.exportLatex);else {const input=document.createElement('textarea');input.value=this.exportLatex;input.style.position='fixed';input.style.opacity='0';document.body.appendChild(input);try{input.select();if(!document.execCommand('copy'))throw new Error('请手动选中并复制下方源码');}finally{input.remove();}}this.$message.success('已复制 LaTeX');}catch(e){this.exportError=e.message||'复制失败，请手动复制源码';} },
        addCurve() { if(this.plotBusy||this.curves.length>=12)return;const curve=this.newCurve(this.nextId++);this.curves.push(curve);this.activeCurveId=curve.id; },
        addExample(name) { if(this.plotBusy||this.curves.length>=12)return;const examples={parabola:['y','x^2'],sine:['y','sin(x)'],circle:['implicit','x^2+y^2=4'],hyperbola:['implicit','x^2-y^2=1'],vertical:['x','2']};const [mode,expression]=examples[name];const curve=this.newCurve(this.nextId++,mode,expression);this.curves.push(curve);this.activeCurveId=curve.id; },
        removeCurve(id) { if(!this.plotBusy)this.curves=this.curves.filter(c=>c.id!==id); },
        async draw() {
            if(this.plotBusy)return;this.plotBusy=true;this.plotError='';this.plotWarnings=[];
            try { await this.$nextTick();await new Promise(resolve=>setTimeout(resolve,0));const result=await buildPlot(this.curves,this.plot);if(this.disposed)return;this.plotResult=result;this.setPreview('plot',result.svg);this.plotWarnings=result.warnings;this.plotStale=false; }
            catch(e) {if(!this.disposed)this.plotError=e.message||'绘制失败';}finally{this.plotBusy=false;}
        },
        async exportPlot(type) {
            if(!this.plotReady||this.exporting)return;this.exporting=true;this.plotBusy=true;this.exportError='';
            try { let result=this.plotResult;if(this.crop.enabled){validateRange(this.crop.xMin,this.crop.xMax,'导出定义域');validateRange(this.crop.yMin,this.crop.yMax,'导出值域');const {xMin,xMax,yMin,yMax}=this.crop;result=await buildPlot(this.curves,this.plot,{xMin,xMax,yMin,yMax});}const blob=type==='svg'?svgBlob(result.svg):await svgPng(result.svg,this.pngScale);downloadBlob(blob,'函数与方程.'+type); }
            catch(e){this.exportError=e.message||'导出失败';}finally{this.plotBusy=false;this.exporting=false;}
        }
    }
};
</script>
<style scoped>
.math-header { display:flex;align-items:center;gap:16px;margin-bottom:18px; }
.math-header h1 { margin:0 0 6px;font-size:22px;line-height:1.3; }
.math-header p,.page-note { margin:0;color:#8794a6;font-size:12px;line-height:1.7; }
.math-tabs ::v-deep .el-tabs__header { margin-bottom:16px; }
.math-panel { min-width:0;padding:16px;border:1px solid #dfe6f0;border-radius:18px;background:#fff;box-shadow:0 12px 36px #1f29370a; }
.formula-layout { display:grid;grid-template-columns:1fr 1fr;gap:16px; }
.section-heading { display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:10px;margin-bottom:12px; }
.section-heading h2 { margin:0;font-size:16px; }
.section-heading .el-select { width:180px; }
.symbol-palette { display:flex;flex-wrap:wrap;gap:6px;min-height:80px;margin:12px 0;align-content:flex-start; }
.symbol-palette button { min-width:34px;height:30px;padding:4px 9px;background:#f4f6ff;color:#5268cc;border:1px solid #dce1ff;border-radius:6px;font-size:13px;cursor:pointer; }
.symbol-palette button:hover { background:#e8ecff;border-color:#a9b5f5; }
.symbol-palette button:focus-visible { outline:2px solid #667eea;outline-offset:2px; }
.input-label { display:block;font-size:12px;color:#6b7d92;margin-bottom:8px;line-height:1.6; }
textarea#latex-source { display:block;width:100%;box-sizing:border-box;resize:vertical;padding:10px;border:1px solid #dce3ef;border-radius:8px;font:14px/1.7 Consolas,monospace;color:#223248; }
textarea#latex-source:focus { outline:2px solid #c7cff8;border-color:#667eea; }
.math-toolbar { display:flex;align-items:center;flex-wrap:wrap;gap:8px;margin:10px 0;font-size:12px;color:#52657c; }
.math-toolbar label { display:inline-flex;align-items:center;gap:6px; }
.math-toolbar .el-button,.plot-symbols .el-button { margin:0; }
.math-toolbar .el-input-number { width:90px; }
.math-toolbar .short-select { width:65px; }
input[type="color"] { flex-shrink:0;width:28px;height:28px;padding:2px;border:1px solid #dce3ef;border-radius:6px;background:#fff;cursor:pointer; }
.math-note { color:#8794a6;font-size:12px;line-height:1.7; }
.math-note p { margin:6px 0; }
.math-note summary { width:fit-content;cursor:pointer; }
.image-board { display:flex;align-items:center;justify-content:center;overflow:auto;border:1px solid #e5eaf3;border-radius:10px;background-color:#fff;background-image:linear-gradient(45deg,#edf0f5 25%,transparent 25%),linear-gradient(-45deg,#edf0f5 25%,transparent 25%),linear-gradient(45deg,transparent 75%,#edf0f5 75%),linear-gradient(-45deg,transparent 75%,#edf0f5 75%);background-size:16px 16px;background-position:0 0,0 8px,8px -8px,-8px 0; }
.formula-board { min-height:200px;padding:18px;box-sizing:border-box; }
.formula-board img { max-width:100%;height:auto; }
.empty-hint { color:#8794a6;font-size:13px;text-align:center; }
.curve-panel { margin-bottom:16px; }
.curve-row { display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:10px 0;border-top:1px solid #edf0f6; }
.curve-row .el-checkbox { margin:0; }
.curve-row .mode-select { width:120px; }
.curve-expression { flex:1;min-width:220px; }
.curve-row > label { display:flex;align-items:center;gap:5px;color:#6b7d92;font-size:12px; }
.curve-row > label .el-input-number { width:80px; }
.curve-range { display:flex;align-items:center;flex-wrap:wrap;gap:6px 18px;width:100%;padding-left:36px;box-sizing:border-box;color:#8794a6;font-size:12px; }
.curve-range label { display:flex;align-items:center;gap:6px; }
.curve-range .el-input-number { width:80px; }
.plot-symbols { display:flex;align-items:center;flex-wrap:wrap;gap:6px;margin:10px 0;color:#8794a6;font-size:12px; }
.plot-layout { display:grid;grid-template-columns:350px minmax(0,1fr);gap:16px;align-items:start; }
.plot-settings ::v-deep .el-collapse { border:0; }
.plot-settings ::v-deep .el-collapse-item__header { height:38px;font-size:13px;color:#52657c; }
.plot-settings ::v-deep .el-collapse-item__content { padding-bottom:12px; }
.range-row { display:flex;align-items:center;gap:6px;margin:8px 0;color:#6b7d92;font-size:12px; }
.range-row > span:first-child { width:52px;flex-shrink:0; }
.range-row .el-input-number { flex:1;width:80px;min-width:0; }
.setting-row { display:flex;align-items:center;justify-content:space-between;gap:8px;margin:8px 0;font-size:12px;color:#6b7d92; }
.setting-row .el-input-number { width:110px; }
.visibility-options { display:flex;align-items:center;flex-wrap:wrap;gap:6px 12px;margin:10px 0; }
.math-page ::v-deep .el-checkbox__label { font-size:12px; }
.visibility-options .el-checkbox { margin:0; }
.plot-board img { display:block;max-width:100%;height:auto; }
.plot-board { min-height:260px; }
.plot-preview .el-alert { margin-bottom:10px; }
.page-note { margin-top:18px; }
@media(max-width:1050px){.formula-layout{grid-template-columns:1fr}.plot-layout{grid-template-columns:310px minmax(0,1fr)}}
@media(max-width:800px){.plot-layout{grid-template-columns:minmax(0,1fr)}.math-panel{padding:12px;border-radius:14px}.math-header h1{font-size:18px}.curve-range{padding-left:0}.curve-expression{flex-basis:calc(100% - 180px);min-width:140px}}
</style>
